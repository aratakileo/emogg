package io.github.aratakileo.emogg.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.emoji.parsing.EmojiParser;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Mixin(Component.Serializer.class)
public abstract class ComponentSerializerMixin {
    @Shadow public abstract JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext);

    @Inject(
            method = "serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("HEAD"),
            cancellable = true)
    private void serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext, CallbackInfoReturnable<JsonElement> cir) {
        final var original = EmojiParser.getOriginal(component);
        if (original != null) {
            if (EmoggConfig.instance.isDebugModeEnabled)
                Emogg.LOGGER.info("Serializing original component <"+original+"> of <"+component+">");
            cir.cancel();
            cir.setReturnValue(serialize(original, type, jsonSerializationContext));
        }
    }
}
