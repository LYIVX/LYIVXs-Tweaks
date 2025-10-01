package net.lyivx.ls_tweaks.client.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.recipes.RecipeBrowser;
import net.lyivx.ls_tweaks.recipes.index.RecipeIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Enhanced recipe browser screen with categories, pagination, and multiple recipes */
public final class RecipeBrowserScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private final Map<String, List<RecipeDisplay>> recipesByCategory = new HashMap<>();
    private final List<String> categoryOrder = new ArrayList<>();
    private int currentCategoryIndex = 0;
    private int currentPage = 0;
    private int recipesPerPage = 4; // Will be calculated based on available space
    
    // Padding constants
    private static final int MAIN_PADDING = 5; // Padding around the main background
    private static final int RECIPE_PADDING = 6; // Padding inside each recipe background
    private static final int RECIPE_GAP = 5; // Gap between recipes
    
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final ResourceLocation TAB_TOP_SEL = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/creative_inventory/tab_top_selected_2.png");
    private static final ResourceLocation TAB_TOP_UNSEL = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/container/creative_inventory/tab_top_unselected_2.png");
    private static final ResourceLocation TAB_LEFT_SEL = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/recipe_book/tab_selected.png");

    private RecipeBrowserScreen(Map<String, List<RecipeDisplay>> recipesByCategory, List<String> categoryOrder) {
        super(new RecipeBrowserMenu(0, Minecraft.getInstance().player.getInventory()), 
              Minecraft.getInstance().player.getInventory(), 
              Component.translatable("ls_tweaks.recipe_browser.title"));
        this.recipesByCategory.putAll(recipesByCategory);
        this.categoryOrder.addAll(categoryOrder);
    }

    public static void openForResult(ItemStack result) {
        RecipeIndex.ensureBuilt();
        System.out.println("[LS Tweaks][RV] openForResult " + result.getHoverName().getString());
        
        Map<String, List<RecipeDisplay>> recipesByCategory = new HashMap<>();
        List<String> categoryOrder = new ArrayList<>();
        
        for (RecipeHolder<?> rh : RecipeIndex.recipesFor(result)) {
            var typeId = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.typeIdOf(rh.value());
            RecipeCategory<?> cat = typeId == null ? null : RecipeBrowser.CATEGORIES.get(typeId);
            if (cat != null) {
                RecipeDisplay d = buildDisplay(cat, rh);
                if (d != null) {
                    String categoryKey = typeId.toString();
                    recipesByCategory.computeIfAbsent(categoryKey, k -> new ArrayList<>()).add(d);
                    if (!categoryOrder.contains(categoryKey)) {
                        categoryOrder.add(categoryKey);
                    }
                }
            }
        }
        
        if (recipesByCategory.isEmpty()) {
            // Fallback: JSON recipe index
            // Removed JsonRecipeIndex fallback - using working recipe system only
            var fallbackDisplays = java.util.List.<net.lyivx.ls_tweaks.api.recipes.RecipeDisplay>of();
            if (!fallbackDisplays.isEmpty()) {
                recipesByCategory.put("crafting", fallbackDisplays);
                categoryOrder.add("crafting");
            }
        }
        
        // Sort categories: crafting first, then furnaces, then mod categories
        sortCategories(categoryOrder);
        
        System.out.println("[LS Tweaks][RV] result displays found=" + recipesByCategory.values().stream().mapToInt(List::size).sum());
        if (!recipesByCategory.isEmpty()) {
            Minecraft.getInstance().setScreen(new RecipeBrowserScreen(recipesByCategory, categoryOrder));
        } else {
            System.out.println("[LS Tweaks][RV] No displays found for result " + result);
        }
    }

    public static void openForUsage(ItemStack ingredient) {
        RecipeIndex.ensureBuilt();
        System.out.println("[LS Tweaks][RV] openForUsage " + ingredient.getHoverName().getString());
        
        Map<String, List<RecipeDisplay>> recipesByCategory = new HashMap<>();
        List<String> categoryOrder = new ArrayList<>();
        
        List<RecipeHolder<?>> usages = RecipeIndex.usagesFor(ingredient);
        System.out.println("[LS Tweaks][RV] Found " + usages.size() + " usage recipes for " + ingredient.getHoverName().getString());
        
        for (RecipeHolder<?> rh : usages) {
            var typeId = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.typeIdOf(rh.value());
            RecipeCategory<?> cat = typeId == null ? null : RecipeBrowser.CATEGORIES.get(typeId);
            if (cat != null) {
                RecipeDisplay d = buildDisplay(cat, rh);
                if (d != null) {
                    String categoryKey = typeId.toString();
                    recipesByCategory.computeIfAbsent(categoryKey, k -> new ArrayList<>()).add(d);
                    if (!categoryOrder.contains(categoryKey)) {
                        categoryOrder.add(categoryKey);
                    }
                }
            }
        }
        
        if (recipesByCategory.isEmpty()) {
            // Removed JsonRecipeIndex fallback - using working recipe system only
            var fallbackDisplays = java.util.List.<net.lyivx.ls_tweaks.api.recipes.RecipeDisplay>of();
            if (!fallbackDisplays.isEmpty()) {
                recipesByCategory.put("crafting", fallbackDisplays);
                categoryOrder.add("crafting");
            }
        }
        
        // Sort categories: crafting first, then furnaces, then mod categories
        sortCategories(categoryOrder);
        
        System.out.println("[LS Tweaks][RV] usage displays found=" + recipesByCategory.values().stream().mapToInt(List::size).sum());
        if (!recipesByCategory.isEmpty()) {
            Minecraft.getInstance().setScreen(new RecipeBrowserScreen(recipesByCategory, categoryOrder));
        } else {
            System.out.println("[LS Tweaks][RV] No displays found for usage " + ingredient);
        }
    }

    private static void sortCategories(List<String> categoryOrder) {
        // Sort by explicit sortOrder on registered categories; fallback to id
        Map<String, Integer> idToOrder = new HashMap<>();
        for (var cat : net.lyivx.ls_tweaks.recipes.RecipeBrowser.getAllCategories()) {
            idToOrder.put(cat.id().toString(), cat.sortOrder());
        }
        categoryOrder.sort((a, b) -> {
            int oa = idToOrder.getOrDefault(a, Integer.MAX_VALUE);
            int ob = idToOrder.getOrDefault(b, Integer.MAX_VALUE);
            if (oa != ob) return Integer.compare(oa, ob);
            return a.compareTo(b);
        });
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private static RecipeDisplay buildDisplay(RecipeCategory cat, RecipeHolder<?> rh) {
        try {
            return (RecipeDisplay)cat.buildDisplay(rh);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Background is handled in render method
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle item panel overlay clicks
        if (net.lyivx.ls_tweaks.client.overlay.ItemPanelOverlay.mouseClicked(this, mouseX, mouseY, button)) {
            return true;
        }
        lastClickX = mouseX; lastClickY = mouseY; lastClickBtn = button;
        
        // Handle category title clicks and top-page tab buttons
        if (!categoryOrder.isEmpty()) {
            int bw = Math.min(this.width - 40, 180);
            int bh = Math.min(this.height - 40, 256);
            int bx = (this.width - bw) / 2;
            int by = (this.height - bh) / 2;
            
            String currentCategory = categoryOrder.get(currentCategoryIndex);
            RecipeCategory<?> categoryObj = getCategoryForId(currentCategory);
            if (categoryObj != null) {
                Component categoryTitle = Component.translatable(categoryObj.titleTranslationKey());
                int titleX = bx + bw / 2;
                int titleY = by + 10;
                int titleWidth = this.font.width(categoryTitle);
                int titleLeft = titleX - titleWidth / 2;
                int titleRight = titleX + titleWidth / 2;

                // Top bar button bounds
                int btnWTop = 18, btnHTop = 18;
                int barYTop = titleY - 4;
                int prevXTop = bx + 5;
                int nextXTop = bx + bw - btnWTop - 5;
                if (button == 0) {
                    if (mouseX >= prevXTop && mouseX < prevXTop + btnWTop && mouseY >= barYTop && mouseY < barYTop + btnHTop) {
                        currentCategoryIndex = (currentCategoryIndex - 1 + categoryOrder.size()) % categoryOrder.size();
                        currentPage = 0;
                        return true;
                    }
                    if (mouseX >= nextXTop && mouseX < nextXTop + btnWTop && mouseY >= barYTop && mouseY < barYTop + btnHTop) {
                        currentCategoryIndex = (currentCategoryIndex + 1) % categoryOrder.size();
                        currentPage = 0;
                        return true;
                    }
                }
                
                if (button == 0 && mouseX >= titleLeft && mouseX <= titleRight && 
                    mouseY >= titleY - 2 && mouseY <= titleY + 10) {
                    // Clicked on category title - show all recipes in this category
                    openCategoryView(currentCategory);
                    return true;
                }
            }
        }
        
        // Handle category tab clicks - always handle clicks
        if (!categoryOrder.isEmpty()) {
            int tabWidth = 26;
            int tabHeight = 32;
            int bw = Math.min(this.width - 40, 180);
            int bh = Math.min(this.height - 40, 256);
            int bx = (this.width - bw) / 2;
            int by = (this.height - bh) / 2;
            int tabY = by - 28;
            int maxTabsVisible = (bw - 10) / tabWidth;
            int tabPage = currentCategoryIndex / maxTabsVisible;
            int startTabIndex = tabPage * maxTabsVisible;
            int endTabIndex = Math.min(startTabIndex + maxTabsVisible, categoryOrder.size());
            int startX = bx + 5;
            
            // Check tab clicks
            for (int i = startTabIndex; i < endTabIndex; i++) {
                int tabX = startX + (i - startTabIndex) * tabWidth;
                boolean selected = i == currentCategoryIndex;
                int tabRenderY = selected ? tabY : tabY - 4; // Move unselected tabs up by 3px
                if (mouseX >= tabX && mouseX < tabX + tabWidth && 
                    mouseY >= tabRenderY && mouseY < tabRenderY + tabHeight) {
                    currentCategoryIndex = i;
                    currentPage = 0; // Reset page when switching categories
                    return true;
                }
            }
            
            // Check tab pagination button clicks - only if there are too many tabs
            int totalTabPages = Math.max(1, (categoryOrder.size() + maxTabsVisible - 1) / maxTabsVisible);
            if (totalTabPages > 1) {
                int buttonY = tabY + tabHeight + 2;
                int btnW = 18, btnH = 18;
                
                // Previous tab page button
                if (mouseX >= startX && mouseX < startX + btnW && 
                    mouseY >= buttonY && mouseY < buttonY + btnH) {
                    // Wrap-around: if on first page, go to last page
                    if (tabPage <= 0) {
                        currentCategoryIndex = Math.max(0, (totalTabPages - 1) * maxTabsVisible);
                    } else {
                        currentCategoryIndex = Math.max(0, (tabPage - 1) * maxTabsVisible);
                    }
                    currentPage = 0;
                    return true;
                }
                
                // Next tab page button
                int nextX = startX + (endTabIndex - startTabIndex) * tabWidth - btnW;
                if (mouseX >= nextX && mouseX < nextX + btnW && 
                    mouseY >= buttonY && mouseY < buttonY + btnH) {
                    // Wrap-around: if on last page, go to first page
                    if (tabPage >= totalTabPages - 1) {
                        currentCategoryIndex = 0;
                    } else {
                        currentCategoryIndex = Math.min(categoryOrder.size() - 1, (tabPage + 1) * maxTabsVisible);
                    }
                    currentPage = 0;
                    return true;
                }
            }
        }
        
        // Handle pagination clicks (same style as item panel) - only handle if multiple pages
        String currentCategory = categoryOrder.get(currentCategoryIndex);
        List<RecipeDisplay> currentRecipes = recipesByCategory.get(currentCategory);
        int totalPages = Math.max(1, (currentRecipes.size() + recipesPerPage - 1) / recipesPerPage);
        
        if (totalPages > 1) {
            int bw = Math.min(this.width - 40, 180);
            int bh = Math.min(this.height - 40, 256);
            int bx = (this.width - bw) / 2;
            int by = (this.height - bh) / 2;
                int btnW = 18, btnH = 18;
            int barY = by + bh - 23; // Move up by 3px
            int prevX = bx + 5;
            int nextX = bx + bw - btnW - 5;
            
            if (mouseX >= prevX && mouseX < prevX + btnW && 
                mouseY >= barY && mouseY < barY + btnH) {
                // Wrap-around: if on first page, go to last page
                if (currentPage <= 0) {
                    currentPage = totalPages - 1;
                } else {
                    currentPage--;
                }
                return true;
            }
            
            if (mouseX >= nextX && mouseX < nextX + btnW && 
                mouseY >= barY && mouseY < barY + btnH) {
                // Wrap-around: if on last page, go to first page
                if (currentPage >= totalPages - 1) {
                    currentPage = 0;
                } else {
                    currentPage++;
                }
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private double lastClickX, lastClickY; private int lastClickBtn;
    private boolean buttonJustPressedLeft(double mx, double my, int x, int y, int w, int h) {
        return lastClickBtn == 0 && mx >= x && mx < x + w && my >= y && my < y + h;
    }
    
    
    private RecipeCategory<?> getCategoryForId(String categoryId) {
        for (RecipeCategory<?> cat : RecipeBrowser.getAllCategories()) {
            if (cat.id().toString().equals(categoryId)) {
                return cat;
            }
        }
        return null;
    }
    
    private void openCategoryView(String categoryId) {
        // Load ALL recipes for this category from the recipe index
        RecipeIndex.ensureBuilt();
        
        Map<String, List<RecipeDisplay>> singleCategory = new HashMap<>();
        List<String> singleCategoryOrder = new ArrayList<>();
        List<RecipeDisplay> allCategoryRecipes = new ArrayList<>();
        
        // Get the category object to find recipes
        RecipeCategory<?> categoryObj = getCategoryForId(categoryId);
        if (categoryObj != null) {
            // Find all recipes of this type
            for (RecipeHolder<?> rh : RecipeIndex.getAllRecipes()) {
                var typeId = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.typeIdOf(rh.value());
                if (typeId != null && typeId.toString().equals(categoryId)) {
                    RecipeDisplay display = buildDisplay(categoryObj, rh);
                    if (display != null) {
                        allCategoryRecipes.add(display);
                    }
                }
            }
        }
        
        if (!allCategoryRecipes.isEmpty()) {
            singleCategory.put(categoryId, allCategoryRecipes);
            singleCategoryOrder.add(categoryId);
            
            // Open new recipe browser screen with all recipes
            Minecraft.getInstance().setScreen(new RecipeBrowserScreen(singleCategory, singleCategoryOrder));
        }
    }
    

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Do not blur background; draw a subtle dim layer
        g.fill(0, 0, this.width, this.height, 0x60000000);

        // Draw main background (revert to original size)
        int bw = Math.min(this.width - 40, 180);
        int bh = Math.min(this.height - 40, 256);
        int bx = (this.width - bw) / 2;
        int by = (this.height - bh) / 2;
        g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, BG, bx, by, bw, bh);
        
        // Draw category title at the top (clickable)
        if (!categoryOrder.isEmpty()) {
            String currentCategory = categoryOrder.get(currentCategoryIndex);
            RecipeCategory<?> categoryObj = getCategoryForId(currentCategory);
            if (categoryObj != null) {
                Component categoryTitle = Component.translatable(categoryObj.titleTranslationKey());
                int titleX = bx + bw / 2;
                int titleY = by + 9;
                int titleWidth = this.font.width(categoryTitle);
                int titleLeft = titleX - titleWidth / 2;
                int titleRight = titleX + titleWidth / 2;
                
                // Check if mouse is hovering over title
                boolean titleHover = mouseX >= titleLeft && mouseX <= titleRight && 
                                   mouseY >= titleY - 2 && mouseY <= titleY + 10;
                
                // Top bar (category title + tab page buttons) styled like other page bars
                int btnWTop = 18, btnHTop = 18;
                int barYTop = titleY - 4; // align with 18px buttons
                int prevXTop = bx + 5;
                int nextXTop = bx + bw - btnWTop - 5;
                // Background box between buttons
                int boxLTop = prevXTop + btnWTop - 2;
                int boxRTop = nextXTop + 2;
                if (boxRTop > boxLTop) {
                    g.fill(boxLTop, barYTop, boxRTop, barYTop + btnHTop, 0xFF8B8B8B);
                }
                // Draw tab page buttons (always visible)
                boolean tabPrevHoverTop = mouseX >= prevXTop && mouseX < prevXTop + btnWTop && mouseY >= barYTop && mouseY < barYTop + btnHTop;
                boolean tabNextHoverTop = mouseX >= nextXTop && mouseX < nextXTop + btnWTop && mouseY >= barYTop && mouseY < barYTop + btnHTop;
                net.minecraft.resources.ResourceLocation tabPrevTexTop = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", tabPrevHoverTop ? "textures/gui/recipe_viewer/widgets/left_button_highlighted.png" : "textures/gui/recipe_viewer/widgets/left_button.png");
                net.minecraft.resources.ResourceLocation tabNextTexTop = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_tweaks", tabNextHoverTop ? "textures/gui/recipe_viewer/widgets/right_button_highlighted.png" : "textures/gui/recipe_viewer/widgets/right_button.png");
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, tabPrevTexTop, prevXTop, barYTop, 0, 0, btnWTop, btnHTop, btnWTop, btnHTop);
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, tabNextTexTop, nextXTop, barYTop, 0, 0, btnWTop, btnHTop, btnWTop, btnHTop);

                // Note: actual switching handled in mouseClicked; no hover switching here
                if (false && buttonJustPressedLeft(mouseX, mouseY, prevXTop, barYTop, btnWTop, btnHTop)) {
                    currentCategoryIndex = (currentCategoryIndex - 1 + categoryOrder.size()) % categoryOrder.size();
                    currentPage = 0;
                } else if (false && buttonJustPressedLeft(mouseX, mouseY, nextXTop, barYTop, btnWTop, btnHTop)) {
                    currentCategoryIndex = (currentCategoryIndex + 1) % categoryOrder.size();
                    currentPage = 0;
                }

                // Handle clicks on tab page buttons to switch to previous/next tab (not page)
                if (net.minecraft.client.Minecraft.getInstance().mouseHandler.isMouseGrabbed()) {
                    // no-op when grabbed
                }

                // Draw title with hover effect
                int titleColor = titleHover ? 0xFFFF00 : 0xFFFFFF; // Yellow when hovered
                g.drawCenteredString(this.font, categoryTitle, titleX, titleY + 1, titleColor);
                
                // (underline removed)
            }
        }
        

        // Draw category tabs (positioned relative to background) - always show tabs
        if (!categoryOrder.isEmpty()) {
            int tabWidth = 26;
            int tabHeight = 32;
            int tabY = by - 28; // Move down by a few pixels from original
            int maxTabsVisible = (bw - 10) / tabWidth; // How many tabs fit in background width
            int tabPage = currentCategoryIndex / maxTabsVisible;
            int startTabIndex = tabPage * maxTabsVisible;
            int endTabIndex = Math.min(startTabIndex + maxTabsVisible, categoryOrder.size());
            
            // Calculate starting X position (left-aligned within background)
            int startX = bx + 5; // Start from left edge of background with small margin
            
            // Draw visible tabs
            for (int i = startTabIndex; i < endTabIndex; i++) {
                String category = categoryOrder.get(i);
                int tabX = startX + (i - startTabIndex) * tabWidth;
                boolean selected = i == currentCategoryIndex;
                
                // Draw tab background
                int tabRenderY = selected ? tabY : tabY - 4; // Move unselected tabs up by 3px
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, 
                    selected ? TAB_TOP_SEL : TAB_TOP_UNSEL, 
                    tabX, tabRenderY, 0, 0, tabWidth, tabHeight, tabWidth, tabHeight);
                
                // Draw category icon (item/block preferred, fallback to text)
                RecipeCategory<?> categoryObj = getCategoryForId(category);
                if (categoryObj != null && !categoryObj.iconItem().isEmpty()) {
                    // Draw item icon
                    g.renderFakeItem(categoryObj.iconItem(), tabX + 5, tabRenderY + 8);
                } else {
                    // Fallback to text
                    g.drawCenteredString(this.font, category.substring(0, 1).toUpperCase(), 
                        tabX + tabWidth/2, tabRenderY + 8, 0xFFFFFF);
                }
            }
            
            // Tab page buttons are now rendered near the title bar above; skip bottom buttons
            
            // Draw hover tooltips for category tabs - always show tooltips
            if (!categoryOrder.isEmpty()) {
                
                for (int i = startTabIndex; i < endTabIndex; i++) {
                    String category = categoryOrder.get(i);
                    int tabX = startX + (i - startTabIndex) * tabWidth;
                    boolean selected = i == currentCategoryIndex;
                    int tabRenderY = selected ? tabY : tabY - 3; // Move unselected tabs up by 3px
                    
                    if (mouseX >= tabX && mouseX < tabX + tabWidth && 
                        mouseY >= tabRenderY && mouseY < tabRenderY + tabHeight) {
                        RecipeCategory<?> categoryObj = getCategoryForId(category);
                        if (categoryObj != null) {
                            List<Component> tooltip = new ArrayList<>();
                            tooltip.add(Component.translatable(categoryObj.titleTranslationKey()));
                            g.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                        }
                    }
                }
            }
        }

        // Draw recipes for current category and page
        String currentCategory = categoryOrder.get(currentCategoryIndex);
        List<RecipeDisplay> currentRecipes = recipesByCategory.get(currentCategory);
        
        if (currentRecipes.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("ls_tweaks.recipe_browser.empty").getString(), 
                this.width / 2, this.height / 2, 0xFFFFFF);
        } else {
            // Calculate available space for recipes
            int availableWidth = bw - (MAIN_PADDING * 2);
            int availableHeight = bh - (MAIN_PADDING * 2) - 40; // Leave space for tabs
            
            // Get the first recipe to determine dimensions
            RecipeDisplay firstRecipe = currentRecipes.get(0);
            int recipeWidth = firstRecipe.contentWidth() + (RECIPE_PADDING * 2);
            int recipeHeight = firstRecipe.contentHeight() + (RECIPE_PADDING * 2);
            
            // Calculate how many recipes fit vertically
            recipesPerPage = Math.max(1, (availableHeight + RECIPE_GAP) / (recipeHeight + RECIPE_GAP));
            
            int startIndex = currentPage * recipesPerPage;
            int endIndex = Math.min(startIndex + recipesPerPage, currentRecipes.size());
            
            // Start recipes from the top of the background
            int startY = by + MAIN_PADDING + 20; // Leave space for category title
            
            for (int i = startIndex; i < endIndex; i++) {
                RecipeDisplay recipe = currentRecipes.get(i);
                int recipeIndex = i - startIndex;
                
                // Center recipe horizontally and position vertically
                int recipeX = bx + MAIN_PADDING + (availableWidth - recipeWidth) / 2;
                int recipeY = startY + recipeIndex * (recipeHeight + RECIPE_GAP);
                
                // Draw recipe background using the same texture as the main background
                g.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, BG, recipeX, recipeY, recipeWidth, recipeHeight);
                
                // Calculate content area with padding
                int contentX = recipeX + RECIPE_PADDING;
                int contentY = recipeY + RECIPE_PADDING;
                
                // Render slots using widget rendering
                for (var slot : recipe.slots()) {
                    slot.render(g, this.font, contentX, contentY, mouseX, mouseY);
                }
                
                // Render textures using widget rendering
                for (var texture : recipe.textures()) {
                    texture.render(g, contentX, contentY);
                }
                
                // Render text widgets using widget rendering
                for (var textWidget : recipe.textWidgets()) {
                    textWidget.render(g, this.font, contentX, contentY);
                }
                // Render hover tooltip widgets
                if (recipe.hoverWidgets() != null && !recipe.hoverWidgets().isEmpty()) {
                    for (var hover : recipe.hoverWidgets()) {
                        hover.render(g, this.font, contentX, contentY, mouseX, mouseY);
                    }
                }
            }
            
            // Draw pagination (same style as item panel) - only show if multiple pages
            int totalPages = Math.max(1, (currentRecipes.size() + recipesPerPage - 1) / recipesPerPage);
            if (totalPages > 1) {
                int btnW = 18, btnH = 18;
                int barY = by + bh - 23; // Move up by 3px
                int prevX = bx + 5;
                int nextX = bx + bw - btnW - 5;
                
                // Box between page buttons
                int boxL2 = prevX + btnW - 2;
                int boxR2 = nextX + 2;
                if (boxR2 > boxL2) {
                    g.fill(boxL2, barY, boxR2, barY + btnH, 0xFF8B8B8B);
                }
                
                // Check for hover
                boolean prevHover = mouseX >= prevX && mouseX < prevX + btnW && mouseY >= barY && mouseY < barY + btnH;
                boolean nextHover = mouseX >= nextX && mouseX < nextX + btnW && mouseY >= barY && mouseY < barY + btnH;
                
                // Previous page button
                ResourceLocation prevTex = prevHover ? 
                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/left_button_highlighted.png") :
                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/left_button.png");
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, prevTex, prevX, barY, 0, 0, btnW, btnH, btnW, btnH);
                
                // Next page button
                ResourceLocation nextTex = nextHover ? 
                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/right_button_highlighted.png") :
                    ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/right_button.png");
                g.blit(net.minecraft.client.renderer.RenderType::guiTextured, nextTex, nextX, barY, 0, 0, btnW, btnH, btnW, btnH);
                
                // Page indicator with shadow
                String pg = (currentPage + 1) + "/" + totalPages;
                int textX = bx + (bw - this.font.width(pg)) / 2;
                g.drawString(this.font, pg, textX, barY + 5, 0xFFFFFFFF, true); // Main text
            }
        }

        // Render the item panel overlay
        net.lyivx.ls_tweaks.client.overlay.ItemPanelOverlay.render(this, g);
    }

    @Override
    public void onClose() {
        var mc = Minecraft.getInstance();
        if (mc.screen != null && mc.screen != this) {
            super.onClose();
            return;
        }
        mc.setScreen(null);
    }

    private long lsTweaks_lastCycleSecond = -1L;
    private boolean lsTweaks_isRebuilding = false;

    @Override
    protected void containerTick() {
        super.containerTick();

        // Ensure we’re in a world
        var mc = this.minecraft;
        if (mc == null || mc.level == null) return;

        // Avoid re-entrant rebuilds
        if (lsTweaks_isRebuilding) return;

        // Advance once per second (20 ticks)
        long sec = mc.level.getGameTime() / 20L;
        if (sec != lsTweaks_lastCycleSecond) {
            lsTweaks_lastCycleSecond = sec;

            // Re-run init to rebuild the current page with fresh stacks
            lsTweaks_isRebuilding = true;
            try {
                // IMPORTANT: keep the same size; this only re-lays out your existing widgets
                this.init(mc, this.width, this.height);
                this.rebuildWidgets();
                System.out.println(lsTweaks_isRebuilding);
                System.out.println(lsTweaks_lastCycleSecond);
            } finally {
                lsTweaks_isRebuilding = false;
            }
        }
    }
}