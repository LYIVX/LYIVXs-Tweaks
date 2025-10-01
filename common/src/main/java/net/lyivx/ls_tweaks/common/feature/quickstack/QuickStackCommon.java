package net.lyivx.ls_tweaks.common.feature.quickstack;

import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.feature.sort.SortingWhitelist;
import net.minecraft.server.level.ServerPlayer;
import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockCommon;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.lyivx.ls_tweaks.common.util.QuickOpContext;


public final class QuickStackCommon {

    public enum Direction { PLAYER_TO_CONTAINER, CONTAINER_TO_PLAYER }
    public enum Mode { MATCHING_ONLY, MOVE_ALL }

    /** Entry point from the network payload. */
    public static void handle(ServerPlayer player, Direction dir, Mode mode) {
        if (player == null) return;
        if (!ConfigProvider.quickStackEnabled()) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) return;

        // Only allow when in a whitelisted container screen
        if (!SortingWhitelist.isAllowed(menu, null)) return;

        if (dir == Direction.PLAYER_TO_CONTAINER) {
            move(player, menu, /*fromPlayerInv*/ true, mode);
        } else {
            move(player, menu, /*fromPlayerInv*/ false, mode);
        }
        menu.broadcastChanges();
    }

    private static void move(ServerPlayer player, AbstractContainerMenu menu, boolean fromPlayerInv, Mode mode) {
        // side identification not used currently
        Iterable<Slot> srcSlots  = () -> menu.slots.stream().filter(s -> {
            if (fromPlayerInv) {
                if (!(s.container instanceof Inventory)) return false;
                // skip locked player slots
                return !ConfigProvider.slotLockStopQuickMoves() || !SlotLockCommon.isLocked(player, s.getContainerSlot());
            } else {
                return !(s.container instanceof Inventory); // container side
            }
        }).iterator();
        Iterable<Slot> destSlots = () -> menu.slots.stream().filter(s -> {
            if (fromPlayerInv) {
                return !(s.container instanceof Inventory); // container side
            } else {
                if (!(s.container instanceof Inventory)) return false;
                // when config ON: skip locked entirely; when OFF: we'll validate signature later per-slot
                return !ConfigProvider.slotLockStopQuickMoves() || !SlotLockCommon.isLocked(player, s.getContainerSlot());
            }
        }).iterator();

        for (Slot s : srcSlots) {
            ItemStack stack = s.getItem();
            if (stack.isEmpty()) continue;

            if (mode == Mode.MATCHING_ONLY) {
                boolean hasMatch = false;
                for (Slot d : destSlots) {
                    ItemStack dst = d.getItem();
                    if (!dst.isEmpty() && ItemStack.isSameItemSameComponents(dst, stack)) {
                        hasMatch = true;
                        break;
                    }
                }
                if (!hasMatch) continue;
            }

            // merge into existing stacks first, then empties
            stack = tryMoveStackInto(player, menu, stack, destSlots, /*onlyMerge*/ true);
            if (!stack.isEmpty()) {
                stack = tryMoveStackInto(player, menu, stack, destSlots, /*onlyMerge*/ false);
            }
            s.set(stack); // leftover stays in source slot
        }
    }

    /** Moves as much as possible of 'stack' into the destination slots. Returns the leftover. */
    private static ItemStack tryMoveStackInto(ServerPlayer player, AbstractContainerMenu menu, ItemStack stack, Iterable<Slot> destSlots, boolean onlyMerge) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();

        for (Slot d : destSlots) {
            if (!d.mayPlace(remaining)) continue;
            // If destination is a player slot and it's locked, enforce signature when config is OFF
            if (d.container instanceof Inventory) {
                int invIdx = d.getContainerSlot();
                if (SlotLockCommon.isLocked(player, invIdx)) {
                    if (ConfigProvider.slotLockStopQuickMoves()) {
                        // config ON: do not place into locked slots at all
                        continue;
                    } else if (!SlotLockCommon.allowsItem(player, invIdx, remaining)) {
                        continue; // locked but non-matching → skip
                    }
                }
            }
            ItemStack existing = d.getItem();

            if (onlyMerge) {
                if (existing.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(existing, remaining)) continue;
                int max = Math.min(existing.getMaxStackSize(), d.getMaxStackSize());
                int canMove = Math.min(remaining.getCount(), max - existing.getCount());
                if (canMove > 0) {
                    existing.grow(canMove);
                    remaining.shrink(canMove);
                    d.set(existing);
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            } else {
                if (!existing.isEmpty()) continue;
                int toMove = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), d.getMaxStackSize()));
                if (toMove > 0) {
                    ItemStack place = remaining.copy();
                    place.setCount(toMove);
                    try {
                        QuickOpContext.enter();
                        d.set(place);
                    } finally {
                        QuickOpContext.exit();
                    }
                    remaining.shrink(toMove);
                    if (remaining.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        return remaining;
    }

    private QuickStackCommon() {}
}