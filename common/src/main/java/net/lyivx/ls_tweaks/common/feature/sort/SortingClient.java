package net.lyivx.ls_tweaks.common.feature.sort;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

import static net.lyivx.ls_tweaks.common.keybinds.SortKeybind.SORT_KEY;

public final class SortingClient {
    private static boolean guiWasDown = false; // edge-detect while a GUI is open

    /** Call from client init. */
    public static void register() {

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (client == null) return;

            // Only function when some GUI is open (inventory or container)
            Screen scr = client.screen;
            if (scr == null) { guiWasDown = false; return; }

            boolean isPlayerInvScreen = (scr instanceof InventoryScreen) || (scr instanceof CreativeModeInventoryScreen);
            boolean isContainerScreen = scr instanceof AbstractContainerScreen<?>;

            if (!isPlayerInvScreen && !isContainerScreen) {
                guiWasDown = false;
                return; // chat/pause/others
            }

            // Poll current bound key while GUI is up (KeyMapping.consumeClick() won’t fire here)
            int keyCode = SORT_KEY.getDefaultKey().getValue();
            if (keyCode == InputConstants.UNKNOWN.getValue()) { guiWasDown = false; return; }
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean down = InputConstants.isKeyDown(window, keyCode);

            // Edge detection
            if (!down || guiWasDown) { guiWasDown = down; return; }
            guiWasDown = true;

            // ---- key pressed (with a GUI open) ----
            try {
                debug("==== Sort key pressed (GUI) ====");
                if (client.player == null) { debug("Abort: player is null"); return; }

                // Config flags
                boolean enabled = ConfigProvider.sortingEnabled();
                boolean allowContainers = ConfigProvider.sortingAllowContainerSorting();
                boolean merge = ConfigProvider.sortingMergeBeforeSort();
                boolean unstackLast = ConfigProvider.sortingUnstackablesLast();
                debug("Config -> enabled=" + enabled + ", allowContainers=" + allowContainers +
                        ", mergeBefore=" + merge + ", unstackLast=" + unstackLast);
                if (!enabled) { toast(client, "Sorting disabled."); debug("Abort: sorting disabled"); return; }

                // Shift state
                boolean shiftDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
                debug("ShiftDown=" + shiftDown);

                // R (no shift) ALWAYS sorts PLAYER inventory (works on both inventory and container screens)
                if (!shiftDown) {
                    debug("Action: sort PLAYER inventory (R)");
                    NetworkManager.sendToServer(new QolNet.SortC2SPayload(
                            QolNet.SortC2SPayload.Target.PLAYER,
                            resolveDefaultSortMode(),
                            merge, unstackLast
                    ));
                    toast(client, "Sorting: Inventory");
                    debug("Sent SortC2S (PLAYER).");
                    return;
                }

                // Shift+R sorts the CONTAINER — only if a real container screen is open
                if (!isContainerScreen) {
                    debug("Abort: Shift+R but no container screen (inventory or other GUI)");
                    toast(client, "No container to sort.");
                    return;
                }

                AbstractContainerMenu menu = client.player.containerMenu;
                if (menu == null) {
                    debug("Abort: container screen but menu is null");
                    toast(client, "No container to sort.");
                    return;
                }

                // Safe: only read MenuType id for actual container screens
                ResourceLocation menuId = BuiltInRegistries.MENU.getKey(menu.getType());
                debug("Container screen=" + scr.getClass().getName() + "  MenuType=" + (menuId == null ? "<null>" : menuId));

                if (!allowContainers) {
                    debug("Abort: allowContainerSorting=false");
                    toast(client, "Container sorting disabled.");
                    return;
                }

                boolean wl = SortingWhitelist.isAllowed(menu, (AbstractContainerScreen<?>) scr);
                debug("Whitelist=" + wl);
                if (!wl) {
                    debug("Abort: container not whitelisted");
                    toast(client, "Container not whitelisted.");
                    return;
                }

                debug("Action: sort CONTAINER (Shift+R)");
                NetworkManager.sendToServer(new QolNet.SortC2SPayload(
                        QolNet.SortC2SPayload.Target.CONTAINER,
                        resolveDefaultSortMode(),
                        merge, unstackLast
                ));
                toast(client, "Sorting: Container");
                debug("Sent SortC2S (CONTAINER).");

            } catch (Throwable t) {
                debug("Exception during sort key handling: " + t);
            } finally {
                debug("==== Sort key handling done ====");
            }
        });
    }

    private static QolNet.SortC2SPayload.Mode resolveDefaultSortMode() {
        var cfg = ConfigProvider.defaultSortMode();
        // Map config sort choice to network mode
        return switch (cfg) {
            case CATEGORY -> QolNet.SortC2SPayload.Mode.DEFAULT;
            case A_TO_Z -> QolNet.SortC2SPayload.Mode.ALPHA_ASC;
            case Z_TO_A -> QolNet.SortC2SPayload.Mode.ALPHA_DESC;
        };
    }

    private static void toast(Minecraft mc, String msg) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[LS Tweaks] " + msg), true);
    }

    private static void debug(String s) {
        try { QolDebug.log("[SortingClient] " + s); }
        catch (Throwable ignored) { System.out.println("[LS Tweaks][SortingClient] " + s); }
    }

    private SortingClient() {}
}