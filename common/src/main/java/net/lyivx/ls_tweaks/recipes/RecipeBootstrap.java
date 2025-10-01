package net.lyivx.ls_tweaks.recipes;

import net.lyivx.ls_tweaks.recipes.categories.CookingCategoryBase;
import net.lyivx.ls_tweaks.recipes.categories.CraftingAnyCategory;
import net.minecraft.world.item.Items;

/** Registers built-in categories. */
public final class RecipeBootstrap {
    public static void registerBuiltins() {
        // 1.21.4 crafting: register unified category (retain shaped/shapeless for compatibility)
        RecipeBrowser.CATEGORIES.register(new CraftingAnyCategory());
        // Basic furnace-like categories using generic display extraction
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "smelting"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/furnace.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.FURNACE); }
        });
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "blasting"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/blast_furnace.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.BLAST_FURNACE); }
        });
        RecipeBrowser.CATEGORIES.register(new CookingCategoryBase(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "smoking"), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/smoker.png")){
            @Override protected net.minecraft.world.item.ItemStack getIconItem() { return new net.minecraft.world.item.ItemStack(Items.SMOKER); }
        });
    }
    private RecipeBootstrap() {}
}


