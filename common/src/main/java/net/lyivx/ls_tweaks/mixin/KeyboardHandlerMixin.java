package net.lyivx.ls_tweaks.mixin;

import net.lyivx.ls_tweaks.client.overlay.OverlayHooks;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void ls_tweaks$onCharTyped(long window, int codePoint, int modifiers, CallbackInfo ci) {
        Screen scr = Minecraft.getInstance().screen;
        if (scr instanceof AbstractContainerScreen<?> cs) {
            if (OverlayHooks.charTyped(cs, (char) codePoint, modifiers)) {
                ci.cancel();
            }
        }
    }
}


