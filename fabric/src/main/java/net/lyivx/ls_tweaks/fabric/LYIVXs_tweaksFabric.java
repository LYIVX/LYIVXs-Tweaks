package net.lyivx.ls_tweaks.fabric;

import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.fabricmc.api.ModInitializer;

public final class LYIVXs_tweaksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        LYIVXs_tweaks.init();
    }
}
