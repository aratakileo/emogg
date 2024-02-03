package io.github.aratakileo.emogg.mixin.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.aratakileo.emogg.emoji.EmojiGlyph;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Font.class)
public class FontMixin {
    @Inject(
            method = "renderChar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;render(ZFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V",
                    ordinal = 1
            ),
            cancellable = true)
    private void noBoldDoubleRenderForEmojis(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h, Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j, float k, float l, int m, CallbackInfo ci) {
        if (bakedGlyph instanceof EmojiGlyph) ci.cancel();
    }
}
