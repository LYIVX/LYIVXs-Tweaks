package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

/** Renders a grid of SlotWidgets generated from a list of ItemStacks. */
public class GridSlotWidget extends SlotWidget {
    public final int columns;
    public final int rows;
    public final boolean asOutput;
    public final boolean withBackground;
    public final List<ItemStack> items; // may be provided by template's supplier/cycling
    public final List<Ingredient> ingredients; // per-cell ingredients for cycling (falls back to template list)
    public final SlotWidget slotWidget; // Template to derive role/background and item behavior
    public final boolean hideEmptySlots;
    public final int cycleSpeedTicks;
    // Internal toggle to prevent list-based cycling replication (set by builder)
    boolean listCycleOverride = true;

    // Removed old boolean-based constructor in favor of SlotWidget-based API

    // New constructor: derive grid cell style from a SlotWidget template (role/background only)
    public GridSlotWidget(int x, int y, int columns, int rows, SlotWidget slotWidget, List<ItemStack> items) {
        super(SlotRole.GHOST, x, y, false, false, ItemStack.EMPTY, 0, null, false, true);
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.asOutput = slotWidget != null && slotWidget.role == SlotRole.OUTPUT;
        this.withBackground = slotWidget == null || !slotWidget.noBackground;
        this.items = items;
        this.ingredients = null;
        this.slotWidget = slotWidget;
        this.hideEmptySlots = true;
        this.cycleSpeedTicks = (slotWidget != null ? slotWidget.getCycleSpeedTicks() : 20);
    }

    public GridSlotWidget(int x, int y, int columns, int rows, SlotWidget slotWidget, List<ItemStack> items, boolean hideEmptySlots) {
        super(SlotRole.GHOST, x, y, false, false, ItemStack.EMPTY, 0, null, false, true);
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.asOutput = slotWidget != null && slotWidget.role == SlotRole.OUTPUT;
        this.withBackground = slotWidget == null || !slotWidget.noBackground;
        this.items = items;
        this.ingredients = null;
        this.slotWidget = slotWidget;
        this.hideEmptySlots = hideEmptySlots;
        this.cycleSpeedTicks = (slotWidget != null ? slotWidget.getCycleSpeedTicks() : 20);
    }

    // Simplified constructor: derive style and items from the SlotWidget (if grid)
    public GridSlotWidget(int x, int y, int columns, int rows, SlotWidget slotWidget) {
        this(x, y, columns, rows, slotWidget,
                (slotWidget instanceof GridSlotWidget gsw) ? gsw.items : Collections.emptyList());
    }

    // New constructor: per-cell ingredients cycling with speed
    public GridSlotWidget(int x, int y, int columns, int rows, SlotWidget slotWidget, List<Ingredient> ingredients, int cycleSpeedTicks) {
        super(SlotRole.GHOST, x, y, false, false, ItemStack.EMPTY, 0, null, false, true);
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.asOutput = slotWidget != null && slotWidget.role == SlotRole.OUTPUT;
        this.withBackground = slotWidget == null || !slotWidget.noBackground;
        this.items = Collections.emptyList();
        this.ingredients = ingredients;
        this.slotWidget = slotWidget;
        this.hideEmptySlots = true;
        this.cycleSpeedTicks = Math.max(1, cycleSpeedTicks);
    }

    @Override
    public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
        int idx = 0;
        int step = asOutput ? 26 : 18;
        int outOffX = asOutput ? 4 : 0;
        int outOffY = asOutput ? 4 : 0;
        int outOffset = 0;
        List<Ingredient> localIngs = (ingredients != null && !ingredients.isEmpty()) ? ingredients : (slotWidget != null ? slotWidget.getIngredientList() : null);
        int total = (localIngs != null && !localIngs.isEmpty()) ? localIngs.size() : ((items != null && !items.isEmpty()) ? items.size() : (slotWidget == null ? 0 : slotWidget.getStackListSize()));
        for (int r = 0; r < rows; r++) {
            int cellsInRow = columns;
            if (hideEmptySlots && total > 0) {
                int remaining = Math.max(0, total - r * columns);
                if (remaining <= 0) break;
                cellsInRow = Math.min(columns, remaining);
            }
            for (int c = 0; c < cellsInRow; c++) {
                int cx = baseX + this.x + c * step + outOffset + outOffX;
                int cy = baseY + this.y + r * step + outOffset + outOffY;
                SlotWidget cell;
                if (localIngs != null && idx < localIngs.size() && localIngs.get(idx) != null) {
                    Ingredient ing = localIngs.get(idx);
                    // Build a cycling cell with default/input style
                    boolean drawBg = withBackground;
                    boolean isOut = asOutput;
                    if (drawBg) {
                        cell = new SlotWidget(isOut ? SlotRole.OUTPUT : SlotRole.INPUT,
                                cx - baseX, cy - baseY,
                                true, isOut,
                                ItemStack.EMPTY, idx,
                                isOut ? SlotWidget.outputBg : SlotWidget.inputBg,
                                true, false,
                                null, null, ing, 0 /*same salt -> sync*/, this.cycleSpeedTicks, true, null);
                    } else {
                        cell = new SlotWidget(isOut ? SlotRole.OUTPUT : SlotRole.INPUT,
                                cx - baseX, cy - baseY,
                                false, isOut,
                                ItemStack.EMPTY, idx,
                                null, false, true,
                                null, null, ing, 0, this.cycleSpeedTicks, true, null);
                    }
                } else {
                    cell = (slotWidget != null)
                            ? slotWidget.copyForGridCell(cx - baseX, cy - baseY, idx, listCycleOverride)
                            : (items != null && idx < items.size() && items.get(idx) != null)
                                    ? (withBackground
                                        ? (asOutput ? SlotWidget.output(cx - baseX, cy - baseY, items.get(idx), idx) : SlotWidget.input(cx - baseX, cy - baseY, items.get(idx), idx))
                                        : (asOutput ? SlotWidget.outputNoBackground(cx - baseX, cy - baseY, items.get(idx), idx) : SlotWidget.inputNoBackground(cx - baseX, cy - baseY, items.get(idx), idx)))
                                    : (asOutput ? SlotWidget.output(cx - baseX, cy - baseY, ItemStack.EMPTY, idx) : SlotWidget.input(cx - baseX, cy - baseY, ItemStack.EMPTY, idx));
                }
                cell.render(g, font, baseX, baseY, mouseX, mouseY);
                idx++;
            }
        }
    }
}


