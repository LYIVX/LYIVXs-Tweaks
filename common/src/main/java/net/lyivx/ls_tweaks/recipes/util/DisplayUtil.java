package net.lyivx.ls_tweaks.recipes.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Minimal utilities to extract display outputs via RecipeManager display entries. */
public final class DisplayUtil {
    public static ItemStack firstOutput(RecipeHolder<?> holder) {
        List<Object> entries = listEntries(holder);
        for (Object e : entries) {
            ItemStack s = extractFirstResultItem(e);
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    private static List<Object> listEntries(RecipeHolder<?> holder) {
        List<Object> out = new ArrayList<>();
        try {
            var mc = Minecraft.getInstance();
            Object server = mc.getSingleplayerServer();
            if (server == null) return out;
            RecipeManager rm = null;
            try {
                var m = server.getClass().getMethod("getRecipeManager");
                Object r = m.invoke(server);
                if (r instanceof RecipeManager rmm) rm = rmm;
            } catch (Throwable ignored) {}
            if (rm == null) return out;
            List<Object> list = new ArrayList<>();
            Consumer<Object> cons = list::add;
            try {
                var m = RecipeManager.class.getMethod("listDisplaysForRecipe", Class.forName("net.minecraft.resources.ResourceKey"), java.util.function.Consumer.class);
                var idM = holder.getClass().getMethod("id");
                Object key = idM.invoke(holder);
                m.invoke(rm, key, cons);
            } catch (Throwable ignored) {}
            out.addAll(list);
        } catch (Throwable ignored) {}
        return out;
    }

    private static ItemStack extractFirstResultItem(Object entry) {
        if (entry == null) return ItemStack.EMPTY;
        try {
            String clsName = entry.getClass().getName();
            if (clsName.contains("display.RecipeDisplayEntry")) {
                try {
                    Class<?> sdc = Class.forName("net.minecraft.world.item.crafting.display.SlotDisplayContext");
                    var fromLevel = sdc.getMethod("fromLevel", Class.forName("net.minecraft.world.level.Level"));
                    Object ctx = fromLevel.invoke(null, Minecraft.getInstance().level);
                    var ctxMap = sdc.getMethod("context").invoke(ctx);
                    var resultItems = entry.getClass().getMethod("resultItems", ContextMap.class);
                    Object list = resultItems.invoke(entry, ctxMap);
                    if (list instanceof java.util.List<?> ls) {
                        for (Object o : ls) if (o instanceof ItemStack s && !s.isEmpty()) return s;
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private DisplayUtil() {}
}


