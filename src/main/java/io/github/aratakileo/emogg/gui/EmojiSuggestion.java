package io.github.aratakileo.emogg.gui;

import io.github.aratakileo.emogg.handler.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.suggestionsapi.suggestion.Suggestion;
import io.github.aratakileo.suggestionsapi.suggestion.SuggestionRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class EmojiSuggestion implements Suggestion, SuggestionRenderer {
    private final Emoji emoji;

    public EmojiSuggestion(@NotNull Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public @NotNull String getSuggestionText() {
        return emoji.getCode();
    }

    @Override
    public int getWidth() {
        return EmojiLiteral.DEFAULT_RENDER_SIZE + EmojiFont.getInstance().width(emoji.getCode(), false) + 6;
    }

    @Override
    public int renderContent(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int x, int y, int color) {
        EmojiUtil.render(emoji, guiGraphics, x + 1, y, EmojiLiteral.DEFAULT_RENDER_SIZE);

        return guiGraphics.drawString(
                font,
                emoji.getEscapedCode(),
                x + EmojiLiteral.DEFAULT_RENDER_SIZE + 3,
                y,
                color
        );
    }
}
