package net.lyivx.ls_tweaks.common.feature.refill;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class RefillClient {
    private static int lastSelectedSlot = -1;
    private static ItemStack lastSnapshot = ItemStack.EMPTY;

    public static void register() {
        ClientTickEvent.CLIENT_POST.register(client -> onClientTick());
    }

    private static void onClientTick() {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        if (!ConfigProvider.refillEnabled()) return;
        if (!ConfigProvider.refillWorkOnContainerScreens() && mc.screen != null) {
            return;
        }
        int slot = mc.player.getInventory().selected;
        ItemStack current = mc.player.getInventory().getSelected();

        if (slot != lastSelectedSlot) {
            lastSelectedSlot = slot;
            lastSnapshot = current.copy();
            return;
        }
        if (lastSnapshot.isEmpty()) {
            lastSnapshot = current.copy();
            return;
        }

        boolean becameEmpty = current.isEmpty() && !lastSnapshot.isEmpty();
        boolean changedTypeOrComponents = !current.isEmpty()
                && !ItemStack.isSameItemSameComponents(current, lastSnapshot);

        if (becameEmpty || changedTypeOrComponents) {
            var payload = new QolNet.RefillC2SPayload(slot, lastSnapshot.copy(), ConfigProvider.refillStrictNbt());
            NetworkManager.sendToServer(payload);

            lastSnapshot = current.copy();
        } else {
            lastSnapshot = current.copy();
        }
    }
}