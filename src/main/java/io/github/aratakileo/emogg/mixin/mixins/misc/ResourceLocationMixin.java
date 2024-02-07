package io.github.aratakileo.emogg.mixin.mixins.misc;

import io.github.aratakileo.emogg.mixin.MixinHelpers;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
    @Inject(method = "decompose", at = @At("RETURN"))
    private static void fixModdedPathLocation(String string, char c, CallbackInfoReturnable<String[]> cir) {
        var value = cir.getReturnValue();
        if (c == ':' && value[0].indexOf('/') != -1) {
            final var matcher = MixinHelpers.PATTERN_NAMESPACE.matcher(string);
            if (matcher.find()) {
                value[0] = matcher.group(1);
                value[1] = string.substring(0, matcher.start(1))
                        + string.substring(matcher.end(0));
            }
        }
    }
}
