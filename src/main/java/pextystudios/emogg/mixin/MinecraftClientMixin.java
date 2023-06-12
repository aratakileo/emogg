package pextystudios.emogg.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.EmojiTextRenderer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method="<init>*", at=@At("RETURN"))
    private void init(RunArgs args, CallbackInfo ci) {
        Emogg.LOGGER.info("SETUPPED");
        MinecraftClient.getInstance().textRenderer = new EmojiTextRenderer();
    }
}
