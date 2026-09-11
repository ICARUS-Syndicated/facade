package rwp.annieawa.recipeaddon.model;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import rwp.annieawa.recipeaddon.RecipeAddon;

import java.util.List;
import java.util.Map;

/**
 * 有序配方模型
 */
public class ShapedRecipeModel implements CustomRecipe {
    public static class IngredientInfo {
        public ItemStack itemStack;
        public boolean exactMatch; // true 表示精确匹配，false 表示材料匹配

        public IngredientInfo(ItemStack itemStack, boolean exactMatch) {
            this.itemStack = itemStack;
            this.exactMatch = exactMatch;
        }
    }
    private final String key;
    private final List<String> pattern;
    private final Map<Character, IngredientInfo> ingredients; // 修改
    private final ItemStack result;

    public ShapedRecipeModel(String key, List<String> pattern,
                             Map<Character, IngredientInfo> ingredients,
                             ItemStack result) {
        this.key = key;
        this.pattern = pattern;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public Recipe toBukkitRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(RecipeAddon.getInstance(), key);
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
        recipe.shape(pattern.toArray(new String[0]));

        for (Map.Entry<Character, IngredientInfo> entry : ingredients.entrySet()) {
            char symbol = entry.getKey();
            IngredientInfo info = entry.getValue();
            RecipeChoice choice;
            if (info.exactMatch) {
                choice = new RecipeChoice.ExactChoice(info.itemStack);
            } else {
                choice = new RecipeChoice.MaterialChoice(info.itemStack.getType());
            }
            recipe.setIngredient(symbol, choice);
        }

        return recipe;
    }


    @Override
    public String getKey() {
        return key;
    }
}