package net.lyivx.ls_tweaks.recipes.wrappers;

import net.lyivx.ls_furniture.common.recipes.WorkstationRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

/**
 * Helper class to extract input items from cooking recipes reliably
 */
public class WorkstationRecipeHelper {
    
    public static ItemStack[] getInputItems(WorkstationRecipe recipe) {
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
    
    public static ItemStack getFirstInputItem(WorkstationRecipe recipe) {
        ItemStack[] items = getInputItems(recipe);
        return items.length > 0 ? items[0] : ItemStack.EMPTY;
    }
}
