package net.lyivx.ls_tweaks.platform;

import net.neoforged.fml.ModList;
import org.objectweb.asm.Type;

public final class AnnotationDiscovery {
	private static final String ANNOTATION_DESC = "Lnet/lyivx/ls_tweaks/api/recipes/annotation/LYIVXsTweakPlugin;";

	public static void discoverAnnotatedPlugins() {
		try {
			for (var scanData : ModList.get().getAllScanData()) {
				for (var ann : scanData.getAnnotations()) {
					Type t = ann.annotationType();
					if (t != null && ANNOTATION_DESC.equals(t.getDescriptor())) {
						String className = ann.clazz().getClassName();
						instantiateAndRegister(className);
					}
				}
			}
		} catch (Throwable ignored) {}
	}

	private static void instantiateAndRegister(String className) {
		try {
			Class<?> clazz = Class.forName(className);
			if (!net.lyivx.ls_tweaks.api.recipes.RecipeBrowserPlugin.class.isAssignableFrom(clazz)) return;
			net.lyivx.ls_tweaks.api.recipes.RecipeBrowserPlugin plugin = (net.lyivx.ls_tweaks.api.recipes.RecipeBrowserPlugin) clazz.getDeclaredConstructor().newInstance();
			net.lyivx.ls_tweaks.recipes.RecipeBrowser.registerPlugin(plugin);
		} catch (Throwable ignored) {}
	}
}

