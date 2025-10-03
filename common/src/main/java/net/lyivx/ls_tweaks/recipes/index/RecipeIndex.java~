package net.lyivx.ls_tweaks.recipes.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.lyivx.ls_tweaks.recipes.util.DisplayUtil;
import net.minecraft.client.Minecraft;

/**
 * Efficient lookups for recipes by result (show recipe) and by ingredient (show usages).
 * Built from the world's RecipeManager. Call rebuild() on world join/data reload.
 */
public final class RecipeIndex {
    private static final Map<String, List<RecipeHolder<?>>> BY_RESULT_ID = new ConcurrentHashMap<>();
    private static final Map<String, List<RecipeHolder<?>>> BY_INGREDIENT_ID = new ConcurrentHashMap<>();
    
    /** Build on-demand; avoid rebuilding on every click. */
    public static void ensureBuilt() {
        if (BY_RESULT_ID.isEmpty() || BY_INGREDIENT_ID.isEmpty()) {
            boolean haveRM = clientRecipeManager() != null;
            boolean haveRA = clientRegistryAccessAvailable();
            if (haveRM || haveRA) {
                System.out.println("[LS Tweaks][RV] RecipeIndex.ensureBuilt: rebuilding indices");
                rebuild();
            } else {
                System.out.println("[LS Tweaks][RV] RecipeIndex.ensureBuilt: deferring rebuild (no RecipeManager/RegistryAccess)");
            }
        }
    }

    public static void rebuild() {
        BY_RESULT_ID.clear();
        BY_INGREDIENT_ID.clear();
        RecipeManager mgr = clientRecipeManager();
        if (mgr == null) return;

        int count = 0;
        final int[] dbg = new int[]{0};
        java.util.function.Consumer<RecipeHolder<?>> indexHolder = holder -> {
            Recipe<?> r = holder.value();
            // Use display-backed result (fast) and placementInfo ingredients
            ItemStack out = DisplayUtil.firstOutput(holder);
            if (!out.isEmpty()) {
                for (String key : keysOf(out)) BY_RESULT_ID.computeIfAbsent(key, k -> new ArrayList<>()).add(holder);
            }
            try {
                var info = r.placementInfo();
                if (info != null && info.ingredients() != null) {
                    int i = 0;
                    for (var ing : info.ingredients()) {
                        if (ing == null) { i++; continue; }
                        for (var h : ing.items().toList()) {
                            ItemStack s = new ItemStack(h.value());
                            if (s.isEmpty()) continue;
                            for (String ikey : keysOf(s)) BY_INGREDIENT_ID.computeIfAbsent(ikey, k -> new ArrayList<>()).add(holder);
                            dbg[0]++;
                        }
                        i++;
                    }
                }
            } catch (Throwable ignored) {}
        };

        for (RecipeHolder<?> holder : mgr.getRecipes()) { indexHolder.accept(holder); count++; }
        // Removed JsonRecipeIndex fallback - using working recipe system only
        System.out.println("[LS Tweaks][RV] Index built: recipes=" + count + " resultsKeys=" + BY_RESULT_ID.size() + " ingredientKeys=" + BY_INGREDIENT_ID.size() + " totalIngredients=" + dbg[0]);
    }

    public static List<RecipeHolder<?>> recipesFor(ItemStack result) {
        if (result == null || result.isEmpty()) return List.of();
        List<RecipeHolder<?>> out = BY_RESULT_ID.getOrDefault(keyOf(result), List.of());
        if (!out.isEmpty()) return out;
        java.util.List<String> alt = keysOf(result);
        for (String k : alt) {
            out = BY_RESULT_ID.getOrDefault(k, List.of());
            if (!out.isEmpty()) return out;
        }
        // Slow fallback: match by base item only (ignore NBT/components like dye color)
        // Removed heavy fallback that depended on old RecipeUtil
        try {
            System.out.println("[LS Tweaks][RV] recipesFor miss: keys=" + alt + " primary=" + keyOf(result) + " resultsKeysSize=" + BY_RESULT_ID.size());
        } catch (Throwable ignored) {}
        return List.of();
    }

    public static List<RecipeHolder<?>> usagesFor(ItemStack ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return List.of();
        String key = keyOf(ingredient);
        List<RecipeHolder<?>> result = BY_INGREDIENT_ID.getOrDefault(key, List.of());
        if (result.isEmpty()) {
            for (String k : keysOf(ingredient)) {
                result = BY_INGREDIENT_ID.getOrDefault(k, List.of());
                if (!result.isEmpty()) break;
            }
        }
        System.out.println("[LS Tweaks][RV] usagesFor(" + ingredient.getHoverName().getString() + ") key=" + key + " altHit=" + (!result.isEmpty()) + " found=" + result.size() + " totalIngredientKeys=" + BY_INGREDIENT_ID.size());
        return result;
    }
    
    public static List<RecipeHolder<?>> getAllRecipes() {
        List<RecipeHolder<?>> allRecipes = new ArrayList<>();
        allRecipes.addAll(BY_RESULT_ID.values().stream().flatMap(List::stream).distinct().toList());
        allRecipes.addAll(BY_INGREDIENT_ID.values().stream().flatMap(List::stream).distinct().toList());
        return allRecipes.stream().distinct().toList();
    }

    private static String keyOf(ItemStack stack) {
        return stack.getItemHolder().unwrapKey().map(k -> k.location().toString()).orElse(stack.getHoverName().getString());
    }

    private static java.util.List<String> keysOf(ItemStack stack) {
        java.util.List<String> keys = new java.util.ArrayList<>();
        try {
            stack.getItemHolder().unwrapKey().ifPresent(k -> keys.add(k.location().toString()));
        } catch (Throwable ignored) {}
        try {
            String hover = stack.getHoverName().getString();
            if (hover != null && !hover.isBlank()) keys.add(hover);
        } catch (Throwable ignored) {}
        try {
            String desc = stack.getItem().getDescriptionId();
            if (desc != null && !desc.isBlank()) keys.add(desc);
        } catch (Throwable ignored) {}
        // Normalize variants: lowercase
        for (int i = 0; i < keys.size(); i++) keys.set(i, keys.get(i));
        return keys;
    }

    private RecipeIndex() {}

    private static RecipeManager clientRecipeManager() {
        try {
            var mc = Minecraft.getInstance();
            if (mc == null) return null;
            Object server = mc.getSingleplayerServer();
            if (server != null) {
                try { var m = server.getClass().getMethod("getRecipeManager"); Object rm = m.invoke(server); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
            }
            var conn = mc.getConnection();
            if (conn != null) {
                try { var m = conn.getClass().getMethod("getRecipeManager"); Object rm = m.invoke(conn); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
                for (var m : conn.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                        try { Object rm = m.invoke(conn); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static boolean clientRegistryAccessAvailable() {
        try {
            var mc = Minecraft.getInstance();
            return mc != null && mc.level != null && mc.level.registryAccess() != null;
        } catch (Throwable ignored) {}
        return false;
    }
}


