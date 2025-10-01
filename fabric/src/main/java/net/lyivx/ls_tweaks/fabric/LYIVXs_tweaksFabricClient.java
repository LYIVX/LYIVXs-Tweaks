package net.lyivx.ls_tweaks.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.lyivx.ls_tweaks.client.LYIVXs_tweaksClient;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.quickstack.client.QuickStackButtons;
import net.lyivx.ls_tweaks.common.feature.sort.client.SortingButtons;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient;
import net.lyivx.ls_tweaks.common.feature.slotlock.client.SlotLockButtons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.lyivx.ls_tweaks.client.overlay.OverlayHooks;

public final class LYIVXs_tweaksFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LYIVXs_tweaksClient.init();

        createQuickButtons();
        createSortButtons();
        createSlotLockOverlay();
        createSlotLockButtons();
        createPlacementGuards();
    }

    private static void createSortButtons() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!ConfigProvider.sortingEnabled() && !ConfigProvider.uiShowSortingButtons()) return;
            if (screen instanceof AbstractContainerScreen<?> || isPlayerInventory(screen)) {
                var list = Screens.getButtons(screen);
                for (var b : SortingButtons.createSortButtons(screen)) list.add(b);
            }
        });
    }

    private static void createQuickButtons() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!ConfigProvider.quickStackEnabled() && ConfigProvider.uiShowQuickStackButtons()) return;
            if (screen instanceof AbstractContainerScreen<?> || isPlayerInventory(screen)) {
                var list = Screens.getButtons(screen);
                for (var b : QuickStackButtons.create(screen)) list.add(b);
            }
        });
    }

    private static void createSlotLockOverlay() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            ScreenEvents.afterRender(screen).register((scr, graphics, mouseX, mouseY, delta) -> {
                if (scr instanceof AbstractContainerScreen<?> cs) {
                    SlotLockClient.drawOverlay(cs, graphics);
                    OverlayHooks.render(cs, graphics);
                }
            });
            if (screen instanceof AbstractContainerScreen<?> cs) {
                ScreenMouseEvents.allowMouseClick(screen).register((scr, mouseX, mouseY, button) -> !OverlayHooks.mouseClick(cs, mouseX, mouseY, button));
                // Also run after click to ensure opening screens even if allow handler didn't fire
                ScreenMouseEvents.afterMouseClick(screen).register((scr, mouseX, mouseY, button) -> {
                    if (scr instanceof AbstractContainerScreen<?> c2) {
                        OverlayHooks.mouseClick(c2, mouseX, mouseY, button);
                    }
                });
            }
        });
    }

    private static void createPlacementGuards() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof AbstractContainerScreen<?> cs) {
                ScreenMouseEvents.allowMouseClick(screen).register((scr, mouseX, mouseY, button) -> !SlotLockClient.shouldBlockPlacement(cs, mouseX, mouseY, button));
                ScreenMouseEvents.allowMouseRelease(screen).register((scr, mouseX, mouseY, button) -> !SlotLockClient.shouldBlockPlacement(cs, mouseX, mouseY, button));
                ScreenKeyboardEvents.allowKeyPress(screen).register((scr, key, scancode, modifiers) -> {
                    // Number keys 1..9 map to hotbar 0..8 in vanilla
                    if (key >= org.lwjgl.glfw.GLFW.GLFW_KEY_1 && key <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
                        int hotbar = key - org.lwjgl.glfw.GLFW.GLFW_KEY_1; // 0..8
                        var mc = Minecraft.getInstance();
                        double mx = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getWidth();
                        double my = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight();
                        return !SlotLockClient.shouldBlockHotbarSwap(cs, mx, my, hotbar);
                    }
                    
                    // Handle special keys (backspace, escape) for Fabric
                    if (key == 259 || key == 256) { // Backspace or Escape
                        if (OverlayHooks.keyPressed(cs, key, scancode, modifiers)) {
                            return false; // Block the event
                        }
                    }
                    
                    return true;
                });
                
                // Character input is now handled by ScreenMixin for both platforms
            }
        });
    }

    private static void createSlotLockButtons() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!ConfigProvider.slotLockEnabled()) return;
            if (screen instanceof AbstractContainerScreen<?> || isPlayerInventory(screen)) {
                var list = Screens.getButtons(screen);
                for (var b : SlotLockButtons.create(screen)) list.add(b);
            }
        });
    }

    private static boolean isPlayerInventory(Screen s) {
        return s instanceof InventoryScreen
                || s instanceof CreativeModeInventoryScreen;
    }
}
