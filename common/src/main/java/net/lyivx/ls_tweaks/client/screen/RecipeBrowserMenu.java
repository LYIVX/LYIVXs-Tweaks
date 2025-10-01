package net.lyivx.ls_tweaks.client.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Simple container menu for the recipe browser screen
 */
public class RecipeBrowserMenu extends AbstractContainerMenu {
    public static final MenuType<RecipeBrowserMenu> TYPE = new MenuType<RecipeBrowserMenu>(RecipeBrowserMenu::new, null);
    
    public RecipeBrowserMenu(int containerId, Inventory playerInventory) {
        super(TYPE, containerId);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true; // Always valid for recipe browser
    }
}
