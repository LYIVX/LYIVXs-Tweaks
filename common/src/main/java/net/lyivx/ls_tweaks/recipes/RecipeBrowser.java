package net.lyivx.ls_tweaks.recipes;

import net.lyivx.ls_tweaks.api.recipes.CategoryRegistry;
import net.lyivx.ls_tweaks.api.recipes.RecipeBrowserPlugin;
import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;

import java.util.ArrayList;
import java.util.List;

/** Global singleton for the recipe browser runtime. */
public class RecipeBrowser {
    public static final CategoryRegistry CATEGORIES = new CategoryRegistry();

	private static final java.util.Set<String> SEEN_PLUGIN_CLASSES = new java.util.HashSet<>();


    /** Discover and register categories from plugins. */
    public static void bootstrap() {
        // Built-ins
        RecipeBootstrap.registerBuiltins();
		// Annotation-based discovery via platform class (Fabric/NeoForge provide this)
		try {
			Class<?> disc = Class.forName("net.lyivx.ls_tweaks.platform.AnnotationDiscovery");
			disc.getMethod("discoverAnnotatedPlugins").invoke(null);
		} catch (Throwable ignored) {}
		// ServiceLoader discovery for external plugins (fallback)
        try {
            for (RecipeBrowserPlugin p : java.util.ServiceLoader.load(RecipeBrowserPlugin.class)) {
				try { registerPlugin(p); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private static final List<RecipeBrowserPlugin> plugins = new ArrayList<>();

	public static void registerPlugin(RecipeBrowserPlugin plugin) {
        if (plugin == null) return;
		String cls = plugin.getClass().getName();
		if (!SEEN_PLUGIN_CLASSES.add(cls)) return; // avoid duplicate registration
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
