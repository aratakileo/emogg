package io.github.aratakileo.emogg.mixin.mixins.gui;

import io.github.aratakileo.emogg.util.HudRenderCallback;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At(value = "TAIL"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V")))
    public void render(GuiGraphics drawContext, float tickDelta, CallbackInfo callbackInfo) {
        HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickDelta);
    }
}
