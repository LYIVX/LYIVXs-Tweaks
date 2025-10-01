package net.lyivx.ls_tweaks.api.recipes;

/** Entry point for external mods to register recipe browser categories. */
public interface RecipeBrowserPlugin {
    void registerCategories(CategoryRegistry registry);
}


