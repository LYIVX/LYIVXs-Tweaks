package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

/**
 * Text widget for displaying text in recipe displays
 */
public class TextWidget {
    public final int x, y;
    public final Component text;
    public final TextColor color;
    public final boolean centered;
    public final int width, height; // bounds for auto-scaling
    public final boolean autoScale;
    
    public TextWidget(int x, int y, Component text, TextColor color, boolean centered) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.centered = centered;
        this.width = 0;
        this.height = 0;
        this.autoScale = false;
    }
    
    public TextWidget(int x, int y, int width, int height, Component text, TextColor color, boolean centered) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.color = color;
        this.centered = centered;
        this.autoScale = true;
    }
    
    // Factory methods for common use cases
    public static TextWidget translatable(int x, int y, String key) {
        return new TextWidget(x, y, Component.translatable(key), null, false);
    }
    
    public static TextWidget translatable(int x, int y, String key, TextColor color) {
        return new TextWidget(x, y, Component.translatable(key), color, false);
    }
    
    public static TextWidget translatable(int x, int y, String key, ChatFormatting formatting) {
        return new TextWidget(x, y, Component.translatable(key).withStyle(formatting), null, false);
    }
    
    public static TextWidget literal(int x, int y, String text) {
        return new TextWidget(x, y, Component.literal(text), null, false);
    }
    
    public static TextWidget literal(int x, int y, String text, TextColor color) {
        return new TextWidget(x, y, Component.literal(text), color, false);
    }
    
    public static TextWidget literal(int x, int y, String text, ChatFormatting formatting) {
        return new TextWidget(x, y, Component.literal(text).withStyle(formatting), null, false);
    }
    
    public static TextWidget centered(int x, int y, String text) {
        return new TextWidget(x, y, Component.literal(text), null, true);
    }
    
    public static TextWidget centered(int x, int y, String text, TextColor color) {
        return new TextWidget(x, y, Component.literal(text), color, true);
    }
    
    public static TextWidget centered(int x, int y, String text, ChatFormatting formatting) {
        return new TextWidget(x, y, Component.literal(text).withStyle(formatting), null, true);
    }
    
    // Bounded text widgets with auto-scaling
    public static TextWidget bounded(int x, int y, int width, int height, String text) {
        return new TextWidget(x, y, width, height, Component.literal(text), null, false);
    }
    
    public static TextWidget bounded(int x, int y, int width, int height, String text, TextColor color) {
        return new TextWidget(x, y, width, height, Component.literal(text), color, false);
    }
    
    public static TextWidget bounded(int x, int y, int width, int height, String text, ChatFormatting formatting) {
        return new TextWidget(x, y, width, height, Component.literal(text).withStyle(formatting), null, false);
    }
    
    public static TextWidget boundedCentered(int x, int y, int width, int height, String text) {
        return new TextWidget(x, y, width, height, Component.literal(text), null, true);
    }
    
    public static TextWidget boundedCentered(int x, int y, int width, int height, String text, TextColor color) {
        return new TextWidget(x, y, width, height, Component.literal(text), color, true);
    }
    
    public static TextWidget boundedCentered(int x, int y, int width, int height, String text, ChatFormatting formatting) {
        return new TextWidget(x, y, width, height, Component.literal(text).withStyle(formatting), null, true);
    }
    
    /**
     * Renders this text widget at the given position
     */
    public void render(GuiGraphics g, Font font, int baseX, int baseY) {
        int textX = baseX + this.x;
        int textY = baseY + this.y;
        
        Component text = this.text;
        if (this.color != null) {
            text = text.copy().withStyle(style -> style.withColor(this.color));
        }
        
        if (this.autoScale && this.width > 0 && this.height > 0) {
            // Auto-scale text to fit within bounds
            String textStr = text.getString();
            int textWidth = font.width(textStr);
            int textHeight = font.lineHeight;
            
            float scaleX = (float) this.width / textWidth;
            float scaleY = (float) this.height / textHeight;
            float scale = Math.min(scaleX, scaleY);
            
            // Calculate centered position within bounds
            int centerX = this.centered ? textX + this.width / 2 - 1 : textX;
            int centerY = textY;
            
            if (scale < 1.0f) {
                // Scale down the text
                g.pose().pushPose();
                g.pose().translate(centerX, centerY, 0);
                g.pose().scale(scale, scale, 1.0f);
                
                if (this.centered) {
                    g.drawCenteredString(font, text, 0, 0, 0xFFFFFF);
                } else {
                    g.drawString(font, text, 0, 0, 0xFFFFFF);
                }
                
                g.pose().popPose();
            } else {
                // Text fits, render normally
                if (this.centered) {
                    g.drawCenteredString(font, text, centerX, centerY, 0xFFFFFF);
                } else {
                    g.drawString(font, text, textX, textY, 0xFFFFFF);
                }
            }
        } else {
            // Normal rendering without scaling
            if (this.centered) {
                g.drawCenteredString(font, text, textX, textY, 0xFFFFFF);
            } else {
                g.drawString(font, text, textX, textY, 0xFFFFFF);
            }
        }
    }
}
