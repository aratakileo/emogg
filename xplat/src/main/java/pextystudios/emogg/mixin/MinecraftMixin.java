package pextystudios.emogg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.emoji.EmojiFontRenderer;
import pextystudios.emogg.mixin.accessor.MinecraftAccessor;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final public Font font;

    @Shadow
    @Final public Font fontFilterFishy;

    @Inject(method="<init>*", at=@At("RETURN"))
    private void init(GameConfig gameConfig, CallbackInfo ci) {
        ((MinecraftAccessor)this).setFont(new EmojiFontRenderer(font));
        ((MinecraftAccessor)this).setFontFilterFishy(new EmojiFontRenderer(fontFilterFishy));
    }
}
