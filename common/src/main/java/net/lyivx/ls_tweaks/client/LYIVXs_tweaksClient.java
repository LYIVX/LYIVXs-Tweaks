package net.lyivx.ls_tweaks.client;

import net.lyivx.ls_tweaks.client.overlay.ItemPanelOverlay;
import net.lyivx.ls_tweaks.common.feature.quickmove.QuickMoveDragClient;
import net.lyivx.ls_tweaks.common.feature.refill.RefillClient;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient;
import net.lyivx.ls_tweaks.common.feature.sort.SortingClient;
import net.lyivx.ls_tweaks.common.keybinds.ConfigKeybind;
import net.lyivx.ls_tweaks.common.keybinds.SortKeybind;
import net.lyivx.ls_tweaks.recipes.RecipeBrowser;

public class LYIVXs_tweaksClient {

    public static void init() {
        registerKeybinds();

        RefillClient.register(); // hooks ClientTickEvent on the client
        SortingClient.register();
        QuickMoveDragClient.register();
        SlotLockClient.register();

        // Bootstrap recipe browser
        RecipeBrowser.bootstrap();
        ItemPanelOverlay.onDataReload();
    }

    public static void registerKeybinds() {
        SortKeybind.register();
        ConfigKeybind.register();
    }
}
