package net.lyivx.ls_tweaks.common.util;

import net.lyivx.ls_tweaks.common.debug.QolDebug;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.core.registries.BuiltInRegistries.ITEM;

public final class InvUtils {

    public static ItemStack findMatchingInBackpack(Player player, ItemStack signature, boolean strict) {
        var inv = player.getInventory();

        for (int i = 9; i < inv.items.size(); i++) {
            var st = inv.items.get(i);
            if (matches(st, signature, strict)) {
                return st;
            }
        }
        for (int i = 0; i < 9; i++) {
            var st = inv.items.get(i);
            if (matches(st, signature, strict)) {
                return st;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean matches(ItemStack stack, ItemStack signature, boolean strict) {
        if (stack.isEmpty() || signature.isEmpty()) return false;
        if (strict) {
            return ItemStack.isSameItemSameComponents(stack, signature);
        } else {
            return BuiltInRegistries.ITEM.getKey(stack.getItem())
                    .equals(BuiltInRegistries.ITEM.getKey(signature.getItem()));
        }
    }

    public static boolean moveIntoHotbarSlot(Player player, ItemStack source, int hotbarSlot) {
        if (source.isEmpty() || hotbarSlot < 0 || hotbarSlot > 8) return false;

        var inv = player.getInventory();
        ItemStack target = inv.getItem(hotbarSlot);

        if (target.isEmpty()) {
            inv.setItem(hotbarSlot, source.copy());
            source.setCount(0);
            inv.setChanged();
            player.containerMenu.broadcastChanges();
            return true;
        } else if (ItemStack.isSameItemSameComponents(source, target)) {
            int canMove = Math.min(source.getCount(), target.getMaxStackSize() - target.getCount());
            if (canMove > 0) {
                target.grow(canMove);
                source.shrink(canMove);
                inv.setChanged();
                player.containerMenu.broadcastChanges();
                return true;
            }
        }
        return false;
    }
}