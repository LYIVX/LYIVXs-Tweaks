package net.lyivx.ls_tweaks;

import dev.architectury.event.events.common.LifecycleEvent;
import net.lyivx.ls_tweaks.common.config.ConfigRegistry;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelistData;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.feature.refill.RefillCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LYIVXs_tweaks {
    public static final String MOD_ID = "ls_tweaks";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        QolNet.register();             // register packets (common side)
        RefillCommon.register();       // server logic hooks

        SortingWhitelistData.register();

        ConfigRegistry.init();

        LifecycleEvent.SETUP.register(() -> LOGGER.info("[QOLCraft] initialized"));
    }
}
