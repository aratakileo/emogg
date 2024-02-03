package io.github.aratakileo.emogg.mixin.rendering;

import io.github.aratakileo.emogg.emoji.EmojiFontSet;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class StringRenderOutputMixin {
    @Shadow float x;

    @Shadow @Final private boolean dropShadow;

    @Inject(
            method = "accept",
            at = @At("HEAD"),
            cancellable = true
    )
    private void noShadowForEmojis(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        if (this.dropShadow && style.getFont().equals(EmojiFontSet.NAME)) {
            this.x += EmojiFontSet.getInstance()
                    .getGlyphInfo(i, false)
                    .getAdvance(style.isBold());
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
