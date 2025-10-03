package net.lyivx.ls_tweaks.client.overlay;

import net.lyivx.ls_tweaks.recipes.index.ItemIndex;
import net.lyivx.ls_tweaks.recipes.index.RecipeIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Right-side item browser overlay, rendered on container screens. */
public final class ItemPanelOverlay {
    private static int pageIndex = 0;
    private static String filter = "";
    private static boolean searchFocused = false;
    private static int columns = 4;
    private static int rows = 7;
    private static List<ItemStack> visible = new ArrayList<>();
    private static int panelLeft, panelTop, panelWidth, panelHeight;
    private static final int SLOT_SIZE = 18;
    private static final int PANEL_MARGIN = 4;
    private static final int BOTTOM_BAR_HEIGHT = 22;
    private static final int SEARCH_BAR_HEIGHT = 18;
    // Textures for pagination buttons (18x18)
    private static final net.minecraft.resources.ResourceLocation PAGE_BACK = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/left_button.png");
    private static final net.minecraft.resources.ResourceLocation PAGE_BACK_HI = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/left_button_highlighted.png");
    private static final net.minecraft.resources.ResourceLocation PAGE_FWD = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/right_button.png");
    private static final net.minecraft.resources.ResourceLocation PAGE_FWD_HI = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/right_button_highlighted.png");
    
    // Textures for search bar and config button
    private static final net.minecraft.resources.ResourceLocation TEXT_FIELD = net.minecraft.resources.ResourceLocation.withDefaultNamespace("widget/text_field");
    private static final net.minecraft.resources.ResourceLocation TEXT_FIELD_HI = net.minecraft.resources.ResourceLocation.withDefaultNamespace("widget/text_field_highlighted");
    // Config button textures (18x18)
    private static final net.minecraft.resources.ResourceLocation CONFIG_BUTTON = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/cog_button.png");
    private static final net.minecraft.resources.ResourceLocation CONFIG_BUTTON_HI = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/cog_button_highlighted.png");

    public static void onDataReload() {
        ItemIndex.rebuild();
        RecipeIndex.rebuild();
    }

