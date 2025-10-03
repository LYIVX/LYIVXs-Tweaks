package net.lyivx.ls_tweaks.recipes.index;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lists all items/blocks for the right-side item panel, with simple pagination/filter. */
public final class ItemIndex {
    private static List<ItemStack> ALL = Collections.emptyList();
    private static final java.util.Map<String, String> MOD_NAME_CACHE = new java.util.HashMap<>();

    public static void rebuild() {
        List<ItemStack> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == null) continue;
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty()) items.add(stack);
        }
        ALL = List.copyOf(items);
        // clear mod name cache; will be lazily rebuilt
        MOD_NAME_CACHE.clear();
    }

    public static List<ItemStack> all() { return ALL; }

    public static List<ItemStack> page(int offset, int limit, String filter) {
        if (ALL.isEmpty()) return ALL;
        List<ItemStack> src = ALL;
        if (filter != null && !filter.isBlank()) {
            src = filterItems(filter);
        }
        int from = Math.max(0, Math.min(offset, src.size()));
        int to = Math.max(from, Math.min(from + Math.max(0, limit), src.size()));
        return src.subList(from, to);
    }
    
    /** Returns the total size for the current filter (or ALL when filter blank). */
    public static int size(String filter) {
        if (ALL.isEmpty()) return 0;
        if (filter == null || filter.isBlank()) return ALL.size();
        return filterItems(filter).size();
    }
    
    private static List<ItemStack> filterItems(String filter) {
        String f = filter.trim().toLowerCase();
        List<ItemStack> filtered = new ArrayList<>();
        
        if (f.startsWith("#")) {
            // Tag search - simplified for now
            String tagName = f.substring(1);
            System.out.println("[LS Tweaks][RV] Tag search not fully implemented yet: " + tagName);
            // TODO: Implement proper tag search when tag API is stable
        } else if (f.startsWith("@")) {
            // Mod search with optional free-text filter, supports mod id or display name (spaces -> underscores)
            String rest = f.substring(1).trim();
            String modQuery;
            String nameFilter = null;
            int space = rest.indexOf(' ');
            if (space >= 0) {
                modQuery = rest.substring(0, space).trim();
                nameFilter = rest.substring(space + 1).trim();
            } else {
                modQuery = rest;
            }
            String modQueryNorm = normalizeModKey(modQuery);
            for (ItemStack s : ALL) {
                String ns = s.getItem().arch$registryName().getNamespace();
                String nsNorm = normalizeModKey(ns);
                String display = getModDisplayName(ns);
                String displayNorm = normalizeModKey(display);
                boolean modMatch = nsNorm.equals(modQueryNorm) || displayNorm.contains(modQueryNorm);
                if (!modMatch) continue;
                if (nameFilter == null || nameFilter.isBlank()) {
                    filtered.add(s);
                } else {
                    String nm = s.getHoverName().getString().toLowerCase();
                    if (nm.contains(nameFilter.toLowerCase())) filtered.add(s);
                }
            }
        } else {
            // Normal search
            for (ItemStack s : ALL) {
                String name = s.getHoverName().getString().toLowerCase();
                if (name.contains(f)) filtered.add(s);
            }
        }
        
        return filtered;
    }

    private static String normalizeModKey(String s) {
        if (s == null) return "";
        return s.toLowerCase().replace(' ', '_');
    }

    public static String getModDisplayName(String namespace) {
        String cached = MOD_NAME_CACHE.get(namespace);
        if (cached != null) return cached;
        try {
            // Fabric
            Class<?> fl = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object inst = fl.getMethod("getInstance").invoke(null);
            Object contOpt = fl.getMethod("getModContainer", String.class).invoke(inst, namespace);
            if (contOpt instanceof java.util.Optional<?> opt) {
                Object cont = opt.orElse(null);
                if (cont != null) {
                    Object meta = cont.getClass().getMethod("getMetadata").invoke(cont);
                    Object name = meta.getClass().getMethod("getName").invoke(meta);
                    if (name instanceof String s) return s;
                }
            }
        } catch (Throwable ignored) {}
        try {
            // NeoForge
            Class<?> ml = Class.forName("net.neoforged.fml.ModList");
            Object inst = ml.getMethod("get").invoke(null);
            Object contOpt = ml.getMethod("getModContainerById", String.class).invoke(inst, namespace);
            if (contOpt instanceof java.util.Optional<?> opt) {
                Object cont = opt.orElse(null);
                if (cont != null) {
                    Object info = cont.getClass().getMethod("getModInfo").invoke(cont);
                    Object name = info.getClass().getMethod("getDisplayName").invoke(info);
                    if (name instanceof String s) return s;
                }
            }
        } catch (Throwable ignored) {}
        String result = "minecraft".equals(namespace) ? "Minecraft" : namespace;
        MOD_NAME_CACHE.put(namespace, result);
        return result;
    }

    private ItemIndex() {}
}


