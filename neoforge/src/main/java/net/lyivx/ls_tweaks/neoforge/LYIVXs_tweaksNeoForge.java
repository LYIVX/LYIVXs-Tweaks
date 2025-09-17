package net.lyivx.ls_tweaks.neoforge;

import dev.architectury.event.events.common.LifecycleEvent;
import net.lyivx.ls_core.LYIVXsCore;
import net.lyivx.ls_tweaks.LYIVXs_tweaks;
import net.neoforged.fml.common.Mod;

@Mod(LYIVXs_tweaks.MOD_ID)
public final class LYIVXs_tweaksNeoForge {
    public LYIVXs_tweaksNeoForge() {
        // Run our common setup.
        LYIVXs_tweaks.init();

        LifecycleEvent.SETUP.register(LYIVXsCore::setup);
    }
}
