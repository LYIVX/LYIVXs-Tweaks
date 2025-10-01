package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Texture widget that combines static textures and animated textures
 */
public class TextureWidget {
    public enum Type {
        STATIC, ANIMATED_ARROW, ANIMATED_FLAME, ANIMATED_CUSTOM
    }
    
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    public final int x, y;
    public final Type type;
    public final Direction direction;
    private final float fillProgress; // 0.0 to 1.0 for animations when not dynamic
    public final ResourceLocation texture; // background texture
    public final int u, v, width, height, texWidth, texHeight;
    private final boolean dynamic; // recompute progress each frame
    // Animation customization
    private final int rampTicks;        // how long to go from empty->full (or full->empty if startFilled)
    private final int pauseTicks;       // pause duration at ends
    private final boolean startFilled;  // starting state for each cycle
    // Fill texture (front)
    private final ResourceLocation fillTextureRes;
    private final int fillTexWidth;
    private final int fillTexHeight;
    
    // Static texture constructor
    public TextureWidget(int x, int y, ResourceLocation texture, int u, int v, int width, int height, int texWidth, int texHeight) {
        this.x = x;
        this.y = y;
        this.type = Type.STATIC;
        this.direction = Direction.RIGHT;
        this.fillProgress = 1.0f;
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.dynamic = false;
        this.rampTicks = 0;
        this.pauseTicks = 0;
        this.startFilled = false;
        this.fillTextureRes = null;
        this.fillTexWidth = 0;
        this.fillTexHeight = 0;
    }
    
