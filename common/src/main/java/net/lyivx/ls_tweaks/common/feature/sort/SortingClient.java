package net.lyivx.ls_tweaks.common.feature.sort;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.InventoryMenu;
import org.lwjgl.glfw.GLFW;

public final class SortingClient {
    private static KeyMapping SORT_KEY;

    public static void register() {
        SORT_KEY = new KeyMapping("key.ls_tweaks.sort_inventory", GLFW.GLFW_KEY_R, "key.categories.inventory");
        KeyMappingRegistry.register(SORT_KEY);
        ClientTickEvent.CLIENT_POST.register(client -> onClientTick());
    }

    private static void onClientTick() {
        if (!ConfigProvider.sortingEnabled()) return;
        var mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        while (SORT_KEY.consumeClick()) {
            boolean shift = Screen.hasShiftDown();

            if (shift && mc.player.containerMenu != null && !(mc.player.containerMenu instanceof InventoryMenu)) {
                boolean allowed = SortingWhitelist.isAllowed(mc.player.containerMenu, mc.screen);
                if (!ConfigProvider.sortingAllowContainerSorting() || !allowed) {
                    return;
                }
                var payload = new QolNet.SortC2SPayload(
                        QolNet.SortC2SPayload.Target.CONTAINER,
                        QolNet.SortC2SPayload.Mode.DEFAULT,
                        ConfigProvider.sortingMergeBeforeSort(),
                        ConfigProvider.sortingUnstackablesLast()
                );
                NetworkManager.sendToServer(payload);
            } else if (mc.player.containerMenu instanceof InventoryMenu) {
                // Player inventory sort (only when inventory screen/menu open)
                var payload = new QolNet.SortC2SPayload(
                        QolNet.SortC2SPayload.Target.PLAYER,
                        QolNet.SortC2SPayload.Mode.DEFAULT,
                        ConfigProvider.sortingMergeBeforeSort(),
                        ConfigProvider.sortingUnstackablesLast()
                );
                NetworkManager.sendToServer(payload);
            }
        }
    }

    private SortingClient() {}
}