package net.lyivx.ls_tweaks.common.feature.sort;

import net.lyivx.ls_tweaks.common.config.QolConfig;
import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SortingCommon {

    public static void onSortRequest(ServerPlayer sp, QolNet.SortC2SPayload req) {
        if (!QolConfig.sorting().enabled) return;

        switch (req.target()) {
            case PLAYER -> sortPlayerMainOnly(sp, req.mode(), req.mergeBeforeSort(), req.unstackablesLast());
            case CONTAINER -> sortOpenContainer(sp, req.mode(), req.mergeBeforeSort(), req.unstackablesLast());
        }
    }

    /** Sort ONLY main inventory (slots 9..35). Hotbar untouched. */
    private static void sortPlayerMainOnly(ServerPlayer sp, QolNet.SortC2SPayload.Mode mode, boolean merge, boolean unstackablesLast) {
        var inv = sp.getInventory();

        List<ItemStack> area = new ArrayList<>(27);
        for (int i = 9; i <= 35; i++) area.add(inv.getItem(i).copy());

        QolDebug.log("Server: sorting PLAYER MAIN (9..35) mode=" + mode + " merge=" + merge + " unstackablesLast=" + unstackablesLast);
        List<ItemStack> sorted = InventorySorter.sort(area, mode, merge, unstackablesLast);

        for (int i = 0; i < sorted.size(); i++) inv.setItem(9 + i, sorted.get(i));
        inv.setChanged();
        sp.containerMenu.broadcastChanges();
    }

    /** Sort the open container's own slots (not the player's inv slots appended to it). */
    private static void sortOpenContainer(ServerPlayer sp, QolNet.SortC2SPayload.Mode mode, boolean merge, boolean unstackablesLast) {
        AbstractContainerMenu menu = sp.containerMenu;
        if (menu == null) return;

        // If it's actually the player's inventory menu, redirect to player sort
        if (menu instanceof InventoryMenu) {
            sortPlayerMainOnly(sp, mode, merge, unstackablesLast);
            return;
        }

        // Blocklisted containers: ignore
        var id = BuiltInRegistries.MENU.getKey(menu.getType());
        if (!SortingWhitelist.isAllowed(menu, null)) {
            return;
        }

        var playerInv = sp.getInventory();
        List<Slot> containerSlots = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (s.container != playerInv && s.isActive()) containerSlots.add(s);
        }
        if (containerSlots.isEmpty()) {
            return;
        }

        List<ItemStack> area = new ArrayList<>(containerSlots.size());
        for (Slot s : containerSlots) area.add(s.getItem().copy());

        List<ItemStack> sorted = InventorySorter.sort(area, mode, merge, unstackablesLast);

        for (int i = 0; i < containerSlots.size(); i++) {
            Slot s = containerSlots.get(i);
            s.container.setItem(s.getContainerSlot(), sorted.get(i));
        }
        menu.broadcastChanges();
    }

    private SortingCommon() {}
}