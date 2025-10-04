package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Slot widget that combines all slot functionality
 */
public class SlotWidget {
    public static ResourceLocation inputBg = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png");
    public static ResourceLocation outputBg = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot_output.png");


    public final SlotRole role;
    public final int x;
    public final int y;
    public final boolean drawBackground;
    public final boolean isOutputStyle;
    public final ItemStack stack;
    public final int recipeJsonIndex;
    public final ResourceLocation customBackground;
    public final boolean useCustomBackground;
    public final boolean noBackground;     // stable per-slot
    // Dynamic rendering sources (optional)
    private final Supplier<ItemStack> stackSupplier;
    private final List<ItemStack> stackList;
    private final Ingredient cyclingIngredient;
    private final int cyclingSalt;
    private final int cycleSpeedTicks; // ticks per item when cycling lists/ingredients (default 20)
    private final boolean listCycle;   // whether list sources should cycle (disabled for grid cells)
    private final List<Ingredient> ingredientList; // optional per-cell ingredient list for grids

    public SlotWidget(SlotRole role, int x, int y, boolean drawBackground, boolean isOutputStyle,
                      ItemStack stack, int recipeJsonIndex,
                      @Nullable ResourceLocation customBackground, boolean useCustomBackground, boolean noBackground) {
        this.role = role;
        this.x = x;
        this.y = y;
        this.drawBackground = drawBackground;
        this.isOutputStyle = isOutputStyle;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.recipeJsonIndex = recipeJsonIndex;
        this.customBackground = customBackground;
        this.useCustomBackground = useCustomBackground;
        this.noBackground = noBackground;
        this.stackSupplier = null;
        this.stackList = null;
        this.cyclingIngredient = null;
        this.cyclingSalt = 0;
        this.cycleSpeedTicks = 20;
        this.listCycle = false;
        this.ingredientList = null;
    }

    public SlotWidget(SlotRole role, int x, int y, boolean drawBackground, boolean isOutputStyle,
                      ItemStack stack, int recipeJsonIndex,
                      @Nullable ResourceLocation customBackground, boolean useCustomBackground, boolean noBackground,
                      @Nullable Supplier<ItemStack> stackSupplier,
                      @Nullable List<ItemStack> stackList,
                      @Nullable Ingredient cyclingIngredient,
                      int cyclingSalt,
                      int cycleSpeedTicks,
                      boolean listCycle,
                      @Nullable List<Ingredient> ingredientList) {
        this.role = role;
        this.x = x;
        this.y = y;
        this.drawBackground = drawBackground;
        this.isOutputStyle = isOutputStyle;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.recipeJsonIndex = recipeJsonIndex;
        this.customBackground = customBackground;
        this.useCustomBackground = useCustomBackground;
        this.noBackground = noBackground;
        this.stackSupplier = stackSupplier;
        this.stackList = stackList;
        this.cyclingIngredient = cyclingIngredient;
        this.cyclingSalt = cyclingSalt;
        this.cycleSpeedTicks = Math.max(1, cycleSpeedTicks);
        this.listCycle = listCycle;
        this.ingredientList = ingredientList;
    }

