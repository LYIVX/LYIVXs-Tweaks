package net.lyivx.ls_tweaks.common.feature.sort;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SortingWhitelist {
    public static boolean isAllowed(AbstractContainerMenu menu, Screen screen) {
        if (menu != null) {
            ResourceLocation id = BuiltInRegistries.MENU.getKey(menu.getType());
            if (id != null && SortingWhitelistData.allowedMenuIds().contains(id.toString())) return true;
        }
        if (screen != null) {
            String cn = screen.getClass().getName();
            if (SortingWhitelistData.allowedScreenClasses().contains(cn)) return true;
        }
        return false;
    }

    public static SortingWhitelistData.Layout getLayout(AbstractContainerMenu menu, Screen screen) {
        if (menu != null) {
            ResourceLocation id = BuiltInRegistries.MENU.getKey(menu.getType());
            if (id != null) return SortingWhitelistData.layoutFor(id.toString());
        }
        if (screen != null) {
            String cn = screen.getClass().getName();
            return SortingWhitelistData.layoutFor(cn);
        }
        return SortingWhitelistData.Layout.HORIZONTAL;
    }

    private SortingWhitelist() {}
}