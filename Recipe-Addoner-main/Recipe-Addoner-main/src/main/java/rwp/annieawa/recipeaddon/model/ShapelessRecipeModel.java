package rwp.annieawa.recipeaddon.model;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import rwp.annieawa.recipeaddon.RecipeAddon;

import java.util.List;

public class ShapelessRecipeModel implements CustomRecipe {
    private final String key;
    private final List<ItemStack> ingredients;
    private final ItemStack result;

    public ShapelessRecipeModel(String key, List<ItemStack> ingredients, ItemStack result) {
        this.key = key;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public Recipe toBukkitRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(RecipeAddon.getInstance(), key);
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, result);

        // 添加原料（同样使用 ExactChoice）
        for (ItemStack ingredient : ingredients) {
            recipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
        }

        return recipe;
    }

    @Override
    public String getKey() {
        return key;
    }
}