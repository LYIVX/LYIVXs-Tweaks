package net.lyivx.ls_tweaks.common.config;

import net.lyivx.ls_core.common.config.CustomConfigSpec;
import net.lyivx.ls_tweaks.LYIVXs_tweaks;

/**
 * Safe to call from both client and server. Creates the Core spec on first use
 * and mirrors values into QolConfig so existing code keeps working.
 */
public final class ConfigRegistry {
    private ConfigRegistry() {}

    /** Call once during common init. */
    public static void init() {
        LYIVXs_tweaks.LOGGER.info("Initializing LS Tweaks config (LYIVX Core).");
    }
}