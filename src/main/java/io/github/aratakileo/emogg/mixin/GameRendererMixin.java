package io.github.aratakileo.emogg.mixin;

import io.github.aratakileo.emogg.emoji.EmojiGlyphRenderTypes;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(
            method = "reloadShaders",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    void loadCustomShaders(ResourceProvider resourceProvider, CallbackInfo ci, List list, List list2) throws IOException {
        EmojiGlyphRenderTypes.Shaders.loadShaders(resourceProvider, list2);
    }
}
