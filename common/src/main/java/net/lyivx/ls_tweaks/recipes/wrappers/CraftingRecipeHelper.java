package net.lyivx.ls_tweaks.recipes.wrappers;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to extract ingredient items from crafting recipes reliably
 */
public class CraftingRecipeHelper {

    public static NonNullList<Ingredient> getIngredients(CraftingRecipe recipe) {
        if (recipe == null) {
            return NonNullList.create();
        }
        
        try {
            // Use the ingredients() method directly on PlacementInfo
            List<Ingredient> ingredientsList = recipe.placementInfo().ingredients();

            if (ingredientsList == null) {
                return NonNullList.create();
            }
            
            // Convert List to NonNullList
            NonNullList<Ingredient> ingredients = NonNullList.create();
            ingredients.addAll(ingredientsList);
            
            return ingredients;
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] ERROR extracting crafting ingredients: " + e.getMessage());
            return NonNullList.create();
        }
    }
    
    public static ItemStack[] getIngredientItems(CraftingRecipe recipe, int index) {
        NonNullList<Ingredient> ingredients = getIngredients(recipe);
        if (index >= 0 && index < ingredients.size()) {
            return IngredientHelper.getItems(ingredients.get(index));
        }
        return new ItemStack[0];
    }
    
    public static ItemStack getFirstIngredientItem(CraftingRecipe recipe, int index) {
        ItemStack[] items = getIngredientItems(recipe, index);
        return items.length > 0 ? items[0] : ItemStack.EMPTY;
    }
    
    /**
     * Gets the ingredient items for a shaped recipe respecting the actual pattern layout.
     * Returns a 3x3 grid of ItemStacks where empty slots are represented by ItemStack.EMPTY.
     */
    public static ItemStack[][] getShapedPatternItems(CraftingRecipe recipe) {
        ItemStack[][] pattern = new ItemStack[3][3];
        
        // Initialize with empty stacks
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pattern[i][j] = ItemStack.EMPTY;
            }
        }
        
        if (recipe == null) {
            return pattern;
        }
        
        try {

            int getWidth = CraftingInput.EMPTY.width();
            // Use REI approach: get width, height, and ingredients
            Method getWidthMethod = recipe.getClass().getMethod("getWidth");
            Method getHeightMethod = recipe.getClass().getMethod("getHeight");
            Method getIngredientsMethod = recipe.getClass().getMethod("getIngredients");

            int recipeWidth = (Integer) getWidthMethod.invoke(recipe);
            int recipeHeight = (Integer) getHeightMethod.invoke(recipe);
            List<?> ingredientsList = (List<?>) getIngredientsMethod.invoke(recipe);
            
            System.out.println("[LS Tweaks][RV] DEBUG: Recipe dimensions: " + recipeWidth + "x" + recipeHeight);
            System.out.println("[LS Tweaks][RV] DEBUG: Ingredients count: " + ingredientsList.size());
            
            // Map ingredients to 3x3 grid using REI's approach
            int ingredientIndex = 0;
            for (int y = 0; y < recipeHeight; y++) {
                for (int x = 0; x < recipeWidth; x++) {
                    if (ingredientIndex < ingredientsList.size()) {
                        Object ingredientObj = ingredientsList.get(ingredientIndex);
                        Ingredient ingredient = null;
                        
                        // Handle Optional<Ingredient> case
                        if (ingredientObj instanceof Optional<?> optional) {
                            if (optional.isPresent()) {
                                Object present = optional.get();
                                if (present instanceof Ingredient) {
                                    ingredient = (Ingredient) present;
                                }
                            }
                        } else if (ingredientObj instanceof Ingredient) {
                            ingredient = (Ingredient) ingredientObj;
                        }
                        
                        if (ingredient != null) {
                            ItemStack cyclingItem = IngredientHelper.getCyclingItem(ingredient, ingredientIndex);
                            if (!cyclingItem.isEmpty()) {
                                // Map to 3x3 grid, centering the recipe
                                int gridX = x + (3 - recipeWidth) / 2;
                                int gridY = y + (3 - recipeHeight) / 2;
                                if (gridX >= 0 && gridX < 3 && gridY >= 0 && gridY < 3) {
                                    pattern[gridY][gridX] = cyclingItem;
                                }
                            }
                        }
                        ingredientIndex++;
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] ERROR extracting shaped pattern: " + e.getMessage());
            // Fallback to sequential filling
            return getSequentialPattern(recipe);
        }
        
        return pattern;
    }
    
    /**
     * Fallback method that fills the pattern sequentially when pattern extraction fails
     */
    private static ItemStack[][] getSequentialPattern(CraftingRecipe recipe) {
        ItemStack[][] pattern = new ItemStack[3][3];
        
        // Initialize with empty stacks
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pattern[i][j] = ItemStack.EMPTY;
            }
        }
        
        // Fill sequentially with available ingredients
        NonNullList<Ingredient> ingredients = getIngredients(recipe);
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (index < ingredients.size()) {
                    pattern[i][j] = IngredientHelper.getFirstItem(ingredients.get(index));
                    index++;
                }
            }
        }
        
        return pattern;
    }
}
