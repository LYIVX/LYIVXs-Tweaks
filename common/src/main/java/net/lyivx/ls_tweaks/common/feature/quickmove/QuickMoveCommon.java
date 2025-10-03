package net.lyivx.ls_tweaks.common.feature.quickmove;

import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockCommon;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.lyivx.ls_tweaks.common.util.QuickOpContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/** Server handler for Shift-drag quick-move events, one slot at a time. */
public final class QuickMoveCommon {
    public static void handle(ServerPlayer player, QolNet.QuickMoveC2SPayload p) {
        if (player == null) return;
        var menu = player.containerMenu;
        if (menu == null) return;
        int idx = p.slotIndex;
        if (idx < 0 || idx >= menu.slots.size()) return;
        Slot s = menu.slots.get(idx);

        // Respect player slot locks
        if (s.container instanceof Inventory && SlotLockCommon.isLocked(player, s.getContainerSlot())) return;

        if (s.hasItem()) {
            boolean stopQuick = net.lyivx.ls_tweaks.common.config.ConfigProvider.slotLockStopQuickMoves();
            boolean isPlayerSlot = s.container instanceof Inventory;
            boolean locked = isPlayerSlot && SlotLockCommon.isLocked(player, s.getContainerSlot());
            if (stopQuick && locked) return; // respect config: do not move from locked
            try {
                QuickOpContext.enter();
                player.containerMenu.clicked(idx, 0, ClickType.QUICK_MOVE, player);
            } finally {
                QuickOpContext.exit();
            }
        }
    }

    private QuickMoveCommon() {}
}


