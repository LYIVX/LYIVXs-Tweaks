package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Slot-like widget that renders a scrollable grid of slots within a fixed number of rows. */
public class ScrollWidget extends SlotWidget {
    private final GridSlotWidget grid;
    private final int visibleRows;
    private int scrollOffsetRows = 0;
    private boolean dragging = false;

    public ScrollWidget(int x, int y, int visibleRows, GridSlotWidget grid) {
        super(SlotRole.GHOST, x, y, false, false, net.minecraft.world.item.ItemStack.EMPTY, 0, null, false, true);
        this.grid = grid;
        this.visibleRows = Math.max(1, visibleRows);
    }

    @Override
    public void render(GuiGraphics g, Font font, int baseX, int baseY, int mouseX, int mouseY) {
        // Compute visible columns (reserve last column for scrollbar)
        int visibleCols = Math.max(1, grid.columns - 1);
        int step = grid.asOutput ? 26 : 18;
        int itemCount;
        if (grid.items != null && !grid.items.isEmpty()) {
            itemCount = grid.items.size();
        } else if (grid.slotWidget != null) {
            itemCount = grid.slotWidget.getStackListSize();
        } else {
            itemCount = 0;
        }
        int totalRows = (itemCount + visibleCols - 1) / visibleCols;
        int startRow = Math.max(0, Math.min(scrollOffsetRows, Math.max(0, totalRows - visibleRows)));
        int rowsToRender = Math.min(visibleRows, Math.max(0, totalRows - startRow));

        int baseLeft = baseX + this.x;
        int baseTop = baseY + this.y;
        int outOffX = grid.asOutput ? 4 : 0;
        int outOffY = grid.asOutput ? 4 : 0;

        // Render only the visible window, row by row across visibleCols
        for (int r = 0; r < rowsToRender; r++) {
            int rowIndex = startRow + r;
            for (int c = 0; c < visibleCols; c++) {
                int itemIndex = rowIndex * visibleCols + c;
                if (itemIndex >= itemCount) break;
                int cx = baseLeft + grid.x + c * step + outOffX;
                int cy = baseTop + grid.y + r * step + outOffY;
                SlotWidget cell;
                if (grid.slotWidget != null) {
                    cell = grid.slotWidget.copyForGridCell(cx - baseLeft, cy - baseTop, itemIndex);
                } else {
                    var item = (grid.items != null && itemIndex < grid.items.size()) ? grid.items.get(itemIndex) : net.minecraft.world.item.ItemStack.EMPTY;
                    cell = grid.withBackground
                            ? (grid.asOutput ? SlotWidget.output(cx - baseLeft, cy - baseTop, item, itemIndex) : SlotWidget.input(cx - baseLeft, cy - baseTop, item, itemIndex))
                            : (grid.asOutput ? SlotWidget.outputNoBackground(cx - baseLeft, cy - baseTop, item, itemIndex) : SlotWidget.inputNoBackground(cx - baseLeft, cy - baseTop, item, itemIndex));
                }
                cell.render(g, font, baseLeft, baseTop, mouseX, mouseY);
            }
        }

        // Draw scrollbar over the reserved last column width (18px)
        // Track height should match the visible viewport height, not rowsToRender
        int heightPx = getHeightPixels();
        int trackX = (baseLeft + grid.x + visibleCols * step) + 2;
        int trackY = baseTop + grid.y;
        int trackTopHit = trackY - 1; // aligns with rendered background offset
        // Use GUI atlas sprite key (no "textures/" prefix and no ".png").
        // The image must live at: assets/ls_tweaks/textures/gui/sprites/recipe_viewer/widgets/scroller_background.png
        ResourceLocation track = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "recipe_viewer/widgets/scroller/scroller_background");
        g.blitSprite(RenderType::guiTextured, track, trackX - 1, trackTopHit, 16, Math.max(1, heightPx));

