package net.lyivx.ls_tweaks.recipes.categories.furnection;

import net.lyivx.ls_furniture.common.recipes.ChoppingBoardRecipe;
import net.lyivx.ls_furniture.registry.ModItems;
import net.lyivx.ls_tweaks.api.recipes.RecipeBackedCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplayBuilder;
import net.lyivx.ls_tweaks.api.recipes.Widgets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import static net.lyivx.ls_furniture.LYIVXsFurnitureMod.MOD_ID;

public class ChoppingBoardCategory implements RecipeBackedCategory<ChoppingBoardRecipe> {
    private final RecipeType<ChoppingBoardRecipe> type;

    protected ChoppingBoardCategory(RecipeType<ChoppingBoardRecipe> type) {
        this.type = type;
    }

    @Override public ResourceLocation id() { return ResourceLocation.fromNamespaceAndPath(MOD_ID, "chopping_board"); }
    @Override public String titleTranslationKey() { return "ls_furniture.category." + id().getPath(); }

    @Override
    public ResourceLocation iconTexture() {
        return null;
    }

    @Override public ItemStack iconItem() { return ModItems.CHOPPING_BOARD.get().getDefaultInstance(); }
    @Override public Class<ChoppingBoardRecipe> recipeClass() { return ChoppingBoardRecipe.class; }
    @Override public RecipeType<ChoppingBoardRecipe> recipeType() { return type; }
    @Override public int sortOrder() {
        return 7;
    }
    @Override
    public RecipeDisplay buildDisplay(ChoppingBoardRecipe recipe) {
        RecipeDisplayBuilder builder = new RecipeDisplayBuilder();


        ItemStack outputs = recipe.getResultItem();

        builder.addSlot(Widgets.AddSlot.input(0, 2, recipe.input(), 20, 0).noBackground().positionTopCenter());
        builder.addSlot(Widgets.AddSlot.input(0, 2, ModItems.KNIFE.get().getDefaultInstance(), 0).noBackground().positionTopLeft());
        builder.addIcon(Widgets.AddIcon.of(0,0, recipe.input(), 0));
        if (recipe.getUses() > 0) {
            builder.addText(
                Widgets.AddText.component(
                    0,
                    0,
                    Component.translatable("gui.ls_furniture.jei.chopping_board.chops", recipe.getUses())
                )
                .centered()
                .bounds(66, 10)
                .positionMiddleLeft()
            );

        }

        builder.addTexture(Widgets.AddArrow.Down.progress(0,0).positionCenter());
        builder.addSlot(Widgets.AddSlot.output(0, 0, outputs, 0).positionBottomCenter());

        builder.contentSize(66, 110);
        return builder.build();
    }
}