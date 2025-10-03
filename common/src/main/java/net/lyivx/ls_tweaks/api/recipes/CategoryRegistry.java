package net.lyivx.ls_tweaks.api.recipes;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry for categories; plugins receive this to register their categories. */
public final class CategoryRegistry {
    private final Map<ResourceLocation, RecipeCategory<?>> idToCategory = new LinkedHashMap<>();

    public <T> void register(RecipeCategory<T> category) {
        idToCategory.put(category.id(), category);
    }

    public RecipeCategory<?> get(ResourceLocation id) {
        return idToCategory.get(id);
    }

    public Collection<RecipeCategory<?>> all() {
        return Collections.unmodifiableCollection(idToCategory.values());
    }
}


