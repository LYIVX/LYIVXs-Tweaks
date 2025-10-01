package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Simple hoverable tooltip widget over a rectangular area, relative to recipe content origin. */
public class HoverTooltipWidget {
    public final int x, y, width, height;
    public final Component tooltip;

    public HoverTooltipWidget(int x, int y, int width, int height, Component tooltip) {
        this.x = x; this.y = y; this.width = width; this.height = height; this.tooltip = tooltip;
    }

    public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
        int left = baseX + x;
        int top = baseY + y;
        if (mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height) {
            g.renderComponentTooltip(font, java.util.List.of(tooltip), mouseX, mouseY);
        }
    }
}


