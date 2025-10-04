package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_tweaks.api.recipes.RecipeBackedCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

/** Simple crafting category for 1.21.4 */
public final class CraftingAnyCategory implements RecipeBackedCategory<CraftingRecipe> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/crafting_table.png");
    private static final ResourceLocation SHAPELESS = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/shapeless.png");

    @Override public ResourceLocation id() { return ID; }
    @Override public String titleTranslationKey() { return "ls_tweaks.category.crafting"; }
    @Override public ResourceLocation iconTexture() { return ICON; }
    @Override public ItemStack iconItem() { return new ItemStack(Items.CRAFTING_TABLE); }
    @Override public int sortOrder() { return 0; }

    @Override public Class<CraftingRecipe> recipeClass() { return CraftingRecipe.class; }
    @Override public RecipeType<CraftingRecipe> recipeType() { return RecipeType.CRAFTING; }

    @Override
    public RecipeDisplay buildDisplay(CraftingRecipe recipe) {
        boolean isShapeless = !(recipe instanceof ShapedRecipe);

        ItemStack out = assembleResult(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();

        // Title
        String title = out.isEmpty() ? "Crafting" : out.getHoverName().getString();
        if (isShapeless) title += " (Shapeless)";
        builder.title(title);

        // Build flattened 3x3 ingredients for per-cell cycling
        java.util.List<Ingredient> ings = new java.util.ArrayList<>(9);
        for (int i = 0; i < 9; i++) ings.add(null);
        if (recipe instanceof ShapedRecipe shaped) {
            int w = shaped.getWidth();
            int h = shaped.getHeight();
            var list = shaped.getIngredients(); // List<Optional<Ingredient>>
            int p = 0;
            for (int ry = 0; ry < h; ry++) {
                for (int rx = 0; rx < w; rx++) {
                    int gx = rx + (3 - w) / 2;
                    int gy = ry + (3 - h) / 2;
                    Ingredient ing = null;
                    if (list != null && p < list.size()) {
                        var opt = list.get(p);
                        if (opt != null && opt.isPresent()) ing = opt.get();
                    }
                    if (ing != null) {
                        int ci = gy * 3 + gx;
                        if (ci >= 0 && ci < 9) ings.set(ci, ing);
                    }
                    p++;
                }
            }
        } else {
            java.util.List<Ingredient> ingredients = java.util.List.of();
            try {
                var info = recipe.placementInfo();
                if (info != null && info.ingredients() != null) ingredients = info.ingredients();
            } catch (Throwable ignored) {}
            for (int i = 0; i < Math.min(9, ingredients.size()); i++) ings.set(i, ingredients.get(i));
        }
        // Use GridSlotWidget per-cell ingredient cycling; inherits speed from template slot
        // Provide ingredients via the template slot; grid/scroll will consume from it
        builder.addSlot(Widgets.AddGrid.items(0, 0, 3, 3, Widgets.AddSlot.input(0, 0, ings, 0)).positionMiddleLeft());

        // Output + arrow
        builder.addSlot(Widgets.AddSlot.output(0, 0, out, 0).positionMiddleRight());
        builder.addTexture(Widgets.AddArrow.Right.progress(57, 20));

        // Shapeless marker
        if (isShapeless) {
            builder.addTexture(Widgets.AddTexture.staticTexture(55, 35, SHAPELESS, 0, 0, 18, 18, 18, 18));
            builder.addHover(Widgets.AddHover.literal(55, 35, 18, 18, "Shapeless"));
        }

        builder.contentSize(108, (3 * 18));
        return builder.build();
    }

    private static ItemStack assembleResult(CraftingRecipe recipe) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var ra = (mc != null && mc.level != null) ? mc.level.registryAccess() : null;
            if (ra == null) return ItemStack.EMPTY;
            net.minecraft.world.item.crafting.CraftingInput input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY));
            return recipe.assemble(input, ra);
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    @Override public int contentWidth() { return 128; }
    @Override public int contentHeight() { return 3 * 18; }
}