    // Animated texture constructor
    public TextureWidget(int x, int y, Type type, Direction direction, float fillProgress) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.fillProgress = Math.max(0.0f, Math.min(1.0f, fillProgress));
        this.texture = getTextureForType();
        this.u = 0;
        this.v = 0;
        this.width = getWidthForType();
        this.height = getHeightForType();
        this.texWidth = this.width;
        this.texHeight = this.height;
        this.dynamic = false;
        this.rampTicks = 0;
        this.pauseTicks = 0;
        // Defaults: arrows start empty, flame starts full
        this.startFilled = (type == Type.ANIMATED_FLAME);
        this.fillTextureRes = getFillTextureForType();
        this.fillTexWidth = getFillWidthForType();
        this.fillTexHeight = getFillHeightForType();
    }
    
    // Dynamic texture with defaults (uses time-based animation)
    public TextureWidget(int x, int y, Type type, Direction direction) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        // initial value not used when dynamic=true; computed each render
        this.fillProgress = 0.0f;
        this.texture = getTextureForType();
        this.u = 0;
        this.v = 0;
        this.width = getWidthForType();
        this.height = getHeightForType();
        this.texWidth = this.width;
        this.texHeight = this.height;
        this.dynamic = true;
        // Defaults
        this.rampTicks = 60; // ~3s
        this.pauseTicks = 10; // ~0.5s
        this.startFilled = (type == Type.ANIMATED_FLAME);
        this.fillTextureRes = getFillTextureForType();
        this.fillTexWidth = getFillWidthForType();
        this.fillTexHeight = getFillHeightForType();
    }

    // Dynamic texture with customization (speed/pause/start)
    public TextureWidget(int x, int y, Type type, Direction direction, boolean startFilled, int rampTicks, int pauseTicks) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.fillProgress = 0.0f;
        this.texture = getTextureForType();
        this.u = 0;
        this.v = 0;
        this.width = getWidthForType();
        this.height = getHeightForType();
        this.texWidth = this.width;
        this.texHeight = this.height;
        this.dynamic = true;
        this.rampTicks = Math.max(1, rampTicks);
        this.pauseTicks = Math.max(0, pauseTicks);
        this.startFilled = startFilled;
        this.fillTextureRes = getFillTextureForType();
        this.fillTexWidth = getFillWidthForType();
        this.fillTexHeight = getFillHeightForType();
    }

    // Static state (non-animated) with filled/empty toggle
    public TextureWidget(int x, int y, Type type, Direction direction, boolean filledState) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.direction = direction;
        this.fillProgress = filledState ? 1.0f : 0.0f;
        this.texture = getTextureForType();
        this.u = 0;
        this.v = 0;
        this.width = getWidthForType();
        this.height = getHeightForType();
        this.texWidth = this.width;
        this.texHeight = this.height;
        this.dynamic = false;
        this.rampTicks = 0;
        this.pauseTicks = 0;
        this.startFilled = filledState;
        this.fillTextureRes = getFillTextureForType();
        this.fillTexWidth = getFillWidthForType();
        this.fillTexHeight = getFillHeightForType();
    }

    // Custom dynamic (background + fill textures, sizes provided)
    public TextureWidget(int x, int y,
                         ResourceLocation backgroundTexture,
                         ResourceLocation fillTexture,
                         int backgroundWidth, int backgroundHeight,
                         int fillWidth, int fillHeight,
                         Direction direction,
                         boolean startFilled,
                         int rampTicks, int pauseTicks) {
        this.x = x;
        this.y = y;
        this.type = Type.ANIMATED_CUSTOM;
        this.direction = direction;
        this.fillProgress = 0.0f;
        this.texture = backgroundTexture;
        this.u = 0;
        this.v = 0;
        this.width = backgroundWidth;
        this.height = backgroundHeight;
        this.texWidth = backgroundWidth;
        this.texHeight = backgroundHeight;
        this.dynamic = true;
        this.rampTicks = Math.max(1, rampTicks);
        this.pauseTicks = Math.max(0, pauseTicks);
        this.startFilled = startFilled;
        this.fillTextureRes = fillTexture;
        this.fillTexWidth = fillWidth;
        this.fillTexHeight = fillHeight;
    }

    // Custom static state
    public TextureWidget(int x, int y,
                         ResourceLocation backgroundTexture,
                         ResourceLocation fillTexture,
                         int backgroundWidth, int backgroundHeight,
                         int fillWidth, int fillHeight,
                         Direction direction,
                         boolean filledState) {
        this.x = x;
        this.y = y;
        this.type = Type.ANIMATED_CUSTOM;
        this.direction = direction;
        this.fillProgress = filledState ? 1.0f : 0.0f;
        this.texture = backgroundTexture;
        this.u = 0;
        this.v = 0;
        this.width = backgroundWidth;
        this.height = backgroundHeight;
        this.texWidth = backgroundWidth;
        this.texHeight = backgroundHeight;
        this.dynamic = false;
        this.rampTicks = 0;
        this.pauseTicks = 0;
        this.startFilled = filledState;
        this.fillTextureRes = fillTexture;
        this.fillTexWidth = fillWidth;
        this.fillTexHeight = fillHeight;
    }
    
    private ResourceLocation getTextureForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/up_arrow.png");
                case DOWN: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/down_arrow.png");
                case LEFT: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/left_arrow.png");
                case RIGHT: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/right_arrow.png");
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/fire/fire.png");
        }
        return null;
    }
    
    private int getWidthForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP:
                case DOWN: return 15; // up_arrow.png and down_arrow.png are 15x22
                case LEFT:
                case RIGHT: return 22; // left_arrow.png and right_arrow.png are 22x15
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return 13; // fire.png is 13x13
        }
        return 16;
    }
    
    private int getHeightForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP:
                case DOWN: return 22; // up_arrow.png and down_arrow.png are 15x22
                case LEFT:
                case RIGHT: return 15; // left_arrow.png and right_arrow.png are 22x15
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return 13; // fire.png is 13x13
        }
        return 16;
    }
    
    private ResourceLocation getFillTextureForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/up_arrow_fill.png");
                case DOWN: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/down_arrow_fill.png");
                case LEFT: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/left_arrow_fill.png");
                case RIGHT: return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/arrows/right_arrow_fill.png");
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/fire/fire_fill.png");
        }
        return null;
    }

    private int getFillWidthForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP:
                case DOWN: return 16; // up_arrow_fill.png and down_arrow_fill.png are 16x22
                case LEFT:
                case RIGHT: return 22; // left_arrow_fill.png and right_arrow_fill.png are 22x16
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return 14; // fire_fill.png is 14x14
        }
        return 16;
    }

    private int getFillHeightForType() {
        if (type == Type.ANIMATED_ARROW) {
            switch (direction) {
                case UP:
                case DOWN: return 22; // up_arrow_fill.png and down_arrow_fill.png are 16x22
                case LEFT:
                case RIGHT: return 16; // left_arrow_fill.png and right_arrow_fill.png are 22x16
            }
        } else if (type == Type.ANIMATED_FLAME) {
            return 14; // fire_fill.png is 14x14
        }
        return 16;
    }
    
    /**
     * Renders this texture widget at the given position
     */
    public void render(GuiGraphics g, int baseX, int baseY) {
        int texX = baseX + this.x;
        int texY = baseY + this.y;
        
        if (this.type == Type.STATIC) {
            // Draw static texture
            g.blit(net.minecraft.client.renderer.RenderType::guiTextured, this.texture, 
                texX, texY, this.u, this.v, this.width, this.height, 
                this.texWidth, this.texHeight);
        } else {
            // Draw animated texture
            // Draw background texture
            g.blit(net.minecraft.client.renderer.RenderType::guiTextured, this.texture, 
                texX, texY, 0, 0, this.width, this.height, 
                this.texWidth, this.texHeight);
            
            // Compute progress (dynamic per-frame if requested)
            float progress = this.fillProgress;
            if (this.dynamic) {
                var mc = net.minecraft.client.Minecraft.getInstance();
                long ticks;
                if (mc.level != null) {
                    ticks = mc.level.getGameTime();
                } else {
                    ticks = System.currentTimeMillis() / 50L; // fallback ~20 TPS
                }
                int ramp = Math.max(1, this.rampTicks);
                int pause = Math.max(0, this.pauseTicks);
                int cycle = ramp + pause + pause; // pause at start, ramp, pause at end
                int t = (int)(ticks % cycle);
                if (t < pause) {
                    // hold at start state
                    progress = this.startFilled ? 1.0f : 0.0f;
                } else if (t < pause + ramp) {
                    float base = (t - pause) / (float) ramp; // 0 -> 1 across ramp
                    progress = this.startFilled ? (1.0f - base) : base;
                } else {
                    // hold at end state
                    progress = this.startFilled ? 0.0f : 1.0f;
                }
            }
            // Draw fill texture with animation
            if (progress > 0) {
                int fullW = (this.fillTexWidth > 0 ? this.fillTexWidth : this.getFillWidthForType());
                int fullH = (this.fillTexHeight > 0 ? this.fillTexHeight : this.getFillHeightForType());
                int fillWidth;
                int fillHeight;
                if (this.type == Type.ANIMATED_FLAME) {
                    // Flame: grow height upwards, full width
                    fillWidth = fullW;
                    fillHeight = (int)(fullH * progress);
                } else if (this.type == Type.ANIMATED_ARROW || this.type == Type.ANIMATED_CUSTOM) {
                    if (this.direction == Direction.LEFT || this.direction == Direction.RIGHT) {
                        fillWidth = (int)(fullW * progress);
                        fillHeight = fullH;
                    } else {
                        fillWidth = fullW;
                        fillHeight = (int)(fullH * progress);
                    }
                } else {
                    fillWidth = (int)(fullW * progress);
                    fillHeight = (int)(fullH * progress);
                }
                
                if (fillWidth > 0 && fillHeight > 0) {
                    // Calculate fill position based on direction
                    int fillX = texX;
                    int fillY = texY;
                    int srcU = 0;
                    int srcV = 0;
                    
                    switch (this.direction) {
                        case RIGHT:
                            // Fill from left to right
                            break;
                        case LEFT:
                            // Fill from right to left
                            fillX = texX + this.width - fillWidth;
                            srcU = fullW - fillWidth;
                            break;
                        case DOWN:
                            // Fill from top to bottom
                            break;
                        case UP:
                            // Fill from bottom to top
                            fillY = texY + this.height - fillHeight;
                            srcV = fullH - fillHeight;
                            break;
                    }
                    
                    // Requested pixel alignment tweaks
                    if (this.type == Type.ANIMATED_FLAME) {
                        // Flame fill 1px to the left
                        fillX -= 1;
                    } else if (this.type == Type.ANIMATED_ARROW && (this.direction == Direction.LEFT || this.direction == Direction.RIGHT)) {
                        // Left/Right arrows: shift fill up by 1px and left by 1px
                        fillY -= 1;
                    } else if (this.type == Type.ANIMATED_ARROW && (this.direction == Direction.UP || this.direction == Direction.DOWN)) {
                        // Up/Down arrows: shift fill left by 1px
                        fillX -= 1;
                    }
                    ResourceLocation fillRes = (this.fillTextureRes != null ? this.fillTextureRes : this.getFillTextureForType());
                    g.blit(net.minecraft.client.renderer.RenderType::guiTextured, fillRes, 
                        fillX, fillY, srcU, srcV, fillWidth, fillHeight, 
                        fullW, fullH);
                }
            }
        }
    }
}
