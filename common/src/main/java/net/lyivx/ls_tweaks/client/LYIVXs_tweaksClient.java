package net.lyivx.ls_tweaks.client;

import net.lyivx.ls_tweaks.common.feature.refill.RefillClient;
import net.lyivx.ls_tweaks.common.feature.sort.SortingClient;

public class LYIVXs_tweaksClient {

    public static void init() {
        RefillClient.register(); // hooks ClientTickEvent on the client
        SortingClient.register();
    }
}
