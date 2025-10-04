package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/** Category that shows all tags an item belongs to. */
public final class TagsCategory implements RecipeCategory<Recipe<?>> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "tags");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/name_tag.png");

    @Override public ResourceLocation id() { return ID; }
    @Override public String titleTranslationKey() { return "ls_tweaks.category.tags"; }
    @Override public ResourceLocation iconTexture() { return ICON; }
    @Override public ItemStack iconItem() { return new ItemStack(Items.NAME_TAG); }
    @Override public int sortOrder() { return 50; }

    // Not used directly
    @Override
    public RecipeDisplay buildDisplay(Recipe<?> recipe) { return null; }

    /** Builds a tag list display for the given item. */
    public RecipeDisplay buildDisplayForItem(ItemStack stack) {
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        return builder.build();
    }

    /** Builds a display for a single tag id, listing all items in that tag. */
    public RecipeDisplay buildDisplayForTag(ResourceLocation tagId) {
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        int columns = 9;
        var items = net.lyivx.ls_tweaks.recipes.util.TagsUtil.itemsInTag(tagId);
        int rows = Math.max(1, (items.size() + columns - 1) / columns);
        int maxVisibleRows = 5;

        // Use grid/scroll with cycling disabled so list sources don't replicate
        var grid = Widgets.AddGrid.items(0, 0, columns, rows, Widgets.AddSlot.input(0, 0, items, 0)).disableListCycling();
        var scroll = Widgets.AddScroll.grid(0, 0, maxVisibleRows, grid).disableListCycling();

        int effCols = (grid instanceof Widgets.AddGrid.GridBuilder gb) ? gb.getEffectiveColumns() : columns;
        int effRows = (grid instanceof Widgets.AddGrid.GridBuilder gb2) ? gb2.getEffectiveRows() : rows;

        builder.addText(Widgets.AddText.component(0, 0, Component.literal(tagId.toString())).positionTopLeft().bounds(contentWidth() + effCols * 18, 10));
        if (rows <= maxVisibleRows) {
            builder.addSlot(grid.positionBottomCenter());
            builder.contentSize(contentWidth() + effCols * 18, contentHeight() + effRows * 18 + 10);
        } else {
            builder.addSlot(scroll.positionBottomCenter());
            builder.contentSize(contentWidth() + effCols * 18, contentHeight() + Math.max(18, maxVisibleRows * 18 + 10));
        }
        return builder.build();
    }

    /** Builds a single combined display showing all tags in a list. */
    public RecipeDisplay buildDisplayForAllTags() {
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        return builder.build();
    }

    @Override public int contentWidth() { return 0; }
    @Override public int contentHeight() { return 2; }
}


