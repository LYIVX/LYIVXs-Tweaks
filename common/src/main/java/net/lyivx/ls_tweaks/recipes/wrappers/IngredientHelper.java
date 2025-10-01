package net.lyivx.ls_tweaks.recipes.wrappers;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Helper class to extract items from ingredients reliably
 */
public class IngredientHelper {
    
    public static ItemStack[] getItems(Ingredient ingredient) {
        if (ingredient == null) {
            return new ItemStack[0];
        }
        
        try {
            // Use the items() method which returns Stream<Holder<Item>>
            Stream<Holder<Item>> items = ingredient.items();
            if (items != null) {
                return items
                    .map(holder -> new ItemStack(holder.value()))
                    .toArray(ItemStack[]::new);
            }
        } catch (Exception e) {
            System.out.println("[LS Tweaks][RV] WARN: Could not extract items from ingredient: " + e.getMessage());
        }
        
        return new ItemStack[0];
    }
    
    public static ItemStack getFirstItem(Ingredient ingredient) {
        ItemStack[] items = getItems(ingredient);
        return items.length > 0 ? items[0] : ItemStack.EMPTY;
    }

    public static ItemStack getCyclingItem(Ingredient ingredient, int salt) {
        ItemStack[] items = getItems(ingredient);
        if (items.length == 0) return ItemStack.EMPTY;

        var mc = net.minecraft.client.Minecraft.getInstance();
        long t = 0L;
        if (mc != null && mc.level != null) t = mc.level.getGameTime(); // 20 ticks/sec

        // change once per second
        int idx = (int) (((t / 20L) + salt) % items.length);
        return items[idx];
    }
}
