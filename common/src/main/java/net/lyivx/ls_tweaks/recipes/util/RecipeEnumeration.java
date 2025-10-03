package net.lyivx.ls_tweaks.recipes.util;

import net.lyivx.ls_tweaks.api.recipes.RecipeBackedCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeCategory;
import net.lyivx.ls_tweaks.api.recipes.RecipeDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
 

/**
 * Minimal helper to enumerate recipes via RecipeManager when categories specify the RecipeType.
 * Based on NeoForge docs for 1.21.4 recipe access.
 * See: docs "Recipes" and "Built-In Recipe Types".
 */
public final class RecipeEnumeration {
    public static List<RecipeDisplay> buildDisplaysForCategory(RecipeCategory<?> category) {
        if (!(category instanceof RecipeBackedCategory<?> backed)) return List.of();
        RecipeManager mgr = clientRecipeManager();
        if (mgr == null) return List.of();

        List<RecipeDisplay> out = new ArrayList<>();
        enumerate(mgr, cast(backed), out);
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Recipe<?>> void enumerate(RecipeManager mgr, RecipeBackedCategory<T> cat, List<RecipeDisplay> out) {
        RecipeType<T> type = cat.recipeType();
        if (type == null) return;
        // Iterate all recipes of a type (RecipeManager#recipeMap().byType(type))
        // NeoForge 1.21.4: iterate through manager recipes and filter by type
        try {
            for (RecipeHolder<?> holder : mgr.getRecipes()) {
                try {
                    Recipe<?> raw = holder.value();
                    if (raw == null) continue;
                    if (raw.getType() == type && cat.recipeClass().isInstance(raw)) {
                        RecipeDisplay d = ((RecipeCategory)cat).buildDisplay(holder);
                        if (d != null) out.add(d);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Recipe<?>> RecipeBackedCategory<T> cast(RecipeBackedCategory<?> raw) {
        return (RecipeBackedCategory) raw;
    }

    private static RecipeManager clientRecipeManager() {
        try {
            var mc = Minecraft.getInstance();
            if (mc == null) return null;
            // Prefer singleplayer server manager when present
            Object server = mc.getSingleplayerServer();
            if (server != null) {
                try {
                    var m = server.getClass().getMethod("getRecipeManager");
                    Object rm = m.invoke(server);
                    if (rm instanceof RecipeManager r) return r;
                } catch (Throwable ignored) {}
            }
            // Fall back to client connection path (NeoForge keeps a client-side mirror)
            var conn = mc.getConnection();
            if (conn != null) {
                try {
                    var m = conn.getClass().getMethod("getRecipeManager");
                    Object rm = m.invoke(conn);
                    if (rm instanceof RecipeManager r) return r;
                } catch (Throwable ignored) {}
                for (var m : conn.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                        try { Object rm = m.invoke(conn); if (rm instanceof RecipeManager r) return r; } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private RecipeEnumeration() {}
}


