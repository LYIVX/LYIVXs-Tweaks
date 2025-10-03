package net.lyivx.ls_tweaks.recipes;

import net.lyivx.ls_furniture.common.recipes.WorkstationRecipe;
import net.lyivx.ls_furniture.registry.ModItems;
import net.lyivx.ls_furniture.registry.ModRecipes;
import net.lyivx.ls_tweaks.recipes.categories.CookingCategoryBase;
import net.lyivx.ls_tweaks.recipes.categories.CraftingAnyCategory;
import net.lyivx.ls_tweaks.recipes.categories.TagsCategory;
import net.lyivx.ls_tweaks.recipes.categories.furnection.WorkstationCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

/** Registers built-in categories. */
public final class RecipeBootstrap {
    public static void registerBuiltins() {
        // 1.21.4 crafting: register unified category bound to the crafting recipe type
        RecipeBrowser.CATEGORIES.register(new CraftingAnyCategory());
        // Basic furnace-like categories bound to their recipe types
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "smelting"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/furnace.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.FURNACE); }
            @Override public Class<AbstractCookingRecipe> recipeClass() { return AbstractCookingRecipe.class; }
            @Override public RecipeType<AbstractCookingRecipe> recipeType() { return (RecipeType) RecipeType.SMELTING; }
        });
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "blasting"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/blast_furnace.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.BLAST_FURNACE); }
            @Override public Class<AbstractCookingRecipe> recipeClass() { return AbstractCookingRecipe.class; }
            @Override public RecipeType<AbstractCookingRecipe> recipeType() { return (RecipeType) RecipeType.BLASTING; }
        });
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "smoking"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/smoker.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.SMOKER); }
            @Override public Class<AbstractCookingRecipe> recipeClass() { return AbstractCookingRecipe.class; }
            @Override public RecipeType<AbstractCookingRecipe> recipeType() { return (RecipeType) RecipeType.SMOKING; }
        });
        // Tags category (usages only)
        RecipeBrowser.CATEGORIES.register(new TagsCategory());


        RecipeBrowser.CATEGORIES.register(new WorkstationCategory(ResourceLocation.fromNamespaceAndPath("ls_furniture", "workstation"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ls_furniture", "textures/item/furnace.png"), ModRecipes.WORKSTATION_RECIPE.get()) {
            @Override
            protected ItemStack getIconItem() {
                return ModItems.WORKSTATION.get().getDefaultInstance();
            }

            @Override
            public Class<WorkstationRecipe> recipeClass() {
                return WorkstationRecipe.class;
            }

            @Override
            public RecipeType<WorkstationRecipe> recipeType() {
                return ModRecipes.WORKSTATION_RECIPE.get();
            }
        });
    }
    private RecipeBootstrap() {}
}


