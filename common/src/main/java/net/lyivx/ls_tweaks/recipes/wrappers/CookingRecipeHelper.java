package net.lyivx.ls_tweaks.recipes.wrappers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

/**
 * Helper class to extract input items from cooking recipes reliably
 */
public class CookingRecipeHelper {
    
    public static ItemStack[] getInputItems(AbstractCookingRecipe recipe) {
        if (recipe == null) {
            return new ItemStack[0];
        }
        
        try {
            var holders = recipe.input().items().toList();
            ItemStack[] items = new ItemStack[holders.size()];
            for (int i = 0; i < holders.size(); i++) items[i] = new ItemStack(holders.get(i).value());
            return items;
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] ERROR extracting cooking input: " + e.getMessage());
            return new ItemStack[0];
        }
    }
    
    public static ItemStack getFirstInputItem(AbstractCookingRecipe recipe) {
        ItemStack[] items = getInputItems(recipe);
        return items.length > 0 ? items[0] : ItemStack.EMPTY;
    }
    
    public static int getCookingTime(AbstractCookingRecipe recipe) {
        if (recipe == null) {
            return 0;
        }
        
        try {
            return recipe.cookingTime();
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] ERROR extracting cooking time: " + e.getMessage());
            return 0;
        }
    }
    
    public static float getExperience(AbstractCookingRecipe recipe) {
        if (recipe == null) {
            return 0.0f;
        }
        
        try {
            return recipe.experience();
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] ERROR extracting experience: " + e.getMessage());
            return 0.0f;
        }
    }
}
