package net.lyivx.ls_tweaks.mixin;

import net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockCommon;
import net.lyivx.ls_tweaks.common.util.QuickOpContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class Slot_mayPlaceMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void ls_tweaks$blockLockedPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Slot self = (Slot)(Object)this;
		if (!(self.container instanceof Inventory inv)) return;
		// Only player inventory slots we track (0..35)
		int idx = self.getContainerSlot();
		if (idx < 0 || idx >= 36) return;
        if (inv.player instanceof ServerPlayer sp) {
            if (QuickOpContext.isActive()) return; // allow internal quick ops to place
            if (!SlotLockCommon.isLocked(sp, idx)) return;
            boolean allows = SlotLockCommon.allowsItem(sp, idx, stack);
            if (!allows) cir.setReturnValue(false);
        } else {
            // Client side: use client cache to prevent ghost previews from showing as if allowed
            if (QuickOpContext.isActive()) return; // allow internal quick ops to place
            boolean allowsClient = net.lyivx.ls_tweaks.common.feature.slotlock.SlotLockClient.allowsClientPlacement(idx, stack);
            if (!allowsClient) cir.setReturnValue(false);
        }
	}
}
