package net.lyivx.ls_tweaks.client.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** Central hook methods that loaders call to render and handle the item panel overlay. */
public final class OverlayHooks {
    public static void render(AbstractContainerScreen<?> cs, GuiGraphics g) {
        ItemPanelOverlay.render(cs, g);
    }
    public static boolean mouseClick(AbstractContainerScreen<?> cs, double x, double y, int button) {
        boolean handled = ItemPanelOverlay.mouseClicked(cs, x, y, button);
        System.out.println("[LS Tweaks][RV] mouseClick handled=" + handled + " btn=" + button + " @" + x + "," + y);
        return handled;
    }
    
    public static boolean keyPressed(AbstractContainerScreen<?> cs, int keyCode, int scanCode, int modifiers) {
        return ItemPanelOverlay.keyPressed(cs, keyCode, scanCode, modifiers);
    }
    
    public static boolean charTyped(AbstractContainerScreen<?> cs, char codePoint, int modifiers) {
        return ItemPanelOverlay.charTyped(cs, codePoint, modifiers);
    }
    
    private OverlayHooks() {}
}