    // Factory methods for common use cases
    public static SlotWidget input(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.INPUT, x, y, true, false, stack, index, inputBg, true, false);
    }
    
    public static SlotWidget inputNoBackground(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.INPUT, x, y, false, false, stack, index, null, false, true);
    }
    
    public static SlotWidget output(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.OUTPUT, x, y, true, true, stack, index, outputBg, true, false);
    }
    
    public static SlotWidget outputNoBackground(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.OUTPUT, x, y, false, true, stack, index, null, false, true);
    }
    
    public static SlotWidget custom(SlotRole role, int x, int y, ItemStack stack, int index, ResourceLocation background) {
        return new SlotWidget(role, x, y, true, role == SlotRole.OUTPUT, stack, index, background, true, false);
    }
    
    // Ghost slots (for display purposes)
    public static SlotWidget ghost(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.GHOST, x, y, true, false, stack, index, inputBg, true, false);
    }
    
    public static SlotWidget ghostNoBackground(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.GHOST, x, y, false, false, stack, index, null, false, true);
    }
    
    // Catalyst slots (for catalysts, fuel, etc.)
    public static SlotWidget catalyst(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.CATALYST, x, y, true, false, stack, index, inputBg, true, false);
    }
    
    public static SlotWidget catalystNoBackground(int x, int y, ItemStack stack, int index) {
        return new SlotWidget(SlotRole.CATALYST, x, y, false, false, stack, index, null, false, true);
    }
    
    // Fuel slots (specialized for fuel items with automatic fuel selection)
    public static SlotWidget fuel(int x, int y, int index) {
        return new SlotWidget(SlotRole.CATALYST, x, y, true, false, getDefaultFuelItem(null), index, inputBg, true, false);
    }
    
    public static SlotWidget fuelNoBackground(int x, int y, int index) {
        return new SlotWidget(SlotRole.CATALYST, x, y, false, false, getDefaultFuelItem(null), index, null, false, true);
    }
    
    // Helper method to get appropriate fuel item for cooking recipes
    public static ItemStack getDefaultFuelItem(String recipeType) {
        if (recipeType == null) {
            return new ItemStack(net.minecraft.world.item.Items.COAL);
        }
        
        switch (recipeType.toLowerCase()) {
            case "smelting":
            case "furnace":
            case "blasting":
            case "smoking":
                return new ItemStack(net.minecraft.world.item.Items.COAL);
            default:
                return new ItemStack(net.minecraft.world.item.Items.COAL); // fallback
        }
    }

    // Expose size of list-backed source for grid widgets
    public int getStackListSize() {
        return this.stackList == null ? 0 : this.stackList.size();
    }

    /**
     * Renders this slot widget at the given position
     */
    public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
        int slotX = baseX + this.x;
        int slotY = baseY + this.y;
        ItemStack toRender;
        if (this.stackSupplier != null) {
            try {
                ItemStack supplied = this.stackSupplier.get();
                toRender = supplied == null ? ItemStack.EMPTY : supplied;
            } catch (Throwable t) {
                toRender = ItemStack.EMPTY;
            }
        } else if (this.stackList != null) {
            if (this.listCycle) {
                try {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    long ticks = (mc != null && mc.level != null) ? mc.level.getGameTime() : System.currentTimeMillis() / 50L;
                    int size = this.stackList.size();
                    if (size <= 0) {
                        toRender = ItemStack.EMPTY;
                    } else {
                        long step = Math.max(1L, this.cycleSpeedTicks);
                        int idx = (int)(((ticks / step) + Math.max(0, this.cyclingSalt)) % size);
                        ItemStack s = this.stackList.get(idx);
                        toRender = (s == null) ? ItemStack.EMPTY : s;
                    }
                } catch (Throwable t) {
                    toRender = ItemStack.EMPTY;
                }
            } else {
                int idx = Math.max(0, this.recipeJsonIndex);
                toRender = (idx < this.stackList.size() && this.stackList.get(idx) != null) ? this.stackList.get(idx) : ItemStack.EMPTY;
            }
        } else if (this.cyclingIngredient != null) {
            toRender = getCyclingItem(this.cyclingIngredient, this.cyclingSalt, this.cycleSpeedTicks);
        } else {
            toRender = this.stack;
        }
        
        // Draw slot background if not disabled
        if (!this.noBackground) {
            if (this.useCustomBackground && this.customBackground != null) {
                // Determine background size based on slot type
                int bgSize = (this.role == SlotRole.OUTPUT) ? 26 : 18;
                int bgOffset = (this.role == SlotRole.OUTPUT) ? -5 : -1;
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, this.customBackground, 
                    slotX + bgOffset, slotY + bgOffset, 0, 0, bgSize, bgSize, bgSize, bgSize);
            } else if (this.drawBackground) {
                ResourceLocation slotBg = ResourceLocation.withDefaultNamespace("container/slot");
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, slotBg, 
                    slotX - 1, slotY - 1, 0, 0, 18, 18, 18, 18);
            }
        }

        // Draw item (model-aware to preserve tints/dyes)
        g.renderItem(toRender, slotX, slotY);
        g.renderItemDecorations(font, toRender, slotX, slotY);
        
        // Add tooltips
        if (mouseX >= slotX && mouseX < slotX + 16 && 
            mouseY >= slotY && mouseY < slotY + 16 && !toRender.isEmpty()) {
            List<Component> tooltip;
            try {
                // Use the same tooltip method as the item panel for proper item tooltips
                tooltip = net.minecraft.client.gui.screens.Screen.getTooltipFromItem(net.minecraft.client.Minecraft.getInstance(), toRender);
            } catch (Throwable t) {
                tooltip = new ArrayList<>();
                tooltip.add(toRender.getHoverName());
            }
            
            g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    // Creates a copy of this slot configured for a specific grid cell position and index,
    // preserving role, backgrounds, suppliers, and cycling settings.
    SlotWidget copyForGridCell(int cellX, int cellY, int index) {
        ResourceLocation bg = this.customBackground;
        boolean useCustom = this.useCustomBackground;
        boolean drawBg = this.drawBackground;
        if (!useCustom && drawBg && !this.noBackground) {
            bg = (this.isOutputStyle || this.role == SlotRole.OUTPUT)
                    ? outputBg
                    : inputBg;
            useCustom = true;
        }
        return new SlotWidget(
                this.role,
                cellX,
                cellY,
                drawBg,
                this.isOutputStyle,
                this.stack,
                index,
                bg,
                useCustom,
                this.noBackground,
                this.stackSupplier,
                this.stackList,
                this.cyclingIngredient,
                this.cyclingSalt,
                this.cycleSpeedTicks,
                this.listCycle,
                this.ingredientList
        );
    }

    SlotWidget copyForGridCell(int cellX, int cellY, int index, boolean listCycleOverride) {
        ResourceLocation bg = this.customBackground;
        boolean useCustom = this.useCustomBackground;
        boolean drawBg = this.drawBackground;
        if (!useCustom && drawBg && !this.noBackground) {
            bg = (this.isOutputStyle || this.role == SlotRole.OUTPUT)
                    ? outputBg
                    : inputBg;
            useCustom = true;
        }
        return new SlotWidget(
                this.role,
                cellX,
                cellY,
                drawBg,
                this.isOutputStyle,
                this.stack,
                index,
                bg,
                useCustom,
                this.noBackground,
                this.stackSupplier,
                this.stackList,
                this.cyclingIngredient,
                this.cyclingSalt,
                this.cycleSpeedTicks,
                listCycleOverride,
                this.ingredientList
        );
    }

    private static ItemStack getCyclingItem(Ingredient ingredient, int salt, int speedTicks) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            long t = (mc != null && mc.level != null) ? mc.level.getGameTime() : System.currentTimeMillis() / 50L; // ~20 TPS fallback
            var holders = ingredient.items().toList();
            if (holders.isEmpty()) return ItemStack.EMPTY;
            long step = Math.max(1L, speedTicks);
            int idx = (int)(((t / step) + Math.max(0, salt)) % holders.size());
            return new ItemStack(holders.get(idx).value());
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    public int getCycleSpeedTicks() { return this.cycleSpeedTicks; }
    public boolean isListCycleEnabled() { return this.listCycle; }
    public @Nullable List<Ingredient> getIngredientList() { return this.ingredientList; }
}
