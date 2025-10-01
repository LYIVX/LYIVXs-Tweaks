package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.lyivx.ls_tweaks.recipes.util.RecipeUtil;
import net.lyivx.ls_tweaks.recipes.wrappers.CookingRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

/** Simple cooking category base */
public abstract class CookingCategoryBase implements RecipeCategory<Recipe<?>> {
    private final ResourceLocation id;
    private final ResourceLocation icon;

    protected CookingCategoryBase(ResourceLocation id, ResourceLocation icon) {
        this.id = id; 
        this.icon = icon;
    }

    @Override public ResourceLocation id() { return id; }
    @Override public String titleTranslationKey() { return "ls_tweaks.category." + id.getPath(); }
    @Override public ResourceLocation iconTexture() { return icon; }
    @Override public ItemStack iconItem() { return getIconItem(); }
    
    protected abstract ItemStack getIconItem();
    
    @Override public int sortOrder() {
        String path = id.getPath();
        if ("smelting".equals(path) || "furnace".equals(path)) return 3;
        if ("blasting".equals(path)) return 4;
        if ("smoking".equals(path)) return 5;
        return Integer.MAX_VALUE;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeDisplay buildDisplay(Recipe<?> recipe) {
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
    
    private RecipeDisplay buildDisplaySimple(Recipe<?> recipe) {
        ItemStack in = ItemStack.EMPTY;
        int cookingTime = 0;
        float experience = 0.0f;
        
        if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
            // Use helper to get input items reliably
            in = CookingRecipeHelper.getFirstInputItem(cookingRecipe);
            cookingTime = CookingRecipeHelper.getCookingTime(cookingRecipe);
            experience = CookingRecipeHelper.getExperience(cookingRecipe);
        }

        ItemStack out = RecipeUtil.resultOf(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.isEmpty() ? id.toString() : out.getHoverName().getString();
        builder.title(title);
        
        // Add input, fuel, and output slots using fluent API
        builder.addSlot(Widgets.AddSlot.input(0, 4, in, 0));
        builder.addSlot(Widgets.AddSlot.fuel(40, 4, 0));
        builder.addSlot(Widgets.AddSlot.output(92, 4, out, 0));

        // Add flame and arrow as progress (default behavior)
        builder.addTexture(Widgets.AddFlame.progress(21, 5));

        builder.addTexture(Widgets.AddArrow.Right.progress(61, 4));
        // Only show primary right arrow

        // Add labels for cooking time and experience using bounded text widgets
        if (cookingTime > 0) {
            builder.addText(Widgets.AddText.literal(61, 22, cookingTime + "tk/s").centered().bounds(22, 6));
        }
        if (experience > 0.0f) {
            builder.addText(Widgets.AddText.literal(88, 26, String.format("%.1f XP", experience)).centered().bounds(26, 6).color("lime"));
        }

        builder.addHover(Widgets.AddHover.literal(61, 22, 22, 6, "Cooking Time"));

        builder.addHover(Widgets.AddHover.literal(88, 26, 26, 6, "Given Experience"));

        // Set content size
        builder.contentSize(112, 29);

        return builder.build();
    }
    
    public RecipeDisplay buildDisplayWithHolder(net.minecraft.world.item.crafting.RecipeHolder<Recipe<?>> holder) {
        Recipe<?> recipe = holder.value();
        ItemStack in = ItemStack.EMPTY;
        int cookingTime = 0;
        float experience = 0.0f;
        
        if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
            // Use helper to get input items reliably
            in = CookingRecipeHelper.getFirstInputItem(cookingRecipe);
            cookingTime = CookingRecipeHelper.getCookingTime(cookingRecipe);
            experience = CookingRecipeHelper.getExperience(cookingRecipe);
        }

        ItemStack out = RecipeUtil.resultOf(recipe);
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.isEmpty() ? id.toString() : out.getHoverName().getString();
        builder.title(title);
        
        // Add input and output slots
        builder.addSlot(Widgets.AddSlot.input(0, 18, in, 0));
        builder.addSlot(Widgets.AddSlot.output(110, 18, out, 0));
        
        // Progress-based arrow
        builder.addTexture(Widgets.AddArrow.Right.progress(78, 20));
        
        // Progress-based flame
        builder.addTexture(Widgets.AddFlame.progress(40, 20));

        // Add labels for cooking time and experience using bounded text widgets
        if (cookingTime > 0) {
            builder.addText(Widgets.AddText.literal(40, 35, cookingTime + "s").bounds(20, 10));
        }
        if (experience > 0.0f) {
            builder.addText(Widgets.AddText.literal(110, 35, String.format("%.1f XP", experience)).bounds(25, 10).color("lime"));
        }

        // Set content size
        builder.contentSize(150, 66);

        return builder.build();
    }

    @Override public int contentWidth() { return 150; }
    @Override public int contentHeight() { return 66; }
}