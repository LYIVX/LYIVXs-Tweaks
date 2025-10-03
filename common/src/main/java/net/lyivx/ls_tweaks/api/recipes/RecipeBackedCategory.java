package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Set;

/**
 * A category that is directly backed by a concrete Minecraft recipe type.
 * Implementations provide the recipe class and type so the browser can enumerate
 * recipes without reflection helpers.
 */
public interface RecipeBackedCategory<T extends Recipe<?>> extends RecipeCategory<T> {
    /** Concrete recipe class handled by this category. */
    Class<T> recipeClass();

    /** Backing recipe type used to enumerate recipes. */
    RecipeType<T> recipeType();

    /** Optional filter of serializers accepted by this category (may be empty for "all"). */
    default Set<RecipeSerializer<? extends T>> allowedSerializers() { return java.util.Set.of(); }
}


