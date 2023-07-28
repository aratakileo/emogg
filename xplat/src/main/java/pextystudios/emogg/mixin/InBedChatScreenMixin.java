package pextystudios.emogg.mixin;

import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pextystudios.emogg.util.KeyboardUtil;

@Mixin(InBedChatScreen.class)
public abstract class InBedChatScreenMixin extends ChatScreenMixin {
    protected InBedChatScreenMixin(Component $$0) {
        super($$0);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emogg$emojiSelectionMenu.visible || keyCode != KeyboardUtil.K_ESC) return;

        emogg$emojiSelectionMenu.visible = false;

        cir.setReturnValue(true);
    }
}
