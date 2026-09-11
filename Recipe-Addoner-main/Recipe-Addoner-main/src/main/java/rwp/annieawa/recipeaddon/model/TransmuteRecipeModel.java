package rwp.annieawa.recipeaddon.model;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.TransmuteRecipe;
import rwp.annieawa.recipeaddon.RecipeAddon;

/**
 * 转化配方模型
 * 适用于潜影盒染色等需要保留输入物品组件的配方
 */
public class TransmuteRecipeModel implements CustomRecipe {
    private final String key;
    private final ItemStack input;      // 被转化的主物品（保留组件）
    private final ItemStack material;    // 消耗的辅助材料
    private final ItemStack result;      // 结果物品（仅类型会被使用，组件来自input）

    public TransmuteRecipeModel(String key, ItemStack input, ItemStack material, ItemStack result) {
        this.key = key;
        this.input = input;
        this.material = material;
        this.result = result;
    }

    @Override
    public Recipe toBukkitRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(RecipeAddon.getInstance(), key);

        // 创建转化配方
        // 注意：result 的实际物品组件会从 input 复制，这里只需要指定结果类型
        TransmuteRecipe recipe = new TransmuteRecipe(
                namespacedKey,
                result.getType(),                    // 结果物品类型
                new RecipeChoice.MaterialChoice(input.getType()),     // 输入主材料（精确匹配组件）
                new RecipeChoice.ExactChoice(material)   // 辅助材料（精确匹配组件）
        );

        return recipe;
    }

    @Override
    public String getKey() {
        return key;
    }
}