        int innerTop = trackTopHit + 1;
        int innerHeight = Math.max(0, heightPx - 2);
        float ratio = totalRows <= 0 ? 1f : (visibleRows / (float) Math.max(1, totalRows));
        int thumbHBase = Math.round(innerHeight * ratio);
        int thumbH = Math.max(8, Math.min(innerHeight, thumbHBase));
        int maxOffset = Math.max(0, totalRows - visibleRows);
        int currentOffset = Math.max(0, Math.min(scrollOffsetRows, maxOffset));
        int maxThumbTravel = Math.max(0, innerHeight - thumbH);
        float frac = maxOffset == 0 ? 0f : (currentOffset / (float) maxOffset);
        int thumbY = innerTop + Math.round(maxThumbTravel * frac);
        ResourceLocation sprite = dragging
                ? ResourceLocation.fromNamespaceAndPath("ls_tweaks", "recipe_viewer/widgets/scroller/scroller_active")
                : ResourceLocation.fromNamespaceAndPath("ls_tweaks", "recipe_viewer/widgets/scroller/scroller");
        g.blitSprite(RenderType::guiTextured, sprite, trackX, thumbY, 14, thumbH);
    }

    public void scrollBy(int rowsDelta) {
        int visibleCols = getVisibleCols();
        int itemCount;
        if (grid.items != null && !grid.items.isEmpty()) {
            itemCount = grid.items.size();
        } else if (grid.slotWidget != null) {
            itemCount = grid.slotWidget.getStackListSize();
        } else {
            itemCount = 0;
        }
        int totalRows = Math.max(1, (itemCount + visibleCols - 1) / visibleCols);
        int maxOffset = Math.max(0, totalRows - visibleRows);
        scrollOffsetRows = Math.max(0, Math.min(scrollOffsetRows + rowsDelta, maxOffset));
    }

    public int getVisibleRows() { return visibleRows; }
    public int getVisibleCols() { return Math.max(1, grid.columns - 1); }
    public int getWidthPixels() { int step = grid.asOutput ? 26 : 18; return getVisibleCols() * step + 18; }
    public int getHeightPixels() { int step = grid.asOutput ? 26 : 18; return visibleRows * step; }

    // Event helpers; baseX/baseY are content origin passed from screen
    public boolean mouseScrolled(int baseX, int baseY, int mouseX, int mouseY, double delta) {
        int visibleCols = getVisibleCols();
        int step = grid.asOutput ? 26 : 18;
        int contentLeft = baseX + this.x + grid.x;
        int contentTop = baseY + this.y + grid.y;
        int contentWidth = visibleCols * step;
        int contentHeight = getHeightPixels();
        int trackLeft = contentLeft + contentWidth; // equals trackX
        int trackTop = contentTop; // equals trackY
        // Track is drawn at (trackLeft-1, trackTop-1), so expand hitbox accordingly
        boolean overTrack = mouseX >= (trackLeft - 1) && mouseX < (trackLeft - 1 + 18)
                && mouseY >= (trackTop - 1) && mouseY < (trackTop - 1 + contentHeight);
        boolean overContent = mouseX >= contentLeft && mouseX < contentLeft + contentWidth
                && mouseY >= contentTop && mouseY < contentTop + contentHeight;
        if (overTrack || overContent) {
            scrollBy(delta < 0 ? 1 : -1);
            return true;
        }
        return false;
    }

    public boolean mouseClicked(int baseX, int baseY, int mouseX, int mouseY, int button) {
        int visibleCols = getVisibleCols();
        int contentLeft = baseX + this.x + grid.x;
        int contentTop = baseY + this.y + grid.y;
        int contentHeight = getHeightPixels();
        int step = grid.asOutput ? 26 : 18;
        int trackLeft = contentLeft + visibleCols * step; // equals trackX
        int trackTop = contentTop; // equals trackY
        // Align with drawn track (-1 offset)
        if (button == 0 && mouseX >= (trackLeft - 1) && mouseX < (trackLeft - 1 + 18)
                && mouseY >= (trackTop - 1) && mouseY < (trackTop - 1 + contentHeight)) {
            dragging = true;
            updateFromThumb(baseX, baseY, mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(int baseX, int baseY, int mouseX, int mouseY, int button, double dx, double dy) {
        if (!dragging) return false;
        updateFromThumb(baseX, baseY, mouseY);
        return true;
    }

    public void mouseReleased() { dragging = false; }

    private void updateFromThumb(int baseX, int baseY, int mouseY) {
        int visibleCols = getVisibleCols();
        int itemCount;
        if (grid.items != null && !grid.items.isEmpty()) {
            itemCount = grid.items.size();
        } else if (grid.slotWidget != null) {
            itemCount = grid.slotWidget.getStackListSize();
        } else {
            itemCount = 0;
        }
        int totalRows = Math.max(1, (itemCount + visibleCols - 1) / visibleCols);
        int heightPx = getHeightPixels();
        int trackTopHit = baseY + this.y + grid.y - 1;
        int innerTop = trackTopHit + 1;
        int innerHeight = Math.max(0, heightPx - 2);
        int thumbHBase = Math.round(innerHeight * (visibleRows / (float) totalRows));
        int thumbH = Math.max(8, Math.min(innerHeight, thumbHBase));
        int maxTravel = Math.max(0, innerHeight - thumbH);
        float pos = Math.max(0, Math.min(maxTravel, mouseY - innerTop - (thumbH / 2f)));
        float frac = maxTravel == 0 ? 0 : (pos / (float) maxTravel);
        int maxOffset = Math.max(0, totalRows - visibleRows);
        scrollOffsetRows = Math.max(0, Math.min(maxOffset, Math.round(frac * maxOffset)));
    }
}


