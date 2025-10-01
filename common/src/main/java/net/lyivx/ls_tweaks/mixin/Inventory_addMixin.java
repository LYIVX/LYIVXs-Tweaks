package net.lyivx.ls_tweaks.mixin;

import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockCommon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class Inventory_addMixin {
    @Shadow public abstract ItemStack getItem(int slot);
    @Shadow public abstract void setItem(int slot, ItemStack stack);
    @Shadow public abstract int getContainerSize();

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void ls_tweaks$skipLockedOnAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Inventory self = (Inventory)(Object)this;
        if (!(self.player instanceof ServerPlayer sp)) return;
        if (stack == null || stack.isEmpty()) return;

        int original = stack.getCount();
        int remaining = original;

        // First: try merge into existing stacks that are allowed
        for (int i = 0; i < 36 && remaining > 0; i++) {
            if (SlotLockCommon.isLocked(sp, i) && !SlotLockCommon.allowsItem(sp, i, stack)) continue;
            ItemStack cur = getItem(i);
            if (!cur.isEmpty() && ItemStack.isSameItemSameComponents(cur, stack)) {
                int max = cur.getMaxStackSize();
                int can = Math.min(remaining, max - cur.getCount());
                if (can > 0) {
                    cur.grow(can);
                    remaining -= can;
                    setItem(i, cur);
                }
            }
        }
        // Then: fill empty allowed slots
        for (int i = 0; i < 36 && remaining > 0; i++) {
            if (SlotLockCommon.isLocked(sp, i) && !SlotLockCommon.allowsItem(sp, i, stack)) continue;
            ItemStack cur = getItem(i);
            if (cur.isEmpty()) {
                ItemStack place = stack.copy();
                place.setCount(remaining);
                setItem(i, place);
                remaining = 0;
            }
        }

        // Cancel vanilla logic regardless, so it doesn't try placing into locked slots
        stack.setCount(remaining);
        cir.setReturnValue(remaining != original);
    }
}
