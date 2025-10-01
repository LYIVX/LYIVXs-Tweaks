package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public final class CraftingShapedCategory implements RecipeCategory<ShapedRecipe> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting_shaped");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/crafting_table.png");

    @Override
    public ResourceLocation id() { return ID; }

    @Override
    public String titleTranslationKey() { return "ls_tweaks.category.crafting_shaped"; }

    @Override
    public ResourceLocation iconTexture() { return ICON; }
    
    @Override
    public ItemStack iconItem() { return new ItemStack(Items.CRAFTING_TABLE); }
    @Override public int sortOrder() { return 1; }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeDisplay buildDisplay(ShapedRecipe recipe) {
        try {
            java.lang.reflect.Constructor<?> c = net.minecraft.world.item.crafting.RecipeHolder.class.getConstructors()[0];
            Object rh = c.newInstance(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unknown", "recipe"), recipe);
            if (rh instanceof net.minecraft.world.item.crafting.RecipeHolder holder) {
                return buildDisplayWithHolder((net.minecraft.world.item.crafting.RecipeHolder<Recipe<?>>) holder);
            }
        } catch (Throwable ignored) {}
        // Fallback: create a simple display without recipe name
        return buildDisplaySimple(recipe);
    }
    
    private RecipeDisplay buildDisplaySimple(ShapedRecipe recipe) {
        ItemStack out = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.resultOf(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.getHoverName().getString();
        builder.title(title);
        
        // Add 3x3 crafting grid respecting the actual pattern
        ItemStack[][] pattern = net.lyivx.ls_tweaks.recipes.wrappers.CraftingRecipeHelper.getShapedPatternItems(recipe);
        int index = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = c * 18;
                int y = r * 18;
                ItemStack stack = pattern[r][c];
                builder.addSlot(Widgets.AddSlot.input(x, y, stack, index));
                index++;
            }
        }
        
        // Add output slot without recipe name
        builder.addSlot(Widgets.AddSlot.output(110, 18, out, 0));
        
        // Progress-based right arrow
        builder.addTexture(Widgets.AddArrow.Right.progress(78, 20));
        
        // Set content size
        builder.contentSize(110 + 18, 3 * 18);
        
        return builder.build();
    }
    
    public RecipeDisplay buildDisplayWithHolder(net.minecraft.world.item.crafting.RecipeHolder<Recipe<?>> holder) {
        ShapedRecipe recipe = (ShapedRecipe) holder.value();
        ItemStack out = net.lyivx.ls_tweaks.recipes.util.RecipeUtil.resultOf(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.getHoverName().getString();
        builder.title(title);
        
        // Add 3x3 crafting grid respecting the actual pattern
        ItemStack[][] pattern = net.lyivx.ls_tweaks.recipes.wrappers.CraftingRecipeHelper.getShapedPatternItems(recipe);
        int index = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int x = c * 18;
                int y = r * 18;
                ItemStack stack = pattern[r][c];
                builder.addSlot(Widgets.AddSlot.input(x, y, stack, index));
                index++;
            }
        }
        
        // Add output slot
        builder.addSlot(Widgets.AddSlot.output(110, 18, out, 0));
        
        // Progress-based right arrow
        builder.addTexture(Widgets.AddArrow.Right.progress(78, 20));
        
        // Set content size
        builder.contentSize(110 + 18, 3 * 18);
        
        return builder.build();
    }
}


