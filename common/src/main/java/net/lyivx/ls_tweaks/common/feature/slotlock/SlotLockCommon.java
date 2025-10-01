package net.lyivx.ls_tweaks.common.feature.slotlock;

import dev.architectury.event.events.common.TickEvent;
import net.lyivx.ls_tweaks.common.config.ConfigProvider;
import net.lyivx.ls_tweaks.common.network.QolNet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Server-side slot lock state as a bitmask for 36 player inventory slots (0..35). */
public final class SlotLockCommon {
    private static final java.util.Map<java.util.UUID, Integer> PLAYER_MASK = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, ItemSignature[]> PLAYER_SIG = new java.util.concurrent.ConcurrentHashMap<>();

    public static void handleToggle(ServerPlayer sp, QolNet.SlotLockToggleC2SPayload p) {
        if (sp == null) return;
        int idx = p.playerInvIndex();
        if (idx < 0 || idx >= 36) return;
        int mask = PLAYER_MASK.getOrDefault(sp.getUUID(), 0);
        if (p.lock()) mask |= (1 << idx); else mask &= ~(1 << idx);
        PLAYER_MASK.put(sp.getUUID(), mask);
        // update signature
        ItemSignature[] sig = PLAYER_SIG.computeIfAbsent(sp.getUUID(), k -> new ItemSignature[36]);
        if (p.lock()) {
            var stack = sp.getInventory().getItem(idx);
            sig[idx] = ItemSignature.from(stack);
        } else {
            sig[idx] = null;
        }
        // sync to client (mask + signatures list)
        java.util.List<net.minecraft.world.item.ItemStack> list = new java.util.ArrayList<>(36);
        for (int i = 0; i < 36; i++) list.add(sig[i] == null ? null : sig[i].proto());
        dev.architectury.networking.NetworkManager.sendToPlayer(sp, new QolNet.SlotLockSyncS2CPayload(mask, list));
    }

    public static boolean isLocked(ServerPlayer sp, int invIndex) {
        if (sp == null || invIndex < 0 || invIndex >= 36) return false;
        int mask = PLAYER_MASK.getOrDefault(sp.getUUID(), 0);
        return (mask & (1 << invIndex)) != 0;
    }
    public static boolean allowsItem(ServerPlayer sp, int invIndex, net.minecraft.world.item.ItemStack stack) {
        ItemSignature[] sig = PLAYER_SIG.get(sp.getUUID());
        if (sig == null) return true;
        ItemSignature s = sig[invIndex];
        if (s == null) return true; // no remembered
        return s.matches(stack);
    }

    /** Common registration for server-side validation to enforce locks on any insertion path. */
    public static void register() {
        TickEvent.PLAYER_POST.register(player -> onPlayerTick(player));
    }

    private static void onPlayerTick(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        int mask = PLAYER_MASK.getOrDefault(sp.getUUID(), 0);
        if (mask == 0) return;
        if (!ConfigProvider.slotLockEnabled()) return;
        Inventory inv = sp.getInventory();
        ItemSignature[] sig = PLAYER_SIG.get(sp.getUUID());
        if (sig == null) return;
        // For each locked slot, ensure the content matches the signature (if any)
        for (int i = 0; i < 36; i++) {
            if ((mask & (1 << i)) == 0) continue;
            ItemStack inSlot = inv.getItem(i);
            ItemSignature s = sig[i];
            if (s == null) {
                // If locked but no signature, clear any content out of it
                if (!inSlot.isEmpty()) {
                    moveOrDrop(sp, i, inSlot.copy());
                    inv.setItem(i, ItemStack.EMPTY);
                }
                continue;
            }
            // If slot has item and it doesn't match, relocate it
            if (!inSlot.isEmpty() && !ItemStack.isSameItemSameComponents(inSlot, s.proto())) {
                moveOrDrop(sp, i, inSlot.copy());
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private static void moveOrDrop(ServerPlayer sp, int fromIdx, ItemStack stack) {
        Inventory inv = sp.getInventory();
        // Try merge into existing allowed stacks first
        for (int i = 0; i < 36 && !stack.isEmpty(); i++) {
            if (i == fromIdx) continue;
            if (isLocked(sp, i)) continue;
            ItemStack dst = inv.getItem(i);
            if (!dst.isEmpty() && ItemStack.isSameItemSameComponents(dst, stack)) {
                int max = Math.min(dst.getMaxStackSize(), inv.getMaxStackSize());
                int can = Math.min(stack.getCount(), max - dst.getCount());
                if (can > 0) {
                    dst.grow(can);
                    stack.shrink(can);
                    inv.setItem(i, dst);
                }
            }
        }
        // Then place into empty allowed slots
        for (int i = 0; i < 36 && !stack.isEmpty(); i++) {
            if (i == fromIdx) continue;
            if (isLocked(sp, i)) continue;
            if (inv.getItem(i).isEmpty()) {
                inv.setItem(i, stack.copy());
                stack.setCount(0);
            }
        }
        // If still leftovers, drop
        if (!stack.isEmpty()) {
            sp.drop(stack, false);
        }
    }

    private record ItemSignature(net.minecraft.world.item.ItemStack proto) {
        static ItemSignature from(net.minecraft.world.item.ItemStack st) {
            if (st == null || st.isEmpty()) return null;
            return new ItemSignature(st.copy());
        }
        boolean matches(net.minecraft.world.item.ItemStack st) {
            if (st == null || st.isEmpty()) return false;
            return net.minecraft.world.item.ItemStack.isSameItemSameComponents(st, proto);
        }
    }

    private SlotLockCommon() {}
}


