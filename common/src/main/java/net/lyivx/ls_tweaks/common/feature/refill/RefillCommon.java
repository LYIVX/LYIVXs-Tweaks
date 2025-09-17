package net.lyivx.ls_tweaks.common.feature.refill;

import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.util.InvUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RefillCommon {
    public static void register() { /* reserved */ }

    public static void onRefillRequest(ServerPlayer player, QolNet.RefillC2SPayload req) {
        if (!ConfigProvider.refillEnabled()) {
            return;
        }
        int slot = req.hotbarSlot();
        if (slot < 0 || slot > 8) {
            return;
        }

        var inv = player.getInventory();
        ItemStack inSlot = inv.getItem(slot);

        ItemStack candidate = InvUtils.findMatchingInBackpack(player, req.signature(), req.strict());

        if (candidate.isEmpty()) {
            QolDebug.actionbar(player, "Refill: no matching stack found");
            return;
        }
        if (!inSlot.isEmpty() && !ItemStack.isSameItemSameComponents(inSlot, candidate)) {
            QolDebug.actionbar(player, "Refill: slot occupied");
            return;
        }

        boolean moved = InvUtils.moveIntoHotbarSlot(player, candidate, slot);
        if (moved) {
            QolDebug.actionbar(player, "Refilled " + QolDebug.itemStr(inv.getItem(slot)));
        } else {
            QolDebug.actionbar(player, "Refill: target full");
        }
    }
}