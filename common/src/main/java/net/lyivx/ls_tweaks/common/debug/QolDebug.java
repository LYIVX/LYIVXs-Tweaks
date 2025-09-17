package net.lyivx.ls_tweaks.common.debug;

import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Toggleable debug logger + helpers. */
public final class QolDebug {
    public static boolean ENABLED = true; // flip off when done

    public static void log(String msg) {
        if (!ENABLED) return;
        // Console print
        System.out.println("[QOL DEBUG] " + msg);
        // Mod logger (shows with log level)
        LYIVXs_tweaks.LOGGER.info("[QOL DEBUG] {}", msg);
    }

    public static String itemStr(ItemStack st) {
        if (st == null || st.isEmpty()) return "EMPTY";
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
        return id + " x" + st.getCount();
    }

    public static void actionbar(net.minecraft.server.level.ServerPlayer sp, String msg) {
        if (!ENABLED) return;
        sp.displayClientMessage(Component.literal(msg), true);
    }
}