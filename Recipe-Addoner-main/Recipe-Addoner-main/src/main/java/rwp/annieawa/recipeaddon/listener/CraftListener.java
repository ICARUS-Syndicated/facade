package rwp.annieawa.recipeaddon.listener;
import io.papermc.paper.datacomponent.DataComponentTypes;

import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import rwp.annieawa.recipeaddon.RecipeAddon;
import rwp.annieawa.recipeaddon.config.SpecialRecipeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CraftListener implements Listener {

    private final RecipeAddon plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CraftListener(RecipeAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;
        if (!(recipe instanceof Keyed keyed)) return;
        NamespacedKey recipeKey = keyed.getKey();
        String fullKey = recipeKey.toString();

        // 优先处理特殊配方
        SpecialRecipeInfo specialInfo = plugin.getRecipeLoader().getSpecialRecipeInfo(fullKey);
        if (specialInfo != null) {
            handleSpecialCraft(event, specialInfo, fullKey);
            return; // 特殊配方处理完毕，不再执行后续
        }

        // 处理普通转化配方组件覆盖
        ConfigurationSection comps = plugin.getRecipeLoader().getResultComponentSection(fullKey);
        if (comps != null) {
            ItemStack originalResult = event.getInventory().getResult();
            if (originalResult != null && !originalResult.getType().isAir()) {
                ItemStack modified = applyComponents(originalResult.clone(), comps, fullKey);
                if (modified != null) {
                    event.getInventory().setResult(modified);
                }
            }
        }
    }

    // 统一处理特殊配方的逻辑
    private void handleSpecialCraft(PrepareItemCraftEvent event, SpecialRecipeInfo info, String fullKey) {

        // 选择源物品
        ItemStack[] matrix = event.getInventory().getMatrix();
        Recipe recipe = event.getRecipe();
        ItemStack sourceInput = findSourceItem(matrix, recipe, info);
        if (info.targetMaterial == null) {
            plugin.getLogger().warning("特殊配方 " + fullKey + " targetMaterial 为 null");
            return;
        }
        if (sourceInput == null) {
            plugin.getLogger().warning("特殊配方 " + fullKey + " 无法找到源物品");
            return;
        }


        int resultAmount = event.getRecipe().getResult().getAmount();
        ItemStack finalItem;
        if (info.copyInput) {
            finalItem = sourceInput.clone();
            finalItem.setType(info.targetMaterial);
            finalItem.setAmount(resultAmount);
        } else {
            finalItem = new ItemStack(info.targetMaterial, resultAmount);
        }

        if (info.components != null) {
            finalItem = applyComponents(finalItem, info.components, fullKey);
        }

        event.getInventory().setResult(finalItem);
    }

    private ItemStack findSourceItem(ItemStack[] matrix, Recipe recipe, SpecialRecipeInfo info) {


        // 1. 如果指定了 source-character 且配方为有序配方
        if (info.sourceCharacter != null) {
            plugin.getLogger().info("尝试使用 source-character: '" + info.sourceCharacter + "'");
            if (recipe instanceof ShapedRecipe shaped) {
                List<String> pattern = List.of(shaped.getShape());
                if (pattern == null || pattern.isEmpty()) {
                    plugin.getLogger().warning("有序配方 pattern 为空");
                    return null;
                }

                // 将 pattern 转换为 3x3 网格（补齐空格）
                char[][] grid = new char[3][3];
                for (int row = 0; row < 3; row++) {
                    String rowStr = (row < pattern.size()) ? pattern.get(row) : "";
                    for (int col = 0; col < 3; col++) {
                        if (col < rowStr.length()) {
                            grid[row][col] = rowStr.charAt(col);
                        } else {
                            grid[row][col] = ' ';
                        }
                    }
                }

                // 查找 source-character 的位置
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        if (grid[row][col] == info.sourceCharacter) {
                            int slot = row * 3 + col;
                            if (slot < matrix.length) {
                                ItemStack item = matrix[slot];
                                if (item != null && !item.getType().isAir()) {
                                    return item.clone();
                                } else {
                                    return null;
                                }
                            } else {
                                return null;
                            }
                        }
                    }
                }
                plugin.getLogger().warning("未在 pattern 中找到字符 '" + info.sourceCharacter + "'");
                return null;
            } else {
                plugin.getLogger().warning("source-character 只能用于有序配方，当前配方类型: " + recipe.getClass().getSimpleName());
                // 可以回退到第一个非空物品，但这里选择返回 null 并让调用者处理
                return null;
            }
        }

        // 2. 如果指定了 source-slot
        if (info.sourceSlot != null) {
            int slot = info.sourceSlot;
            if (slot >= 0 && slot < matrix.length && matrix[slot] != null && !matrix[slot].getType().isAir()) {
                return matrix[slot].clone();
            }
        }
        // 3. 如果为转化配方
        if (recipe instanceof TransmuteRecipe){
            for (ItemStack item : matrix) {
                RecipeChoice rc = ((TransmuteRecipe) recipe).getInput();
                if (item != null && new RecipeChoice.MaterialChoice(item.getType()).equals(rc)) {
                    return item.clone();
                }
            }
        }
        // 4. 回退：取第一个非空物品
        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                return item.clone();
            }
        }

        return null;
    }
    // 为 CraftItemEvent 也重载一个处理
    private ItemStack applyComponents(ItemStack item, ConfigurationSection comps, String RecipeKey){
        if (item == null) return null;
        if (comps.contains("item-model")) {
            String modelStr = comps.getString("item-model");
            if (modelStr != null) {
                NamespacedKey modelKey = parseNamespacedKey(modelStr);
                if (modelKey != null) {
                    item.setData(DataComponentTypes.ITEM_MODEL, modelKey);
                }
            }
        }
        boolean hasDisplayName = comps.contains("display-name");
        if (!hasDisplayName) {
            String prefix = comps.getString("prefix");
            String suffix = comps.getString("suffix");
            if (prefix != null || suffix != null) {
                Component baseName = getItemDisplayName(item);
                if (prefix != null) {
                    Component prefixComp = miniMessage.deserialize(prefix);
                    baseName = prefixComp.append(baseName);
                }
                if (suffix != null) {
                    Component suffixComp = miniMessage.deserialize(suffix);
                    baseName = baseName.append(suffixComp);
                }
                item.setData(DataComponentTypes.CUSTOM_NAME, baseName);
            }
        }

        // 处理 display-name（优先级最高）
        if (comps.contains("display-name")) {
            String name = comps.getString("display-name");
            if (name != null) {
                Component displayName = miniMessage.deserialize(name);
                item.setData(DataComponentTypes.CUSTOM_NAME, displayName);
            }
        }
        // 2. maxdamage (最大耐久)
        if (comps.contains("maxdamage")) {
            int maxDamage = comps.getInt("maxdamage");
            // 尝试使用数据组件 API (如果存在)
            try {
                item.setData(DataComponentTypes.MAX_DAMAGE, maxDamage);
            } catch (NoSuchFieldError e) {
                // 回退到 ItemMeta 方式
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    damageable.setMaxDamage(maxDamage);
                    item.setItemMeta(meta);
                }
            }
        }


        // 4. lore (描述)
        if (comps.contains("lore")) {
            List<String> loreLines = comps.getStringList("lore");
            List<Component> loreComponents = loreLines.stream()
                    .map(line -> miniMessage.deserialize(line))
                    .collect(Collectors.toList());
            // 创建 ItemLore 对象
            ItemLore itemLore = ItemLore.lore(loreComponents);
            item.setData(DataComponentTypes.LORE, itemLore);
        }


        // 6. enchantments (附魔)
        if (comps.contains("enchantments")) {
            List<String> enchList = comps.getStringList("enchantments");
            Map<Enchantment, Integer> enchMap = new HashMap<>();
            for (String enchEntry : enchList) {
                String[] parts = enchEntry.split(":");
                if (parts.length == 2) {
                    Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(parts[0].toLowerCase()));
                    int level = Integer.parseInt(parts[1]);
                    if (ench != null) {
                        enchMap.put(ench, level);
                    }
                }
            }
            if (!enchMap.isEmpty()) {
                // 创建 ItemEnchantments 对象
                ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments(enchMap);
                item.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
            }}

        // 7. unbreakable (无法破坏)
        if (comps.getBoolean("unbreakable", false)) {
            item.setData(DataComponentTypes.UNBREAKABLE);
        }
        if (comps.getBoolean("equippable-on-head",true)){
            Equippable eq = Equippable.equippable(EquipmentSlot.HEAD).build();
            item.setData(DataComponentTypes.EQUIPPABLE,eq);
        }
        return item;
    }
    // 辅助方法：解析 NamespacedKey
    private NamespacedKey parseNamespacedKey(String input) {
        if (input.contains(":")) {
            return NamespacedKey.fromString(input);
        } else {
            return new NamespacedKey(plugin, input);
        }
    }
    private Component getItemDisplayName(ItemStack item) {
        Component customName = item.getData(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            return customName;
        }
        // 返回默认的本地化名称（例如 "item.minecraft.diamond_sword"）
        return Component.translatable(item.getType().getItemTranslationKey());
    }
}