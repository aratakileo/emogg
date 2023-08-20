package io.github.aratakileo.emogg.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import io.github.aratakileo.emogg.font.EmojiFont;
import io.github.aratakileo.emogg.font.EmojiLiteral;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.resource.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin {
    @Shadow @Final
    private Rect2i rect;

    @Unique
    private final HashMap<String, Emoji> emojis = new HashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(
            CommandSuggestions commandSuggestions,
            int x,
            int y,
            int width,
            List<Suggestion> suggestions,
            boolean bl,
            CallbackInfo ci
    ){
        for (var suggestion: suggestions) {
            final var suggestionText = suggestion.getText();
            final var matcher = EmojiLiteral.EMOJI_CODE_PATTERN.matcher(suggestionText);
            final String emojiName;

            if (matcher.find() && EmojiHandler.getInstance().hasEmoji(emojiName = matcher.group(2))) {
                emojis.put(matcher.group(1), EmojiHandler.getInstance().getEmoji(emojiName));
                rect.setWidth(Math.max(
                        rect.getWidth(),
                        EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE + 6 + ((EmojiFont)Minecraft.getInstance().font).width(
                                suggestionText,
                                false
                        )
                ));
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int updateCommandInfo(GuiGraphics instance, Font font, String string, int x, int y, int color){
        if (!emojis.containsKey(string)) return instance.drawString(font, string, x, y, color);

        EmojiUtil.render(emojis.get(string), instance, x + 1, y, EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE);

        return instance.drawString(
                font,
                '\\' + string,
                x + EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE + 3,
                y,
                color
        );
    }
}
