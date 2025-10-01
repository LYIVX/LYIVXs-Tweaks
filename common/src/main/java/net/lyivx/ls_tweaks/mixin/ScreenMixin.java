package net.lyivx.ls_tweaks.mixin;

import net.lyivx.ls_tweaks.client.overlay.OverlayHooks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof AbstractContainerScreen<?> screen) {
            if (OverlayHooks.keyPressed(screen, keyCode, scanCode, modifiers)) {
                cir.setReturnValue(true);
            }
        }
    }
}
