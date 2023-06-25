package pextystudios.emogg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.EmojiFontRenderer;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method="<init>*", at=@At("RETURN"))
    private void init(GameConfig gameConfig, CallbackInfo ci) {
        Emogg.LOGGER.info("SETUPPED");
        Minecraft.getInstance().font = new EmojiFontRenderer();
    }
}