package io.github.aratakileo.emogg.mixin.component;

import io.github.aratakileo.emogg.emoji.EmojiParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Component.class)
public interface ComponentMixin {
    @Inject(method = "getString()Ljava/lang/String;",
            at = @At("HEAD"),
            cancellable = true)
    private void preGetString(CallbackInfoReturnable<String> cir) {
        EmojiParser.isInGetString = true;
        EmojiParser.mixinApplyUsingOriginal(
                (Component) this,
                cir,
                Component::getString,
                "Component.getString() - "
        );
    }

    @Inject(method = "getString(I)Ljava/lang/String;",
            at = @At("HEAD"),
            cancellable = true)
    private void preGetString(int i, CallbackInfoReturnable<String> cir) {
        EmojiParser.isInGetString = true;
        EmojiParser.mixinApplyUsingOriginal(
                (Component) this,
                cir,
                c -> c.getString(i),
                "Component.getString(int) - "
        );
    }

    @Inject(method = "getString()Ljava/lang/String;",
            at = @At("RETURN"))
    private void postGetString(CallbackInfoReturnable<String> cir) {
        EmojiParser.isInGetString = false;
    }

    @Inject(method = "getString(I)Ljava/lang/String;",
            at = @At("RETURN"))
    private void postGetString(int i, CallbackInfoReturnable<String> cir) {
        EmojiParser.isInGetString = false;
    }

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void preVisit(FormattedText.ContentConsumer<T> contentConsumer, CallbackInfoReturnable<Optional<T>> cir) {
        if (!EmojiParser.isInGetString) return;
        EmojiParser.mixinApplyUsingOriginal(
                (Component) this, cir,
                c -> c.visit(contentConsumer),
                "Component.visit(_) - "
        );
    }

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void preVisit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style, CallbackInfoReturnable<Optional<T>> cir) {
        if (!EmojiParser.isInGetString) return;
        EmojiParser.mixinApplyUsingOriginal(
                (Component) this, cir,
                c -> c.visit(styledContentConsumer, style),
                "Component.visit(_,Style) - "
        );
    }
}
