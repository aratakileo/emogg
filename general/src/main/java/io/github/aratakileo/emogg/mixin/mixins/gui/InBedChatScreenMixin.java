package io.github.aratakileo.emogg.mixin.mixins.gui;

import io.github.aratakileo.emogg.util.KeyboardUtil;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InBedChatScreen.class)
public class InBedChatScreenMixin extends ChatScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.visible || keyCode != KeyboardUtil.K_ESC) return;

        emojiSelectionMenu.visible = false;

        cir.setReturnValue(true);
    }
}
