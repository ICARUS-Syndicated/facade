package rwp.annieawa.recipeaddon.config;

import io.papermc.paper.datapack.Datapack;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import rwp.annieawa.recipeaddon.RecipeAddon;
import rwp.annieawa.recipeaddon.model.CustomRecipe;
import rwp.annieawa.recipeaddon.model.ShapedRecipeModel;
import rwp.annieawa.recipeaddon.model.ShapelessRecipeModel;
import rwp.annieawa.recipeaddon.model.TransmuteRecipeModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class RecipeLoader {
    private final RecipeAddon plugin;
    // 存储配方 key -> 结果物品（克隆）
    private final Map<String, ItemStack> recipeResults = new HashMap<>();
    private final Map<String, List<ItemStack>> recipeIngredients = new HashMap<>();
    private final Map<String, SpecialRecipeInfo> specialRecipes = new HashMap<>();

    private final String namespace;
    private final String datapackname;

    public RecipeLoader(RecipeAddon plugin, String namespace, String datapackname) {
        this.plugin = plugin;
        this.namespace = namespace;
        this.datapackname = datapackname;
    }

    // 存储配方 key -> 期望的 item_model (NamespacedKey)
    private final Map<String, NamespacedKey> recipeItemModels = new HashMap<>();
    private final Map<String, ConfigurationSection> resultComponentSections = new HashMap<>();

    // 提供公共访问方法
    public NamespacedKey getItemModelForRecipe(String fullKey) {
        return recipeItemModels.get(fullKey);
    }

    public SpecialRecipeInfo getSpecialRecipeInfo(String fullKey) {
        return specialRecipes.get(fullKey);
    }

    public List<ItemStack> getIngredients(String key) { // 新增
        return recipeIngredients.get(key);
    }

    public ConfigurationSection getResultComponentSection(String fullKey) {
        return resultComponentSections.get(fullKey);
    }

    private final Set<NamespacedKey> registeredKeys = new HashSet<>();

    // 获取已注册的 keys（只读）
    public Set<NamespacedKey> getRegisteredKeys() {
        return Collections.unmodifiableSet(registeredKeys);
    }

    // 在成功注册配方后调用
    private void addRegisteredKey(NamespacedKey key) {
        registeredKeys.add(key);
    }

    // 移除本插件所有配方
    public void removeAllPluginRecipes() {
        List<NamespacedKey> toRemove = new ArrayList<>();
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            if (recipe instanceof Keyed keyed) {
                NamespacedKey key = keyed.getKey();
                if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                    toRemove.add(key);
                }
            }
        }
        for (NamespacedKey key : toRemove) {
            Bukkit.removeRecipe(key);
        }
        registeredKeys.clear(); // 清空记录
    }

    /**
     * 加载所有配方并注册到服务器
     *
     * @return 成功加载的配方数量
     */
    public int loadAllRecipes() {
        recipeItemModels.clear();
        recipeResults.clear();
        recipeIngredients.clear();
        resultComponentSections.clear();
        registeredKeys.clear();
        int count = 0;

        File ourdatapacks = new File(Bukkit.getWorldContainer(), "world/datapacks/" + datapackname);
        if (ourdatapacks.exists()) {
            try {
                java.util.Map<String, String> env = new java.util.HashMap<>();
                env.put("create", "false");

                try (FileSystem fs = FileSystems.newFileSystem(
                        java.nio.file.FileSystems.getDefault().getPath(ourdatapacks.getAbsolutePath()),
                        env)) {
                    Path recipesPath = fs.getPath("/data/" + namespace + "/customrecipes/");
                    if (Files.exists(recipesPath)) {
                        try (Stream<Path> paths = Files.walk(recipesPath)) {
                            List<Path> recipePaths = paths
                                    .filter(Files::isRegularFile)
                                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                                    .collect(java.util.stream.Collectors.toList());

                            for (Path recipePath : recipePaths) {
                                try {
                                    // 从ZIP中读取文件内容并创建临时配置
                                    String content = Files.readString(recipePath);
                                    YamlConfiguration config = YamlConfiguration.loadConfiguration(
                                            new java.io.StringReader(content));

                                    int loaded = 0;
                                    if (registerRecipe(config)) {
                                        loaded++;
                                    }
                                    count += loaded;
                                    plugin.getLogger().info(" 加载了 " + loaded + " 个配方");
                                } catch (Exception e) {
                                    plugin.getLogger().log(Level.WARNING, "加载配方文件 " + recipePath.toString() + " 时出错", e);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "扫描数据包失败", e);
            }
        } else {
            plugin.getLogger().info("数据包不存在: " + ourdatapacks.getAbsolutePath());
        }
        return count;
    }

    /**
     * 从单个 YAML 文件加载配方（支持一个文件包含多个配方）
     */
    private int loadRecipesFromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int loaded = 0;

        // 如果文件直接是一个配方（顶层有 type 字段）
        if (config.contains("type")) {
            if (registerRecipe(config)) {
                loaded++;
            }
        } else {
            // 否则认为文件包含多个配方，每个顶级键为一个配方
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section != null && registerRecipe(section)) {
                    loaded++;
                }
            }
        }
        return loaded;
    }

    /**
     * 根据配置段注册单个配方
     *
     * @return 是否成功注册
     */
    private boolean registerRecipe(ConfigurationSection section) {
        try {
            String type = section.getString("type");
            if (type == null) {
                plugin.getLogger().warning("跳过配方：缺少 type 字段");
                return false;
            }

            String key = section.getString("key");
            if (key == null) {
                plugin.getLogger().warning("跳过配方：缺少 key 字段");
                return false;
            }

            // 解析 result
            ItemStack result = parseItemStack(section.getConfigurationSection("result"));
            if (result == null) {
                plugin.getLogger().warning("跳过配方 " + key + "：结果物品无效");
                return false;
            }

            CustomRecipe recipeModel;
            if ("shaped".equalsIgnoreCase(type)) {
                List<String> pattern = section.getStringList("pattern");
                if (pattern.isEmpty()) {
                    plugin.getLogger().warning("跳过有序配方 " + key + "：pattern 不能为空");
                    return false;
                }

                ConfigurationSection ingredientsSection = section.getConfigurationSection("ingredients");
                if (ingredientsSection == null) {
                    plugin.getLogger().warning("跳过有序配方 " + key + "：缺少 ingredients");
                    return false;
                }

                Map<Character, ShapedRecipeModel.IngredientInfo> ingredients = new HashMap<>();
                for (String charKey : ingredientsSection.getKeys(false)) {
                    if (charKey.length() != 1) {
                        plugin.getLogger().warning("跳过有序配方 " + key + "：原料键必须是单个字符，但得到 " + charKey);
                        return false;
                    }
                    char symbol = charKey.charAt(0);
                    ConfigurationSection ingSec = ingredientsSection.getConfigurationSection(charKey);
                    if (ingSec == null) {
                        plugin.getLogger().warning("跳过有序配方 " + key + "：原料 " + charKey + " 配置无效");
                        return false;
                    }

                    ItemStack ing = parseItemStack(ingSec);
                    if (ing == null) {
                        plugin.getLogger().warning("跳过有序配方 " + key + "：原料 " + charKey + " 无效");
                        return false;
                    }

                    // 读取 match-mode，默认为 true（精确匹配）
                    String matchMode = ingSec.getString("match-mode", "exact");
                    boolean exactMatch = !"material".equalsIgnoreCase(matchMode); // 如果不是 material，则为 exact

                    ingredients.put(symbol, new ShapedRecipeModel.IngredientInfo(ing, exactMatch));
                }

                recipeModel = new ShapedRecipeModel(key, pattern, ingredients, result);
            } else if ("shapeless".equalsIgnoreCase(type)) {
                // 解析无序配方：使用 getMapList() 获取原料列表
                List<Map<?, ?>> ingMaps = section.getMapList("ingredients");
                if (ingMaps.isEmpty()) {
                    plugin.getLogger().warning("跳过无序配方 " + key + "：ingredients 不能为空");
                    return false;
                }

                List<ItemStack> ingredients = new ArrayList<>();
                for (Map<?, ?> map : ingMaps) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stringMap = (Map<String, Object>) map;
                    // 将 Map 转换为临时 ConfigurationSection，以便复用 parseItemStack
                    ConfigurationSection ingSec = new org.bukkit.configuration.MemoryConfiguration().createSection("temp", stringMap);
                    ItemStack ing = parseItemStack(ingSec);
                    if (ing == null) {
                        plugin.getLogger().warning("跳过无序配方 " + key + "：某个原料无效");
                        return false;
                    }
                    ingredients.add(ing);
                }

                recipeModel = new ShapelessRecipeModel(key, ingredients, result);
            } else if ("transmute".equalsIgnoreCase(type)) {
                // === 新增：转化配方 ===
                ConfigurationSection inputSection = section.getConfigurationSection("input");
                ConfigurationSection materialSection = section.getConfigurationSection("material");

                if (inputSection == null || materialSection == null) {
                    plugin.getLogger().warning("跳过转化配方 " + key + "：缺少 input 或 material");
                    return false;
                }

                ItemStack input = parseItemStack(inputSection);
                ItemStack material = parseItemStack(materialSection);

                if (input == null || material == null) {
                    plugin.getLogger().warning("跳过转化配方 " + key + "：input 或 material 无效");
                    return false;
                }

                NamespacedKey fullKey = new NamespacedKey(plugin, key);

                // 保存 result.components 配置节（如果有）
                ConfigurationSection resultComponents = section.getConfigurationSection("result.components");
                if (resultComponents != null) {
                    resultComponentSections.put(fullKey.toString(), resultComponents);
                }
                // 从 result 的 components 中读取 item-model（如果有）
                if (resultComponents != null && resultComponents.contains("item-model")) {
                    String modelStr = resultComponents.getString("item-model");
                    if (modelStr != null && !modelStr.isEmpty()) {
                        NamespacedKey modelKey;
                        if (modelStr.contains(":")) {
                            modelKey = NamespacedKey.fromString(modelStr);
                        } else {
                            modelKey = new NamespacedKey(plugin, modelStr);
                        }
                        if (modelKey != null) {
                            // 使用完整的 key 字符串存储
                            recipeItemModels.put(fullKey.toString(), modelKey);
                        }
                    }
                }

                recipeModel = new TransmuteRecipeModel(key, input, material, result);

            } else {
                plugin.getLogger().warning("跳过配方 " + key + "：未知类型 " + type);
                return false;
            }
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            // 注册到 Bukkit
            Bukkit.addRecipe(recipeModel.toBukkitRecipe());
            addRegisteredKey(namespacedKey);
            if (recipeModel != null) {
                // 成功注册后，存储结果物品（克隆一份，防止后续被修改）
                recipeResults.put(key, result.clone());
                // 根据配方类型生成原料列表
                List<ItemStack> ingredients = new ArrayList<>();

                if ("shaped".equalsIgnoreCase(type)) {
                    // 有序配方：按 pattern 顺序（从左到右、从上到下）收集非空格原料
                    List<String> pattern = section.getStringList("pattern");
                    ConfigurationSection ingredientsSec = section.getConfigurationSection("ingredients");
                    if (pattern != null && ingredientsSec != null) {
                        for (String row : pattern) {
                            for (char c : row.toCharArray()) {
                                if (c != ' ') {
                                    ConfigurationSection ingSec = ingredientsSec.getConfigurationSection(String.valueOf(c));
                                    if (ingSec != null) {
                                        ItemStack ing = parseItemStack(ingSec);
                                        if (ing != null) {
                                            ingredients.add(ing.clone());
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if ("shapeless".equalsIgnoreCase(type)) {
                    // 无序配方：直接使用解析时的原料列表（已存储在 recipeModel 中？）
                    // 但我们在解析时已经生成了 ingredients 列表，需要复用或重新解析。
                    // 由于在解析无序配方时我们已有 List<ItemStack>，可以在那时就存入映射。
                    // 这里我们重新解析一次，保持统一逻辑。
                    List<Map<?, ?>> ingMaps = section.getMapList("ingredients");
                    for (Map<?, ?> map : ingMaps) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> stringMap = (Map<String, Object>) map;
                        ConfigurationSection ingSec = new MemoryConfiguration().createSection("temp", stringMap);
                        ItemStack ing = parseItemStack(ingSec);
                        if (ing != null) ingredients.add(ing.clone());
                    }
                } else if ("transmute".equalsIgnoreCase(type)) {
                    // 转化配方：input 和 material
                    ConfigurationSection inputSec = section.getConfigurationSection("input");
                    ConfigurationSection materialSec = section.getConfigurationSection("material");
                    if (inputSec != null) {
                        ItemStack input = parseItemStack(inputSec);
                        if (input != null) ingredients.add(input.clone());
                    }
                    if (materialSec != null) {
                        ItemStack material = parseItemStack(materialSec);
                        if (material != null) ingredients.add(material.clone());
                    }
                }
                if (section.contains("special")) {
                    ConfigurationSection specialSec = section.getConfigurationSection("special");
                    if (specialSec != null) {
                        SpecialRecipeInfo info = new SpecialRecipeInfo();
                        String targetMatStr = specialSec.getString("target-material");
                        if (targetMatStr != null) {
                            info.targetMaterial = Material.getMaterial(targetMatStr.toUpperCase());
                        }
                        info.copyInput = specialSec.getBoolean("copy-input", true);
                        info.components = specialSec.getConfigurationSection("components");

                        // 解析 source-character
                        if (specialSec.contains("source-character")) {
                            String charStr = specialSec.getString("source-character");
                            if (charStr != null && charStr.length() == 1) {
                                info.sourceCharacter = charStr.charAt(0);
                            }
                        }
                        // 解析 source-slot
                        if (specialSec.contains("source-slot")) {
                            int slot = specialSec.getInt("source-slot");
                            if (slot >= 0 && slot <= 8) {
                                info.sourceSlot = slot;
                            } else {
                                plugin.getLogger().warning("特殊配方 " + key + " 的 source-slot " + slot + " 无效，必须 0-8");
                            }
                        }

                        NamespacedKey fullKey = new NamespacedKey(plugin, key);
                        specialRecipes.put(fullKey.toString(), info);
                        plugin.getLogger().info("已存储特殊配方: " + fullKey + " -> " + info.targetMaterial +
                                (info.sourceCharacter != null ? ", sourceChar=" + info.sourceCharacter : "") +
                                (info.sourceSlot != null ? ", sourceSlot=" + info.sourceSlot : ""));
                    }
                }
                recipeIngredients.put(key, ingredients);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "解析配方时发生异常", e);
            return false;
        }
    }

    public ItemStack getResultItem(String key) {
        return recipeResults.get(key);
    }

    public Set<String> getAllRecipeKeys() {
        return recipeResults.keySet();
    }

    /**
     * 解析一个物品的配置节，返回带有组件的 ItemStack
     * 配置格式示例：
     * material: DIAMOND_SWORD
     * amount: 1
     * components:
     * custom-model-data: 1001
     * display-name: "红剑"
     * lore:
     * - "第一行"
     * enchantments:
     * - sharpness:5
     * unbreakable: true
     */
    private ItemStack parseItemStack(ConfigurationSection section) {
        if (section == null) return null;

        String materialName = section.getString("material");
        if (materialName == null) return null;

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) return null;

        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);

        // 解析 components 子节
        ConfigurationSection components = section.getConfigurationSection("components");
        if (components != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;

            // CustomModelData (保留兼容)
            if (components.contains("custom-model-data")) {
                int cmd = components.getInt("custom-model-data");
                meta.setCustomModelData(cmd);
            }

            // === 新增：item_model 组件支持 ===
            // 格式: "item-model: namespace:path" 或直接 "item-model: path"
            if (components.contains("item-model")) {
                String itemModelStr = components.getString("item-model");
                if (itemModelStr != null && !itemModelStr.isEmpty()) {
                    NamespacedKey modelKey;
                    if (itemModelStr.contains(":")) {
                        modelKey = NamespacedKey.fromString(itemModelStr);
                    } else {
                        modelKey = new NamespacedKey(plugin, itemModelStr);
                    }
                    if (modelKey != null) {
                        meta.setItemModel(modelKey);
                    }
                }
            }
            if (components.contains("glint")) {
                boolean ws = components.getBoolean("glint");
                if (ws) {
                    // 解析命名空间ID
                    meta.setEnchantmentGlintOverride(ws);
                }
            }
            // DisplayName (MiniMessage 格式)
            if (components.contains("display-name")) {
                String name = components.getString("display-name");
                if (name != null) {
                    meta.displayName(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize(name));
                }
            }

            // Lore
            if (components.contains("lore")) {
                List<String> loreLines = components.getStringList("lore");
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            .deserialize(line));
                }
                meta.lore(lore);
            }

            // Enchantments
            if (components.contains("enchantments")) {
                List<String> enchList = components.getStringList("enchantments");
                for (String enchEntry : enchList) {
                    String[] parts = enchEntry.split(":");
                    if (parts.length == 2) {
                        try {
                            org.bukkit.enchantments.Enchantment ench =
                                    org.bukkit.Registry.ENCHANTMENT.get(
                                            org.bukkit.NamespacedKey.minecraft(parts[0].toLowerCase())
                                    );
                            int level = Integer.parseInt(parts[1]);
                            if (ench != null) {
                                meta.addEnchant(ench, level, true);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            // Unbreakable
            if (components.getBoolean("unbreakable", false)) {
                meta.setUnbreakable(true);
            }
            if (components.contains("maxdamage")) {
                int maxDamage = components.getInt("maxdamage");
                // 检查物品是否可损坏（即 meta 是否为 Damageable 的实例）
                if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                    damageable.setMaxDamage(maxDamage);
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}