package pextystudios.emogg.mixin;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(CommandSuggestor.class)
public abstract class CommandSuggestorMixin {
    private static final Pattern COLON_PATTERN = Pattern.compile("[^A-Za-z0-9](:)");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    @Shadow @Final TextFieldWidget textField;

    @Shadow @Nullable private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Shadow @Final private boolean slashOptional;

    @Inject(method="refresh",at=@At("TAIL"),cancellable = true)
    private void inject(CallbackInfo ci){
        Emogg.LOGGER.info("Process: CommandSuggestorMixin");

        String text = this.textField.getText();
        StringReader stringReader = new StringReader(text);
        boolean hasSlash = stringReader.canRead() && stringReader.peek() == '/';
        if (hasSlash) {
            stringReader.skip();
        }
        boolean isCommand = this.slashOptional || hasSlash;
        int cursor = this.textField.getCursor();
        if (!isCommand) {
            String textUptoCursor = text.substring(0, cursor);
            int start = Math.max(getLastPattern(textUptoCursor, COLON_PATTERN) - 1, 0);
            int whitespace = getLastPattern(textUptoCursor, WHITESPACE_PATTERN);
            if(start < textUptoCursor.length() && start >= whitespace){
                if(textUptoCursor.charAt(start) == ':') {
                    this.pendingSuggestions = CommandSource.suggestMatching(Emogg.getInstance().getEmojiSuggestions(), new SuggestionsBuilder(textUptoCursor, start));
                    this.pendingSuggestions.thenRun(() -> {
                        if (!this.pendingSuggestions.isDone()) {
                            return;
                        }
                        this.showSuggestions(false);
                    });
                    ci.cancel();
                }
            }
        }
    }

    private int getLastPattern(String input, Pattern pattern){
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }
}
