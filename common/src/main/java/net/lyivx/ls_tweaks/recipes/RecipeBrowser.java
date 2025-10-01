package net.lyivx.ls_tweaks.recipes;

import java.util.ArrayList;
import java.util.List;
import net.lyivx.ls_tweaks.api.recipes.CategoryRegistry;
import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeBrowserPlugin;

/** Global singleton for the recipe browser runtime. */
public final class RecipeBrowser {
    public static final CategoryRegistry CATEGORIES = new CategoryRegistry();

    /** Discover and register categories from plugins. */
    public static void bootstrap() {
        // Built-ins
        RecipeBootstrap.registerBuiltins();
        // ServiceLoader discovery for external plugins
        try {
            for (RecipeBrowserPlugin p : java.util.ServiceLoader.load(RecipeBrowserPlugin.class)) {
                try { registerPlugin(p); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private static final List<RecipeBrowserPlugin> plugins = new ArrayList<>();

    public static void registerPlugin(RecipeBrowserPlugin plugin) {
        if (plugin == null) return;
        plugins.add(plugin);
        plugin.registerCategories(CATEGORIES);
        // Sanity log
        try {
            for (var cat : CATEGORIES.all()) {
                if (cat == null || cat.id() == null) {
                    System.out.println("[LS Tweaks][RV] WARN: Null category or id after plugin register: " + plugin.getClass().getName());
                }
            }
        } catch (Throwable ignored) {}
    }

    public static List<RecipeCategory<?>> getAllCategories() {
        return new ArrayList<>(CATEGORIES.all());
    }

    private RecipeBrowser() {}
}


