package net.lyivx.ls_tweaks.recipes.categories;

import java.util.ArrayList;
import java.util.List;
import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.lyivx.ls_tweaks.recipes.util.RecipeUtil;
import net.lyivx.ls_tweaks.recipes.wrappers.CraftingRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingRecipe;

/** Simple crafting category for 1.21.4 */
public final class CraftingAnyCategory implements RecipeCategory<Recipe<?>> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("minecraft", "crafting");
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/crafting_table.png");
    private static final ResourceLocation SHAPELESS = ResourceLocation.fromNamespaceAndPath("ls_tweaks", "textures/gui/recipe_viewer/widgets/shapeless.png");

    @Override public ResourceLocation id() { return ID; }
    @Override public String titleTranslationKey() { return "ls_tweaks.category.crafting"; }
    @Override public ResourceLocation iconTexture() { return ICON; }
    @Override public ItemStack iconItem() { return new ItemStack(Items.CRAFTING_TABLE); }
    @Override public int sortOrder() { return 0; }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeDisplay buildDisplay(Recipe<?> recipe) {
        List<ItemStack> inputs = new ArrayList<>();
        boolean isShapeless = false;
        
        if (recipe instanceof CraftingRecipe craftingRecipe) {
            // Check if it's a shapeless recipe
            try {
                String className = craftingRecipe.getClass().getSimpleName().toLowerCase();
                isShapeless = className.contains("shapeless");
            } catch (Exception e) {
                // Fallback: assume shaped if we can't determine
            }
            
            // Get ingredients and respect the pattern for shaped recipes
            var ingredients = CraftingRecipeHelper.getIngredients(craftingRecipe);
            
            if (isShapeless) {
                // For shapeless recipes, just fill the first slots with ingredients
                for (int i = 0; i < 9; i++) {
                    if (i < ingredients.size()) {
                        ItemStack item = CraftingRecipeHelper.getFirstIngredientItem(craftingRecipe, i);
                        inputs.add(item);
                    } else {
                        inputs.add(ItemStack.EMPTY);
                    }
                }
            } else {
                // For shaped recipes, respect the actual pattern
                ItemStack[][] pattern = CraftingRecipeHelper.getShapedPatternItems(craftingRecipe);
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        inputs.add(pattern[r][c]);
                    }
                }
            }
        }

        ItemStack out = RecipeUtil.resultOf(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.isEmpty() ? "Crafting" : out.getHoverName().getString();
        if (isShapeless) {
            title += " (Shapeless)";
        }
        builder.title(title);
        
        // Add 3x3 crafting grid
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int index = r * 3 + c;
                int x = c * 18;
                int y = r * 18;
                ItemStack stack = index < inputs.size() ? inputs.get(index) : ItemStack.EMPTY;
                builder.addSlot(Widgets.AddSlot.input(x, y, stack, index));
            }
        }
        
        // Add output slot without recipe name
        builder.addSlot(Widgets.AddSlot.output(88, 18, out, 0));
        
        // Progress-based right arrow
        builder.addTexture(Widgets.AddArrow.Right.progress(57, 18));
        
        // Add shapeless indicator if it's a shapeless recipe
        if (isShapeless) {
            builder.addTexture(Widgets.AddTexture.staticTexture(55, 35, SHAPELESS, 0, 0, 18, 18, 18, 18));
            // Add hover tooltip region over the icon
            builder.addHover(Widgets.AddHover.literal(55, 35, 18, 18, "Shapeless"));
        }

        // Set content size
        builder.contentSize(108, (3 * 18) - 2);

        return builder.build();
    }

    @Override public int contentWidth() { return 110 + 18; }
    @Override public int contentHeight() { return 3 * 18; }
}