    public static void render(AbstractContainerScreen<?> cs, GuiGraphics g) {
        if (!net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.enabled()) return;
        computeLayout(cs);
        rebuildPage();
        
        // Get mouse position for hover detection
        int mx = currentMouseX();
        int my = currentMouseY();

        // Draw items - center the grid horizontally
        int gridWidth = columns * SLOT_SIZE;
        int contentRight = panelLeft + panelWidth - PANEL_MARGIN; // keep content at least 4px inside right edge
        int gridLeft = panelLeft + (panelWidth - gridWidth) / 2; // Center the grid
        if (gridLeft + gridWidth > contentRight) gridLeft = contentRight - gridWidth; // clamp within background
        if (gridLeft < panelLeft) gridLeft = panelLeft;
        int slot = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                int x = gridLeft + c * SLOT_SIZE + 1; // shift right by 1px
                int y = panelTop + r * SLOT_SIZE;
                
                if (slot < visible.size()) {
                    g.renderFakeItem(visible.get(slot), x, y);
                }
                slot++;
            }
        }

        // Search bar positioning - consistent gaps between all elements
        boolean searchTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.searchBarPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean buttonsTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.buttonsPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean fullSide = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fillMode() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.FillMode.FULL_SIDE;
        
        // Calculate consistent positioning with equal gaps
        int searchY;
        
        if (fullSide) {
            // Full size mode - use same logic as container size but fill whole right side
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int bgTop = PANEL_MARGIN;
            int bgBottom = screenHeight - PANEL_MARGIN;
            
            // Calculate available space within background padding
            int availableTop = bgTop + PANEL_MARGIN;
            int availableBottom = bgBottom - PANEL_MARGIN;
            int availableHeight = availableBottom - availableTop;
            
            // Calculate total height needed for all elements
            int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
            int remainingSpace = availableHeight - totalElementHeight;
            
            // Distribute remaining space as gaps (2 gaps between 3 elements)
            int gap = Math.max(3, remainingSpace / 2);
            
            if (searchTop) {
                if (buttonsTop) {
                    // Both at top: buttons (top), search bar (middle), slots (bottom)
                    // Search bar is middle, so it's between buttons and slots
                    int buttonsBottom = availableTop + BOTTOM_BAR_HEIGHT;
                    int slotsTop = availableBottom - (rows * SLOT_SIZE);
                    searchY = buttonsBottom + (slotsTop - buttonsBottom - SEARCH_BAR_HEIGHT) / 2;
                } else {
                    // Search bar (top), slots (middle), buttons (bottom)
                    // Search bar goes all the way up
                    searchY = availableTop;
                }
            } else {
                if (buttonsTop) {
                    // Buttons (top), slots (middle), search bar (bottom)
                    // Search bar goes all the way down
                    searchY = availableBottom - SEARCH_BAR_HEIGHT;
                } else {
                    // Both at bottom: slots (top), buttons (middle), search bar (bottom)
                    // Search bar goes all the way down
                    searchY = availableBottom - SEARCH_BAR_HEIGHT;
                }
            }
        } else {
            // Container size mode - use specific positioning
            searchY = getContainerSizeSearchY(cs, searchTop, buttonsTop);
        }
        
        // Draw background if enabled - always same height as container and respect right padding
        if (net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.showItemPanelBackground()) {
            int bgLeft = panelLeft - PANEL_MARGIN;
            // Only add left padding so the background's right edge also respects right PANEL_MARGIN
            int bgWidth = panelWidth + PANEL_MARGIN;
            
            if (fullSide) {
                // Full size mode - use dynamic background that covers all elements
                int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                int bgTop = PANEL_MARGIN;
                int bgBottom = screenHeight - PANEL_MARGIN;
                int bgHeight = bgBottom - bgTop;
                
                g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, 
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe"), 
                    bgLeft, bgTop, bgWidth, bgHeight);
            } else {
                // Container size mode - exactly match container size, reduce by 1px
                int containerTop = getTop(cs);
                int containerBottom = getTop(cs) + getImageHeight(cs);
                int bgTop = containerTop;
                int bgHeight = containerBottom - containerTop - 1; // Reduce by 1px
                
                g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, 
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe"), 
                    bgLeft, bgTop, bgWidth, bgHeight);
            }
        }
        
        // Draw slot backgrounds if enabled (after main background) - only for slots with items
        if (net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.showSlotBackgrounds()) {
            int slotIndex = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    int x = gridLeft + c * SLOT_SIZE + 1; // align with item shift
                    int y = panelTop + r * SLOT_SIZE;
                    // Only draw background if there's an item in this slot
                    if (slotIndex < visible.size() && !visible.get(slotIndex).isEmpty()) {
                        // Add padding to slot background
                        g.blit(net.minecraft.client.renderer.RenderType::guiTextured, 
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/slots/slot.png"), 
                            x - 1, y - 1, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                    }
                    slotIndex++;
                }
            }
        }
        
        int searchX = panelLeft;
        int configBtnSize = 18;
        int searchW = Math.max(0, (contentRight - configBtnSize - (PANEL_MARGIN / 2)) - searchX);
        
        // Check if mouse is over search bar for highlighting
        boolean searchHover = pointIn(searchX, searchY, searchW, SEARCH_BAR_HEIGHT, mx, my);
        
        // Draw search bar background (200x20 texture, use nine-slice scaling)
        g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, 
            (searchHover || searchFocused) ? TEXT_FIELD_HI : TEXT_FIELD, searchX, searchY, searchW, SEARCH_BAR_HEIGHT);
        
        // Draw search text
        String displayText;
        int textColor;
        
        if (searchFocused) {
            // When focused, show typed text or just cursor
            displayText = filter.isEmpty() ? "" : filter;
            textColor = 0xFFFFFF;
            // Add blinking cursor
            if (System.currentTimeMillis() / 500 % 2 == 0) {
                displayText += "_";
            }
        } else if (searchHover) {
            // When hovering but not focused, show placeholder in white
            displayText = filter.isEmpty() ? "Search..." : filter;
            textColor = 0xFFFFFF;
        } else {
            // Normal state
            displayText = filter.isEmpty() ? "Search..." : filter;
            textColor = filter.isEmpty() ? 0x808080 : 0xFFFFFF;
        }
        
        g.drawString(Minecraft.getInstance().font, displayText, searchX + 4, searchY + 6, textColor, false);
        
        // Config button (18x18 texture)
        int configX = contentRight - configBtnSize;
        int configY = searchY + (SEARCH_BAR_HEIGHT - configBtnSize) / 2; // Center vertically
        int cmx1 = currentMouseX();
        int cmy1 = currentMouseY();
        boolean configHover = pointIn(configX, configY, configBtnSize, configBtnSize, cmx1, cmy1);
        g.blit(net.minecraft.client.renderer.RenderType::guiTextured, 
            configHover ? CONFIG_BUTTON_HI : CONFIG_BUTTON, 
            configX, configY, 0, 0, configBtnSize, configBtnSize, configBtnSize, configBtnSize);

        // Buttons and page indicator
        int barY = bottomBarY(cs);
        int btnW = 18, btnH = 18;
        int prevX = panelLeft;
        int nextX = contentRight - btnW;
        boolean prevHover = pointIn(prevX, barY, btnW, btnH, cmx1, cmy1);
        boolean nextHover = pointIn(nextX, barY, btnW, btnH, cmx1, cmy1);

        // Draw page background box between buttons
        int boxL = prevX + btnW - 2;
        int boxR = nextX + 2;
        if (boxR > boxL) {
            g.fill(boxL, barY, boxR, barY + btnH, 0xFF8B8B8B);
        }
        drawIconButton(g, prevX, barY, btnW, btnH, prevHover ? PAGE_BACK_HI : PAGE_BACK);
        drawIconButton(g, nextX, barY, btnW, btnH, nextHover ? PAGE_FWD_HI : PAGE_FWD);

        int total = Math.max(1, totalPages());
        int cur = Math.min(pageIndex + 1, total);
        String pg = cur + "/" + total;
        int textX = panelLeft + (panelWidth - Minecraft.getInstance().font.width(pg)) / 2;
        int textY = barY + (BOTTOM_BAR_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2; // Center vertically
        g.drawString(Minecraft.getInstance().font, pg, textX, textY - 1, 0xFFFFFFFF, true); // Enable shadow

        // Tooltip for hovered stack with mod name
        ItemStack hovered = hoveredStack(cs);
        if (hovered != null && !hovered.isEmpty()) {
            var font = Minecraft.getInstance().font;
            List<Component> lines = getTooltipWithModName(cs, hovered);
            try {
                g.renderComponentTooltip(font, lines, mx, my);
            } catch (Throwable ignored) {
                g.renderTooltip(font, hovered, mx, my);
            }
        }
    }

    public static boolean keyPressed(AbstractContainerScreen<?> cs, int keyCode, int scanCode, int modifiers) {
        if (!net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.enabled()) return false;
        // If the search field is focused, we want to consume most key presses here so the
        // container screen does not handle them (e.g., 'E' closing the screen).
        if (!searchFocused) return false;
        
        System.out.println("[LS Tweaks][RV] keyPressed called: keyCode=" + keyCode + " focused=" + searchFocused);
        
        // Handle backspace
        if (keyCode == 259) { // GLFW.GLFW_KEY_BACKSPACE
            if (!filter.isEmpty()) {
                filter = filter.substring(0, filter.length() - 1);
                pageIndex = 0; // Reset to first page
                System.out.println("[LS Tweaks][RV] Backspace, filter now: '" + filter + "'");
                return true;
            }
        }
        
        // Handle escape to unfocus
        if (keyCode == 256) { // GLFW.GLFW_KEY_ESCAPE
            searchFocused = false;
            System.out.println("[LS Tweaks][RV] Escape pressed, unfocusing");
            return true;
        }
        
        // For all other keys while focused, consume the event so the container
        // does not process it (prevents 'E' closing the screen). Actual text
        // characters are delivered via charTyped.
        return true;
    }
    
    public static boolean charTyped(AbstractContainerScreen<?> cs, char codePoint, int modifiers) {
        if (!net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.enabled() || !searchFocused) return false;
        
        System.out.println("[LS Tweaks][RV] charTyped called: '" + codePoint + "' focused=" + searchFocused);
        
        // Only accept printable characters
        if (codePoint >= 32 && codePoint <= 126) {
            filter += codePoint;
            pageIndex = 0; // Reset to first page
            System.out.println("[LS Tweaks][RV] Added char, filter now: '" + filter + "'");
            return true;
        }
        
        return false;
    }

    public static boolean mouseClicked(AbstractContainerScreen<?> cs, double mx, double my, int button) {
        if (!net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.enabled()) return false;
        computeLayout(cs);
        System.out.println("[LS Tweaks][RV] click in overlay region? mx=" + mx + " my=" + my);
        
        // Config button - use same positioning logic as rendering
        // Use same consistent positioning logic as render method
        boolean searchTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.searchBarPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean buttonsTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.buttonsPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean fullSide = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fillMode() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.FillMode.FULL_SIDE;
        
        // Calculate consistent positioning with equal gaps
        int searchY;
        int availableHeight = fullSide ? (Minecraft.getInstance().getWindow().getGuiScaledHeight() - (2 * PANEL_MARGIN)) : 
                                        (getTop(cs) + getImageHeight(cs) - panelTop);
        
        // Calculate total height needed for all elements
        int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
        int totalGaps = 2 * PANEL_MARGIN; // Top and bottom gaps
        int remainingSpace = availableHeight - totalElementHeight - totalGaps;
        
        // Distribute remaining space as gaps between elements (2 gaps between 3 elements)
        int elementGap = Math.max(6, remainingSpace / 2);
        
        if (fullSide) {
            // Full size mode - use dynamic positioning
            if (searchTop) {
                if (buttonsTop) {
                    // Both at top: buttons first, then search bar, then slots
                    searchY = panelTop - SEARCH_BAR_HEIGHT - elementGap;
                } else {
                    // Search bar at top, slots in middle, buttons at bottom
                    searchY = panelTop - SEARCH_BAR_HEIGHT - elementGap;
                }
            } else {
                if (buttonsTop) {
                    // Buttons at top, slots in middle, search bar at bottom
                    searchY = panelTop + rows * SLOT_SIZE + elementGap;
                } else {
                    // Slots at top, buttons in middle, search bar at bottom
                    searchY = panelTop + rows * SLOT_SIZE + BOTTOM_BAR_HEIGHT + elementGap;
                }
            }
        } else {
            // Container size mode - use specific positioning
            searchY = getContainerSizeSearchY(cs, searchTop, buttonsTop);
        }
        
        // Check if search bar was clicked
        int searchW = panelWidth - 20;
        if (pointIn(panelLeft, searchY, searchW, SEARCH_BAR_HEIGHT, (int)mx, (int)my)) {
            System.out.println("[LS Tweaks][RV] search bar clicked, focusing");
            // Focus search bar
            searchFocused = true;
            return true;
        } else {
            // Clicked outside search bar, unfocus it
            if (searchFocused) {
                System.out.println("[LS Tweaks][RV] clicked outside search bar, unfocusing");
            }
            searchFocused = false;
        }
        
        int configX = panelLeft + panelWidth - 14;
        int configY = searchY + (SEARCH_BAR_HEIGHT - 14) / 2; // Center vertically
        if (pointIn(configX, configY, 14, 14, (int)mx, (int)my)) {
            System.out.println("[LS Tweaks][RV] config button clicked");
            searchFocused = false; // Unfocus search when clicking config button
            net.lyivx.ls_tweaks.recipeview.config.RecipeViewerClientConfigHelper.openConfigScreen();
            return true;
        }
        
        // Buttons
        int barY = bottomBarY(cs);
        int btnW = 18, btnH = 18;
        int prevX = panelLeft;
        int nextX = panelLeft + panelWidth - btnW;
        if (pointIn(prevX, barY, btnW, btnH, (int)mx, (int)my)) { 
            System.out.println("[LS Tweaks][RV] prev page"); 
            searchFocused = false; // Unfocus search when clicking page button
            prevPage(); 
            return true; 
        }
        if (pointIn(nextX, barY, btnW, btnH, (int)mx, (int)my)) { 
            System.out.println("[LS Tweaks][RV] next page"); 
            searchFocused = false; // Unfocus search when clicking page button
            nextPage(); 
            return true; 
        }

        // Grid
        int w = columns * SLOT_SIZE;
        int h = rows * SLOT_SIZE;
        int gridLeftClick = panelLeft + (panelWidth - w) / 2;
        if (mx < gridLeftClick + 1 || my < panelTop || mx >= gridLeftClick + 1 + w || my >= panelTop + h) { 
            System.out.println("[LS Tweaks][RV] outside grid"); 
            searchFocused = false; // Unfocus search when clicking outside
            return false; 
        }
        int cx = (int)(mx - (gridLeftClick + 1)) / SLOT_SIZE;
        int cy = (int)(my - panelTop) / SLOT_SIZE;
        int idx = cy * columns + cx;
        if (idx < 0 || idx >= visible.size()) { 
            System.out.println("[LS Tweaks][RV] idx out of range: " + idx); 
            searchFocused = false; // Unfocus search when clicking invalid area
            return false; 
        }
        ItemStack stack = visible.get(idx);
        if (stack == null || stack.isEmpty()) { 
            System.out.println("[LS Tweaks][RV] empty stack at idx=" + idx); 
            searchFocused = false; // Unfocus search when clicking empty slot
            return false; 
        }
        searchFocused = false; // Unfocus search when clicking on item
        if (button == 0) { System.out.println("[LS Tweaks][RV] open recipes for " + stack.getHoverName().getString()); net.lyivx.ls_tweaks.client.screen.RecipeBrowserScreen.openForResult(stack); return true; }
        else if (button == 1) { System.out.println("[LS Tweaks][RV] open usages for " + stack.getHoverName().getString()); net.lyivx.ls_tweaks.client.screen.RecipeBrowserScreen.openForUsage(stack); return true; }
        return false;
    }

    private static void rebuildPage() {
        int limit = Math.max(1, columns * rows);
        int start = Math.max(0, Math.min(pageIndex * limit, Math.max(0, ItemIndex.all().size() - 1)));
        visible = ItemIndex.page(start, limit, filter);
        if (visible.size() < limit && start + visible.size() >= ItemIndex.all().size()) {
            // clamp page index if overflow
            int total = totalPages();
            if (pageIndex >= total) pageIndex = Math.max(0, total - 1);
        }
    }

    private static int getRightEdge(AbstractContainerScreen<?> cs) { return getLeft(cs) + getImageWidth(cs); }
    private static int getLeft(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("leftPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.width - 176) / 2; }
    }
    private static int getImageWidth(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("imageWidth"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return 176; }
    }
    private static int getTop(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("topPos"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return (cs.height - 166) / 2; }
    }

    private static boolean pointIn(int x, int y, int w, int h, int px, int py) {
        return px >= x && py >= y && px < x + w && py < y + h;
    }

    private static void computeLayout(AbstractContainerScreen<?> cs) {
        boolean fullSide = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fillMode() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.FillMode.FULL_SIDE;
        boolean searchTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.searchBarPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean buttonsTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.buttonsPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        
        if (fullSide) {
            panelLeft = getRightEdge(cs) + PANEL_MARGIN + 4; // Add 4px gap
            panelWidth = Math.max(0, cs.width - panelLeft - PANEL_MARGIN);
            
            // Calculate available height within screen bounds with padding
            int screenHeight = cs.height;
            int bgTop = PANEL_MARGIN;
            int bgBottom = screenHeight - PANEL_MARGIN;
            int availableTop = bgTop + PANEL_MARGIN;
            int availableBottom = bgBottom - PANEL_MARGIN;
            int availableHeight = availableBottom - availableTop;
            
            // Calculate total height needed for all elements
            int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT;
            int remainingHeight = availableHeight - totalElementHeight - 8; // 8px for gaps
            
            // Use remaining height for slots
            panelHeight = Math.max(0, remainingHeight);
            
            // Set panel top using the same logic as container size
            panelTop = getFullSizePanelTop(searchTop, buttonsTop);
        } else {
            panelLeft = getRightEdge(cs) + PANEL_MARGIN + 4; // Add 4px gap
            panelWidth = Math.max(0, cs.width - panelLeft - PANEL_MARGIN);
            
            // Calculate available height within container bounds with padding
            int containerTop = getTop(cs);
            int containerBottom = getTop(cs) + getImageHeight(cs) - 1; // Account for background reduction
            int availableTop = containerTop + PANEL_MARGIN;
            int availableBottom = containerBottom - PANEL_MARGIN;
            int availableHeight = availableBottom - availableTop;
            
            // Calculate total height needed for all elements
            int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT;
            int remainingHeight = availableHeight - totalElementHeight;
            
            // Use remaining height for slots (with some gap)
            panelHeight = Math.max(0, remainingHeight - 16); // 16px for gaps between elements
            
            // Set panel top using the new positioning logic
            panelTop = getContainerSizePanelTop(cs, searchTop, buttonsTop);
        }

        int fixedCols = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fixedColumns();
        int fixedRows = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fixedRows();
        
        if (fullSide) {
            // FullSide mode: dynamically size based on available space
            columns = fixedCols > 0 ? fixedCols : Math.max(1, panelWidth / SLOT_SIZE);
            rows = fixedRows > 0 ? fixedRows : Math.max(1, panelHeight / SLOT_SIZE);
            // Don't override panelWidth in FullSide mode - use full available width
        } else {
            // Container size mode: use full available width but limit height to container bounds
            columns = fixedCols > 0 ? fixedCols : Math.max(1, panelWidth / SLOT_SIZE);
            rows = fixedRows > 0 ? fixedRows : Math.max(1, panelHeight / SLOT_SIZE);
            // Don't override panelWidth - use full available width
        }
        
        // Force dynamic sizing if fixed values are the default (4, 7)
        if (fixedCols == 4 && fixedRows == 7) {
            columns = Math.max(1, panelWidth / SLOT_SIZE);
            if (fullSide) {
                rows = Math.max(1, panelHeight / SLOT_SIZE);
            } else {
                // For container mode, calculate rows based on available space
                int containerTop = getTop(cs);
                int containerBottom = getTop(cs) + getImageHeight(cs) - 1;
                int availableTop = containerTop + PANEL_MARGIN;
                int availableBottom = containerBottom - PANEL_MARGIN;
                int availableHeight = availableBottom - availableTop;
                int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT;
                int remainingHeight = availableHeight - totalElementHeight - 4; // 8px for gaps
                rows = Math.max(1, remainingHeight / SLOT_SIZE);
            }
        }
    }

    private static int getImageHeight(AbstractContainerScreen<?> cs) {
        try { var f = AbstractContainerScreen.class.getDeclaredField("imageHeight"); f.setAccessible(true); return f.getInt(cs); } catch (Throwable ignored) { return 166; }
    }

    private static int getFullSizePanelTop(boolean searchTop, boolean buttonsTop) {
        // Full size mode - use same logic as container size but fill whole right side
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int bgTop = PANEL_MARGIN;
        int bgBottom = screenHeight - PANEL_MARGIN;
        
        // Calculate available space within background padding
        int availableTop = bgTop + PANEL_MARGIN;
        int availableBottom = bgBottom - PANEL_MARGIN;
        int availableHeight = availableBottom - availableTop;
        
        // Calculate total height needed for all elements
        int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
        int remainingSpace = availableHeight - totalElementHeight;
        
        // Distribute remaining space as gaps (2 gaps between 3 elements)
        int gap = Math.max(4, remainingSpace / 2);
        
        // Calculate where slots should start
        int slotsHeight = rows * SLOT_SIZE;
        
        if (searchTop && buttonsTop) {
            // Both at top: buttons (top), search bar (middle), slots (bottom)
            // Slots go all the way down
            return availableBottom - slotsHeight;
        } else if (searchTop) {
            // Search bar (top), slots (middle), buttons (bottom)
            // Center slots between search bar and buttons, move down by 3px
            int searchBottom = availableTop + SEARCH_BAR_HEIGHT;
            int buttonsBarTop = availableBottom - BOTTOM_BAR_HEIGHT;
            return searchBottom + (buttonsBarTop - searchBottom - slotsHeight) / 2 + 3;
        } else if (buttonsTop) {
            // Buttons (top), slots (middle), search bar (bottom)
            // Center slots between buttons and search bar
            int buttonsBottom = availableTop + BOTTOM_BAR_HEIGHT;
            int searchBarTop = availableBottom - SEARCH_BAR_HEIGHT;
            return buttonsBottom + (searchBarTop - buttonsBottom - slotsHeight) / 2;
        } else {
            // Both at bottom: slots (top), buttons (middle), search bar (bottom)
            // Slots go all the way up but respect padding, move down by 2px more
            return availableTop + 2;
        }
    }

    private static int getContainerSizePanelTop(AbstractContainerScreen<?> cs, boolean searchTop, boolean buttonsTop) {
        // Container size mode - center slots dynamically between top and bottom elements
        int containerTop = getTop(cs);
        int containerBottom = getTop(cs) + getImageHeight(cs) - 1; // Account for background reduction
        int bgTop = containerTop;
        int bgBottom = containerBottom;
        
        // Calculate available space within background padding
        int availableTop = bgTop + PANEL_MARGIN;
        int availableBottom = bgBottom - PANEL_MARGIN;
        int availableHeight = availableBottom - availableTop;
        
        // Calculate total height needed for all elements
        int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
        int remainingSpace = availableHeight - totalElementHeight;
        
        // Distribute remaining space as gaps (2 gaps between 3 elements)
        int gap = Math.max(4, remainingSpace / 2);
        
        // Calculate where slots should start
        int slotsHeight = rows * SLOT_SIZE;
        
        if (searchTop && buttonsTop) {
            // Both at top: buttons (top), search bar (middle), slots (bottom)
            // Slots go all the way down
            return availableBottom - slotsHeight;
        } else if (searchTop) {
            // Search bar (top), slots (middle), buttons (bottom)
            // Center slots between search bar and buttons, move down by 3px
            int searchBottom = availableTop + SEARCH_BAR_HEIGHT;
            int buttonsBarTop = availableBottom - BOTTOM_BAR_HEIGHT;
            return searchBottom + (buttonsBarTop - searchBottom - slotsHeight) / 2 + 3;
        } else if (buttonsTop) {
            // Buttons (top), slots (middle), search bar (bottom)
            // Center slots between buttons and search bar
            int buttonsBottom = availableTop + BOTTOM_BAR_HEIGHT;
            int searchBarTop = availableBottom - SEARCH_BAR_HEIGHT;
            return buttonsBottom + (searchBarTop - buttonsBottom - slotsHeight) / 2;
        } else {
            // Both at bottom: slots (top), buttons (middle), search bar (bottom)
            // Slots go all the way up but respect padding, move down by 1px
            return availableTop + 2;
        }
    }

    private static int getContainerSizeSearchY(AbstractContainerScreen<?> cs, boolean searchTop, boolean buttonsTop) {
        // Container size mode - respect padding and dynamically center
        int containerTop = getTop(cs);
        int containerBottom = getTop(cs) + getImageHeight(cs) - 1; // Account for background reduction
        int bgTop = containerTop;
        int bgBottom = containerBottom;
        
        // Calculate available space within background padding
        int availableTop = bgTop + PANEL_MARGIN;
        int availableBottom = bgBottom - PANEL_MARGIN;
        int availableHeight = availableBottom - availableTop;
        
        // Calculate total height needed for all elements
        int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
        int remainingSpace = availableHeight - totalElementHeight;
        
        // Distribute remaining space as gaps (2 gaps between 3 elements)
        int gap = Math.max(4, remainingSpace / 2);
        
        if (searchTop) {
            if (buttonsTop) {
                // Both at top: buttons (top), search bar (middle), slots (bottom)
                // Search bar is middle, so it's between buttons and slots
                int buttonsBottom = availableTop + BOTTOM_BAR_HEIGHT;
                int slotsTop = availableBottom - (rows * SLOT_SIZE);
                return buttonsBottom + (slotsTop - buttonsBottom - SEARCH_BAR_HEIGHT) / 2;
            } else {
                // Search bar (top), slots (middle), buttons (bottom)
                // Search bar goes all the way up
                return availableTop;
            }
        } else {
            if (buttonsTop) {
                // Buttons (top), slots (middle), search bar (bottom)
                // Search bar goes all the way down
                return availableBottom - SEARCH_BAR_HEIGHT;
            } else {
                // Both at bottom: slots (top), buttons (middle), search bar (bottom)
                // Search bar goes all the way down
                return availableBottom - SEARCH_BAR_HEIGHT;
            }
        }
    }

    private static int bottomBarY(AbstractContainerScreen<?> cs) {
        boolean topButtons = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.buttonsPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean searchTop = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.searchBarPos() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.LayoutPos.TOP;
        boolean fullSide = net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.fillMode() == net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.FillMode.FULL_SIDE;
        
        if (fullSide) {
            // Full size mode - use same logic as container size but fill whole right side
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int bgTop = PANEL_MARGIN;
            int bgBottom = screenHeight - PANEL_MARGIN;
            
            // Calculate available space within background padding
            int availableTop = bgTop + PANEL_MARGIN;
            int availableBottom = bgBottom - PANEL_MARGIN;
            int availableHeight = availableBottom - availableTop;
            
            // Calculate total height needed for all elements
            int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
            int remainingSpace = availableHeight - totalElementHeight;
            
            // Distribute remaining space as gaps (2 gaps between 3 elements)
            int gap = Math.max(4, remainingSpace / 2);
            
            if (topButtons) {
                if (searchTop) {
                    // Both at top: buttons (top), search bar (middle), slots (bottom)
                    // Buttons go all the way up, move down by 1px
                    return availableTop + 1;
                } else {
                    // Buttons (top), slots (middle), search bar (bottom)
                    // Buttons go all the way up, move down by 1px
                    return availableTop + 1;
                }
            } else {
                if (searchTop) {
                    // Search bar (top), slots (middle), buttons (bottom)
                    // Buttons go all the way down, move down by 3px
                    return availableBottom - BOTTOM_BAR_HEIGHT + 3;
                } else {
                    // Both at bottom: slots (top), buttons (middle), search bar (bottom)
                    // Buttons are middle, so center them between slots and search bar, move down by 3px
                    int slotsBottom = availableTop + (rows * SLOT_SIZE);
                    int searchBarTop = availableBottom - SEARCH_BAR_HEIGHT;
                    return slotsBottom + (searchBarTop - slotsBottom - BOTTOM_BAR_HEIGHT) / 2 + 3;
                }
            }
        } else {
            // Container size mode - respect padding and dynamically center
            int containerTop = getTop(cs);
            int containerBottom = getTop(cs) + getImageHeight(cs) - 1; // Account for background reduction
            int bgTop = containerTop;
            int bgBottom = containerBottom;
            
            // Calculate available space within background padding
            int availableTop = bgTop + PANEL_MARGIN;
            int availableBottom = bgBottom - PANEL_MARGIN;
            int availableHeight = availableBottom - availableTop;
            
            // Calculate total height needed for all elements
            int totalElementHeight = SEARCH_BAR_HEIGHT + BOTTOM_BAR_HEIGHT + (rows * SLOT_SIZE);
            int remainingSpace = availableHeight - totalElementHeight;
            
            // Distribute remaining space as gaps (2 gaps between 3 elements)
            int gap = Math.max(4, remainingSpace / 2);
            
            if (topButtons) {
                if (searchTop) {
                    // Both at top: buttons (top), search bar (middle), slots (bottom)
                    // Buttons go all the way up, move down by 1px
                    return availableTop + 1;
                } else {
                    // Buttons (top), slots (middle), search bar (bottom)
                    // Buttons go all the way up, move down by 1px
                    return availableTop + 1;
                }
            } else {
                if (searchTop) {
                    // Search bar (top), slots (middle), buttons (bottom)
                    // Buttons go all the way down, move down by 3px
                    return availableBottom - BOTTOM_BAR_HEIGHT + 3;
                } else {
                    // Both at bottom: slots (top), buttons (middle), search bar (bottom)
                    // Buttons are middle, so center them between slots and search bar, move down by 3px
                    int slotsBottom = availableTop + (rows * SLOT_SIZE);
                    int searchBarTop = availableBottom - SEARCH_BAR_HEIGHT;
                    return slotsBottom + (searchBarTop - slotsBottom - BOTTOM_BAR_HEIGHT) / 2 + 3;
                }
            }
        }
    }

    private static void drawIconButton(GuiGraphics g, int x, int y, int w, int h, net.minecraft.resources.ResourceLocation tex) {
        g.blit(net.minecraft.client.renderer.RenderType::guiTextured, tex, x, y, 0, 0, w, h, w, h);
    }

    private static int totalPages() {
        int size = ItemIndex.size(filter);
        int per = Math.max(1, columns * rows);
        return Math.max(1, (size + per - 1) / per);
    }

    private static void nextPage() { 
        int total = totalPages();
        if (pageIndex >= total - 1) {
            pageIndex = 0; // Wrap to first page
        } else {
            pageIndex++;
        }
    }
    private static void prevPage() { 
        int total = totalPages();
        if (pageIndex <= 0) {
            pageIndex = total - 1; // Wrap to last page
        } else {
            pageIndex--;
        }
    }

    private static ItemStack hoveredStack(AbstractContainerScreen<?> cs) {
        int mx = currentMouseX();
        int my = currentMouseY();
        int gridWidth = columns * SLOT_SIZE;
        int gridLeft = panelLeft + (panelWidth - gridWidth) / 2; // Center the grid
        int w = columns * SLOT_SIZE;
        int h = rows * SLOT_SIZE;
        if (mx < gridLeft + 1 || my < panelTop || mx >= gridLeft + 1 + w || my >= panelTop + h) return ItemStack.EMPTY;
        int cx = (mx - (gridLeft + 1)) / SLOT_SIZE;
        int cy = (my - panelTop) / SLOT_SIZE;
        int idx = cy * columns + cx;
        if (idx < 0 || idx >= visible.size()) return ItemStack.EMPTY;
        return visible.get(idx);
    }

    private static int currentMouseX() {
        var mc = Minecraft.getInstance();
        return (int)(mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getWidth());
    }
    private static int currentMouseY() {
        var mc = Minecraft.getInstance();
        return (int)(mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getHeight());
    }


    private static List<Component> getTooltipWithModName(AbstractContainerScreen<?> cs, ItemStack stack) {
        List<Component> lines;
        try {
            lines = net.minecraft.client.gui.screens.Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
        } catch (Throwable t) {
            lines = new ArrayList<>();
            lines.add(stack.getHoverName());
        }
        String mod = resolveModName(stack);
        if (!net.lyivx.ls_tweaks.recipeview.config.RecipeViewerConfigProvider.tooltipPreferModName()) {
            // Force mod id instead of display name
            try {
                var opt = stack.getItemHolder().unwrapKey();
                if (opt.isPresent()) {
                    var rl = opt.get().location();
                    mod = rl.getNamespace();
                }
            } catch (Throwable ignored) {}
        } else {
            // Prefer display name via ItemIndex cache helper
            try {
                var opt = stack.getItemHolder().unwrapKey();
                if (opt.isPresent()) {
                    var rl = opt.get().location();
                    mod = net.lyivx.ls_tweaks.recipes.index.ItemIndex.getModDisplayName(rl.getNamespace());
                }
            } catch (Throwable ignored) {}
        }
        lines.add(Component.empty()); // Blank line before mod name
        lines.add(Component.literal(mod).withStyle(s -> s.withColor(0x808080)));
        return lines;
    }

    private static String resolveModName(ItemStack stack) {
        try {
            var opt = stack.getItemHolder().unwrapKey();
            if (opt.isPresent()) {
                var rl = opt.get().location();
                String ns = rl.getNamespace();
                // Try Fabric Loader
                try {
                    Class<?> fl = Class.forName("net.fabricmc.loader.api.FabricLoader");
                    Object inst = fl.getMethod("getInstance").invoke(null);
                    Object cont = fl.getMethod("getModContainer", String.class).invoke(inst, ns);
                    if (cont instanceof java.util.Optional<?> o) {
                        Object c = o.orElse(null);
                        if (c != null) {
                            Object meta = c.getClass().getMethod("getMetadata").invoke(c);
                            Object name = meta.getClass().getMethod("getName").invoke(meta);
                            if (name instanceof String s && !s.isBlank()) return s;
                        }
                    }
                } catch (Throwable ignored) {}
                // Try NeoForge ModList
                try {
                    Class<?> ml = Class.forName("net.neoforged.fml.ModList");
                    Object inst = ml.getMethod("get").invoke(null);
                    Object optC = ml.getMethod("getModContainerById", String.class).invoke(inst, ns);
                    if (optC instanceof java.util.Optional<?> o) {
                        Object c = o.orElse(null);
                        if (c != null) {
                            Object info = c.getClass().getMethod("getModInfo").invoke(c);
                            Object name = info.getClass().getMethod("getDisplayName").invoke(info);
                            if (name instanceof String s && !s.isBlank()) return s;
                        }
                    }
                } catch (Throwable ignored) {}
                if ("minecraft".equals(ns)) return "Minecraft";
                return ns;
            }
        } catch (Throwable ignored) {}
        return "Minecraft";
    }

    private ItemPanelOverlay() {}
}


