package net.lyivx.ls_tweaks.recipes.categories;

import net.lyivx.ls_furniture.common.recipes.WorkstationRecipe;
import net.lyivx.ls_tweaks.api.recipes.RecipeBackedCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.lyivx.ls_tweaks.recipes.wrappers.CookingRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.lyivx.ls_tweaks.recipes.util.DisplayUtil;
import net.minecraft.world.item.crafting.RecipeHolder;

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
        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeDisplay buildDisplay(RecipeHolder<? extends Recipe<?>> holder) {
        return buildDisplayWithHolder((RecipeHolder) holder);
    }
    
    public RecipeDisplay buildDisplayWithHolder(RecipeHolder<Recipe<?>> holder) {
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();

        Recipe<?> recipe = holder.value();
        Ingredient in = null;
        int cookingTime = 0;
        float experience = 0.0f;
        if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
            // Use helper to get input items reliably
            in = cookingRecipe.input();
            cookingTime = cookingRecipe.cookingTime();
            experience = cookingRecipe.experience();
        }

        ItemStack out = (recipe instanceof AbstractCookingRecipe cooking) ? assembleCookingOutput(cooking) : DisplayUtil.firstOutput(holder);

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
            builder.addText(Widgets.AddText.component(40, 35, Component.literal(cookingTime + "s")).bounds(20, 10));
        }
        if (experience > 0.0f) {
            builder.addText(Widgets.AddText.component(110, 35, Component.literal(String.format("%.1f XP", experience))).bounds(25, 10).color("lime"));
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