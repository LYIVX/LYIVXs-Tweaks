package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_tweaks.api.recipes.RecipeBackedCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.lyivx.ls_tweaks.recipes.wrappers.CookingRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.lyivx.ls_tweaks.recipes.util.DisplayUtil;

/** Simple cooking category base */
public abstract class CookingCategoryBase implements RecipeBackedCategory<AbstractCookingRecipe> {
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
    public RecipeDisplay buildDisplay(AbstractCookingRecipe recipe) {
        try {
            java.lang.reflect.Constructor<?> c = net.minecraft.world.item.crafting.RecipeHolder.class.getConstructors()[0];
            Object rh = c.newInstance(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unknown", "recipe"), (Recipe<?>) recipe);
            if (rh instanceof net.minecraft.world.item.crafting.RecipeHolder holder) {
                return buildDisplayWithHolder((net.minecraft.world.item.crafting.RecipeHolder<Recipe<?>>) holder);
            }
        } catch (Throwable ignored) {}
        // Fallback: create a simple display without recipe name
        return buildDisplaySimple(recipe);
    }
    
    private RecipeDisplay buildDisplaySimple(AbstractCookingRecipe recipe) {
        ItemStack in = ItemStack.EMPTY;
        int cookingTime = 0;
        float experience = 0.0f;
        
        // Use helper to get input items reliably
        in = CookingRecipeHelper.getFirstInputItem(recipe);
        cookingTime = CookingRecipeHelper.getCookingTime(recipe);
        experience = CookingRecipeHelper.getExperience(recipe);

        ItemStack out = (recipe instanceof AbstractCookingRecipe cooking) ? assembleCookingOutput(cooking) : ItemStack.EMPTY;
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();
        
        // Set title
        String title = out.isEmpty() ? id.toString() : out.getHoverName().getString();
        builder.title(title);
        
        // Add Input
        builder.addSlot(Widgets.AddSlot.input(0, 4, in, 0).positionTopLeft());
        // Add Flame
        builder.addTexture(Widgets.AddFlame.progress(21, 6).positionTopLeft());
        // Add Fuel
        builder.addSlot(Widgets.AddSlot.fuel(-10, 4, 0).positionTopCenter());
        // Add Arrow
        builder.addTexture(Widgets.AddArrow.Right.progress(-30, 5).positionTopRight());
        if (cookingTime > 0) {
            builder.addText(Widgets.AddText.literal(-30, 0, cookingTime + "tk/s").centered().bounds(22, 6).positionBottomRight());
        }
        builder.addHover(Widgets.AddHover.literal(-30, 0, 22, 6, "Cooking Time").positionBottomRight());

        // Add Output
        builder.addSlot(Widgets.AddSlot.output(0, 0, out, 0).positionTopRight());
        if (experience > 0.0f) {
            builder.addText(Widgets.AddText.literal(0, 0, String.format("%.1f XP", experience)).centered().bounds(26, 6).positionBottomRight().color("lime"));
        }
        builder.addHover(Widgets.AddHover.literal(0, 0, 26, 6, "Given Experience").positionBottomRight());

        // Set content size
        builder.contentSize(111, 34);

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

        ItemStack out = (recipe instanceof AbstractCookingRecipe cooking) ? assembleCookingOutput(cooking) : DisplayUtil.firstOutput(holder);
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

    private static ItemStack assembleCookingOutput(AbstractCookingRecipe recipe) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var ra = (mc != null && mc.level != null) ? mc.level.registryAccess() : null;
            if (ra == null || recipe == null) return ItemStack.EMPTY;
            ItemStack first = recipe.input().items().findFirst().map(h -> new ItemStack(h.value())).orElse(ItemStack.EMPTY);
            var single = new net.minecraft.world.item.crafting.SingleRecipeInput(first);
            return recipe.assemble(single, ra);
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }
}