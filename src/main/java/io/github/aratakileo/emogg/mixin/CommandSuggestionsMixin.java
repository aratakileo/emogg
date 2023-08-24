package io.github.aratakileo.emogg.mixin;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.aratakileo.emogg.resource.Emoji;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.aratakileo.emogg.handler.EmojiHandler;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Unique
    private static final Pattern COLON_PATTERN = Pattern.compile("[^A-Za-z0-9](:)");
    @Unique
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    @Shadow @Final
    EditBox input;

    @Shadow @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Shadow @Final
    private boolean commandsOnly;

    @Inject(method = "updateCommandInfo", at = @At("TAIL"), cancellable = true)
    private void updateCommandInfo(CallbackInfo ci){
        final var contentText = input.getValue();
        final var stringReader = new StringReader(contentText);
        final var hasSlash = stringReader.canRead() && stringReader.peek() == '/';
        final var cursorPosition = input.getCursorPosition();

        if (hasSlash)
            stringReader.skip();

        if (this.commandsOnly || hasSlash) return;

        final var textUptoCursor = contentText.substring(0, cursorPosition);
        final var semicolonStart = Math.max(getLastMatchedEnd(COLON_PATTERN) - 1, 0);
        final var whitespaceEnd = getLastMatchedEnd(WHITESPACE_PATTERN);

        if (
                semicolonStart >= textUptoCursor.length()
                        || semicolonStart < whitespaceEnd
                        || textUptoCursor.charAt(semicolonStart) != ':'
        ) return;

        pendingSuggestions = SharedSuggestionProvider.suggest(
                EmojiHandler.getInstance().getEmojisStream().map(Emoji::getCode),
                new SuggestionsBuilder(textUptoCursor, semicolonStart)
        );

        pendingSuggestions.thenRun(() -> {
            if (!pendingSuggestions.isDone()) return;
            showSuggestions(false);
        });

        ci.cancel();
    }

    @Unique
    private int getLastMatchedEnd(Pattern pattern){
        if (Strings.isNullOrEmpty(input.getValue())) {
            return 0;
        }

        final var matcher = pattern.matcher(input.getValue());
        var end = 0;

        while (matcher.find()) end = matcher.end();

        return end;
    }
}
