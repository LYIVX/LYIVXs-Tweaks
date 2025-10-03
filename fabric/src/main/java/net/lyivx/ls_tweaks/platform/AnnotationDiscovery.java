package net.lyivx.ls_tweaks.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AnnotationDiscovery {
	private static final String ANNOTATION_DESC = "Lnet/lyivx/ls_tweaks/api/recipes/annotation/LYIVXsTweakPlugin;";

	public static void discoverAnnotatedPlugins() {
		try {
			Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
			for (ModContainer modContainer : mods) {
				ModOrigin origin = modContainer.getOrigin();
				List<Path> paths = origin.getPaths();
				for (Path path : paths) {
					if (Files.isDirectory(path)) {
						scanDirectory(path);
					} else {
						scanJar(path);
					}
				}
			}
		} catch (Throwable ignored) {}
	}

	private static void scanDirectory(Path root) {
		try {
			Files.walk(root).forEach(p -> {
				if (!p.toString().endsWith(".class")) return;
				try { byte[] bytes = Files.readAllBytes(p); analyzeClassBytes(bytes); } catch (Throwable ignored) {}
			});
		} catch (Throwable ignored) {}
	}

	private static void scanJar(Path jarPath) {
		try (ZipFile zf = new ZipFile(jarPath.toFile())) {
			Enumeration<? extends ZipEntry> en = zf.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				if (ze.isDirectory()) continue;
				String name = ze.getName();
				if (!name.endsWith(".class")) continue;
				try (InputStream in = zf.getInputStream(ze)) {
					byte[] bytes = in.readAllBytes();
					analyzeClassBytes(bytes);
				} catch (Throwable ignored) {}
			}
		} catch (Throwable ignored) {}
	}

	private static void analyzeClassBytes(byte[] bytes) {
		try {
			ClassReader cr = new ClassReader(bytes);
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);
			if (cn.visibleAnnotations != null) {
				for (AnnotationNode an : cn.visibleAnnotations) {
					if (ANNOTATION_DESC.equals(an.desc)) { instantiateAndRegister(cn.name.replace('/', '.')); return; }
				}
			}
			if (cn.invisibleAnnotations != null) {
				for (AnnotationNode an : cn.invisibleAnnotations) {
					if (ANNOTATION_DESC.equals(an.desc)) { instantiateAndRegister(cn.name.replace('/', '.')); return; }
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

