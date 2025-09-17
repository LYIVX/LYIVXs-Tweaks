package net.lyivx.ls_tweaks.common.feature.sort;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SortingWhitelist {
    public static boolean isAllowed(AbstractContainerMenu menu, Screen screen) {
        // menu id whitelist
        if (menu != null) {
            ResourceLocation id = BuiltInRegistries.MENU.getKey(menu.getType());
            if (id != null && SortingWhitelistData.allowedMenuIds().contains(id.toString())) return true;
        }
        // OR screen class whitelist
        if (screen != null) {
            String cn = screen.getClass().getName();
            if (SortingWhitelistData.allowedScreenClasses().contains(screen.getClass().getName())) return true;
        }
        return false;
    }
    private SortingWhitelist() {}
}