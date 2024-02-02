package io.github.aratakileo.emogg.mixin.component;

import io.github.aratakileo.emogg.emoji.EmojiParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MutableComponent.class)
public abstract class MutableComponentMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ComponentContents componentContents, List<Component> list, Style style, CallbackInfo ci) {
        if (EmojiParser.isOnLogicalClient())
            EmojiParser.parse((MutableComponent) (Object) this);
    }
}
