package net.lyivx.ls_tweaks.common.feature.sort;

import net.lyivx.ls_tweaks.common.config.QolConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class SortingWhitelist {
    public static boolean isAllowed(AbstractContainerMenu menu, Screen screen) {
        // menu id whitelist
        if (menu != null) {
            ResourceLocation id = BuiltInRegistries.MENU.getKey(menu.getType());
            if (id != null && QolConfig.sorting().allowedMenuTypeIds.contains(id.toString())) return true;
        }
        // OR screen class whitelist
        if (screen != null) {
            String cn = screen.getClass().getName();
            if (QolConfig.sorting().allowedScreenClasses.contains(cn)) return true;
        }
        return false;
    }
    private SortingWhitelist() {}
}