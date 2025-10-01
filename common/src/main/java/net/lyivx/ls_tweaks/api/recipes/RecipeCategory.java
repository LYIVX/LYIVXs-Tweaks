package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** Describes a recipe category (e.g., crafting, smelting). */
public interface RecipeCategory<T> {
    /** Unique id for this category, typically the underlying recipe type id. */
    ResourceLocation id();

    /** Display name translation key. */
    String titleTranslationKey();

    /** Icon to render for this category tab. */
    ResourceLocation iconTexture();
    
    /** Item/block icon for this category tab (preferred over texture). */
    default ItemStack iconItem() { return ItemStack.EMPTY; }

    /**
     * Optional sort order for categories in the browser. Lower values appear first.
     * Defaults to MAX_VALUE (i.e., sorted after explicitly ordered categories).
     */
    default int sortOrder() { return Integer.MAX_VALUE; }

    /** Builds a display from a concrete recipe instance. */
    RecipeDisplay buildDisplay(T recipe);
    
    /** Builds a display from a recipe holder (includes recipe ID). */
    @SuppressWarnings("unchecked")
    default RecipeDisplay buildDisplay(net.minecraft.world.item.crafting.RecipeHolder<? extends Recipe<?>> holder) {
        return buildDisplay((T) holder.value());
    }

    /** Optional: width/height of the content area for layout. */
    default int contentWidth() { return 150; }
    default int contentHeight() { return 66; }
}


