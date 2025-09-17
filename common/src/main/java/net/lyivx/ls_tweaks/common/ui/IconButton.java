package net.lyivx.ls_tweaks.common.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class IconButton extends AbstractButton {
    private final ResourceLocation icon;
    private final ResourceLocation iconHover;
    private final Consumer<IconButton> onPressCallback;

    public IconButton(int x, int y, int w, int h,
                      ResourceLocation icon, ResourceLocation iconHover,
                      Component tooltip, Consumer<IconButton> onPress) {
        super(x, y, w, h, Component.empty());
        this.icon = icon;
        this.iconHover = iconHover;
        this.onPressCallback = onPress;
        if (tooltip != null) setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress() {
        if (onPressCallback != null) onPressCallback.accept(this);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex = (isHovered() ? iconHover : icon);
        // Draw the 12x10 texture at 1:1 scale
        g.blit(RenderType::guiTextured, tex, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput n) {
        defaultButtonNarrationText(n);
    }
}