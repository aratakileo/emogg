package io.github.aratakileo.emogg.mixin;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.emoji.EmojiFontSet;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(FontManager.class)
public class FontManagerMixin {
    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private Map<ResourceLocation, FontSet> fontSets;

    @Inject(method = "apply", at = @At("TAIL"))
    private void postReload(FontManager.Preparation preparation, ProfilerFiller profilerFiller, CallbackInfo ci) {
        this.fontSets.put(EmojiFontSet.NAME, new EmojiFontSet(this.textureManager));
        Emogg.LOGGER.info("Internal emoji font registered! ("+ EmojiFontSet.NAME+")");
    }
}
