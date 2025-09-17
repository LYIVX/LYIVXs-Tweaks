package net.lyivx.ls_tweaks.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.lyivx.ls_tweaks.client.LYIVXs_tweaksClient;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.sort.client.SortingButtons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class LYIVXs_tweaksFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LYIVXs_tweaksClient.init();

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!ConfigProvider.sortingEnabled()) return;
            if (screen instanceof AbstractContainerScreen<?> || isPlayerInventory(screen)) {
                var btns = SortingButtons.createSortButtons(screen);
                var list = Screens.getButtons(screen);
                for (var b : btns) list.add(b);
            }
        });
    }

    private static boolean isPlayerInventory(Screen s) {
        return s instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen
                || s instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
    }
}
