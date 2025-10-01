package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for easy widget creation
 */
public class Widgets {
    
    public static class AddSlot {
        public static SlotBuilder input(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.INPUT, true);
        }
        
        public static SlotBuilder output(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.OUTPUT, true);
        }
        
        public static SlotBuilder ghost(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.GHOST, true);
        }
        
        public static SlotBuilder catalyst(int x, int y, ItemStack stack, int index) {
            return new SlotBuilder(x, y, stack, index, SlotRole.CATALYST, true);
        }
        
        public static SlotBuilder fuel(int x, int y, int index) {
            return new SlotBuilder(x, y, SlotWidget.getDefaultFuelItem(null), index, SlotRole.CATALYST, true, true); // true = isFuel
        }
        
        // Builder class for fluent API that extends SlotWidget
        public static class SlotBuilder extends SlotWidget {
            private boolean noBackground = false;
            private ResourceLocation customBackground = null;
            private final SlotRole role;
            private final boolean isFuel;
            
            public SlotBuilder(int x, int y, ItemStack stack, int index, SlotRole role, boolean drawBackground) {
                super(role, x, y, drawBackground, role == SlotRole.OUTPUT, stack, index, null, false, false);
                this.role = role;
                this.isFuel = false;
            }
            
            public SlotBuilder(int x, int y, ItemStack stack, int index, SlotRole role, boolean drawBackground, boolean isFuel) {
                super(role, x, y, drawBackground, role == SlotRole.OUTPUT, stack, index, null, false, false);
                this.role = role;
                this.isFuel = isFuel;
            }
            
            public SlotBuilder noBackground() {
                this.noBackground = true;
                return this;
            }
            
            public SlotBuilder background(ResourceLocation background) {
                this.customBackground = background;
                return this;
            }
            
            // Override render to use the builder's settings
            @Override
            public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
                SlotWidget widget;
                if (noBackground) {
                    widget = new SlotWidget(role, x, y, false, role == SlotRole.OUTPUT, stack, recipeJsonIndex, null, false, true);
                } else if (customBackground != null) {
                    widget = new SlotWidget(role, x, y, true, role == SlotRole.OUTPUT, stack, recipeJsonIndex, customBackground, true, false);
                } else {
                    // Use default slot creation based on role with proper textures
                    switch (role) {
                        case INPUT:
                            widget = SlotWidget.input(x, y, stack, recipeJsonIndex);
                            break;
                        case OUTPUT:
                            widget = SlotWidget.output(x, y, stack, recipeJsonIndex);
                            break;
                        case GHOST:
                            widget = SlotWidget.ghost(x, y, stack, recipeJsonIndex);
                            break;
                        case CATALYST:
                            // For catalyst slots, use the appropriate texture
                            if (isFuel) {
                                widget = SlotWidget.fuel(x, y, recipeJsonIndex);
                            } else {
                                widget = SlotWidget.catalyst(x, y, stack, recipeJsonIndex);
                            }
                            break;
                        default:
                            widget = new SlotWidget(role, x, y, true, role == SlotRole.OUTPUT, stack, recipeJsonIndex, null, false, false);
                    }
                }
                widget.render(g, font, baseX, baseY, mouseX, mouseY);
            }
        }
    }
    
    public static class AddArrow {
        public static class Right {
            // Progress (default dynamic behavior like previous animate): start empty, default timings
            public static TextureWidget progress(int x, int y) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT); }
            // Progress with start override
            public static TextureWidget progress(int x, int y, boolean startFilled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, startFilled, 60, 10); }
            // Animate with customization (speed/pause)
            public static TextureWidget animate(int x, int y, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, false, rampTicks, pauseTicks); }
            // Animate with customization
            public static TextureWidget animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, startFilled, rampTicks, pauseTicks); }
            // Static filled/empty
            public static TextureWidget state(int x, int y, boolean filled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.RIGHT, filled); }
        }
        
        public static class Left {
            public static TextureWidget progress(int x, int y) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT); }
            public static TextureWidget progress(int x, int y, boolean startFilled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, startFilled, 60, 10); }
            public static TextureWidget animate(int x, int y, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, false, rampTicks, pauseTicks); }
            public static TextureWidget animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, startFilled, rampTicks, pauseTicks); }
            public static TextureWidget state(int x, int y, boolean filled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.LEFT, filled); }
        }
        
        public static class Up {
            public static TextureWidget progress(int x, int y) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP); }
            public static TextureWidget progress(int x, int y, boolean startFilled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, startFilled, 60, 10); }
            public static TextureWidget animate(int x, int y, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, false, rampTicks, pauseTicks); }
            public static TextureWidget animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, startFilled, rampTicks, pauseTicks); }
            public static TextureWidget state(int x, int y, boolean filled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.UP, filled); }
        }
        
        public static class Down {
            public static TextureWidget progress(int x, int y) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN); }
            public static TextureWidget progress(int x, int y, boolean startFilled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, startFilled, 60, 10); }
            public static TextureWidget animate(int x, int y, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, false, rampTicks, pauseTicks); }
            public static TextureWidget animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, startFilled, rampTicks, pauseTicks); }
            public static TextureWidget state(int x, int y, boolean filled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_ARROW, TextureWidget.Direction.DOWN, filled); }
        }
    }
    
    public static class AddFlame {
        public static TextureWidget progress(int x, int y) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP); }
        public static TextureWidget progress(int x, int y, boolean startFilled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, startFilled, 60, 10); }
        public static TextureWidget animate(int x, int y, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, true, rampTicks, pauseTicks); }
        public static TextureWidget animate(int x, int y, boolean startFilled, int rampTicks, int pauseTicks) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, startFilled, rampTicks, pauseTicks); }
        public static TextureWidget state(int x, int y, boolean filled) { return new TextureWidget(x, y, TextureWidget.Type.ANIMATED_FLAME, TextureWidget.Direction.UP, filled); }
    }
    
    public static class AddTexture {
        public static TextureWidget staticTexture(int x, int y, ResourceLocation texture, int u, int v, int width, int height, int texWidth, int texHeight) {
            return new TextureWidget(x, y, texture, u, v, width, height, texWidth, texHeight);
        }
        // Custom animated texture pair - dynamic
        public static TextureWidget animate(int x, int y,
                                            ResourceLocation background, ResourceLocation fill,
                                            int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                            TextureWidget.Direction direction,
                                            boolean startFilled, int rampTicks, int pauseTicks) {
            return new TextureWidget(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, startFilled, rampTicks, pauseTicks);
        }
        // Custom progress (default dynamic behavior, default timings)
        public static TextureWidget progress(int x, int y,
                                             ResourceLocation background, ResourceLocation fill,
                                             int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                             TextureWidget.Direction direction) {
            return new TextureWidget(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, false, 60, 10);
        }
        // Custom static state
        public static TextureWidget state(int x, int y,
                                          ResourceLocation background, ResourceLocation fill,
                                          int bgWidth, int bgHeight, int fillWidth, int fillHeight,
                                          TextureWidget.Direction direction,
                                          boolean filled) {
            return new TextureWidget(x, y, background, fill, bgWidth, bgHeight, fillWidth, fillHeight, direction, filled);
        }
    }
    
    public static class AddText {
        public static TextBuilder translatable(int x, int y, String key) {
            return new TextBuilder(x, y, Component.translatable(key), null);
        }
        
        public static TextBuilder translatable(int x, int y, String key, TextColor color) {
            return new TextBuilder(x, y, Component.translatable(key), color);
        }
        
        public static TextBuilder translatable(int x, int y, String key, ChatFormatting formatting) {
            return new TextBuilder(x, y, Component.translatable(key).withStyle(formatting), null);
        }
        
        public static TextBuilder literal(int x, int y, String text) {
            return new TextBuilder(x, y, Component.literal(text), null);
        }
        
        public static TextBuilder literal(int x, int y, String text, TextColor color) {
            return new TextBuilder(x, y, Component.literal(text), color);
        }
        
        public static TextBuilder literal(int x, int y, String text, ChatFormatting formatting) {
            return new TextBuilder(x, y, Component.literal(text).withStyle(formatting), null);
        }
        
        // Builder class for fluent API that extends TextWidget
        public static class TextBuilder extends TextWidget {
            private boolean centered = false;
            private int width = 0;
            private int height = 0;
            private boolean autoScale = false;
            private Component text;
            private TextColor color;
            
            public TextBuilder(int x, int y, Component text, TextColor color) {
                super(x, y, text, color, false);
                this.text = text;
                this.color = color;
            }
            
            public TextBuilder centered() {
                this.centered = true;
                return this;
            }
            
            public TextBuilder bounds(int width, int height) {
                this.width = width;
                this.height = height;
                this.autoScale = true;
                return this;
            }
            
            public TextBuilder color(ChatFormatting formatting) {
                this.color = TextColor.fromRgb(formatting.getColor());
                return this;
            }
            
            public TextBuilder color(String colorName) {
                ChatFormatting formatting = getColorFromName(colorName);
                if (formatting != null) {
                    this.color = TextColor.fromRgb(formatting.getColor());
                }
                return this;
            }
            
            private ChatFormatting getColorFromName(String colorName) {
                if (colorName == null) return null;
                
                String name = colorName.toLowerCase().trim();
                switch (name) {
                    case "black": return ChatFormatting.BLACK;
                    case "dark_blue": case "darkblue": return ChatFormatting.DARK_BLUE;
                    case "dark_green": case "darkgreen": return ChatFormatting.DARK_GREEN;
                    case "dark_aqua": case "darkaqua": return ChatFormatting.DARK_AQUA;
                    case "dark_red": case "darkred": return ChatFormatting.DARK_RED;
                    case "dark_purple": case "darkpurple": return ChatFormatting.DARK_PURPLE;
                    case "gold": return ChatFormatting.GOLD;
                    case "gray": case "grey": return ChatFormatting.GRAY;
                    case "dark_gray": case "darkgray": case "dark_grey": case "darkgrey": return ChatFormatting.DARK_GRAY;
                    case "blue": return ChatFormatting.BLUE;
                    case "green": case "lime": return ChatFormatting.GREEN;
                    case "aqua": case "cyan": return ChatFormatting.AQUA;
                    case "red": return ChatFormatting.RED;
                    case "light_purple": case "lightpurple": case "magenta": return ChatFormatting.LIGHT_PURPLE;
                    case "yellow": return ChatFormatting.YELLOW;
                    case "white": return ChatFormatting.WHITE;
                    default: return null;
                }
            }
            
            // Override render to use the builder's settings
            @Override
            public void render(GuiGraphics g, Font font, int baseX, int baseY) {
                TextWidget widget;
                if (autoScale) {
                    widget = new TextWidget(x, y, width, height, this.text, this.color, centered);
                } else {
                    widget = new TextWidget(x, y, this.text, this.color, centered);
                }
                widget.render(g, font, baseX, baseY);
            }
        }
    }

    /** Hoverable tooltip widgets */
    public static class AddHover {
        public static HoverTooltipWidget translatable(int x, int y, int width, int height, String key) {
            return new HoverTooltipWidget(x, y, width, height, Component.translatable(key));
        }
        public static HoverTooltipWidget literal(int x, int y, int width, int height, String text) {
            return new HoverTooltipWidget(x, y, width, height, Component.literal(text));
        }
    }
}
