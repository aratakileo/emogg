package io.github.aratakileo.emogg.gui;

import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.emoji.EmojiGlyph;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.suggestionsapi.suggestion.Suggestion;
import io.github.aratakileo.suggestionsapi.suggestion.SuggestionRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class EmojiSuggestion implements Suggestion, SuggestionRenderer {
    private final Emoji emoji;

    public EmojiSuggestion(@NotNull Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public @NotNull String getText() {
        return emoji.getCode();
    }

    @Override
    public int getWidth() {
        return (int) (EmojiGlyph.HEIGHT + Minecraft.getInstance().font.width(emoji.getCode()) + 6);
    }

    @Override
    public int renderContent(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int x, int y, int color) {
        EmojiUtil.render(emoji.getGlyph(), guiGraphics, x + 1, y, (int) EmojiGlyph.HEIGHT, false);

        return guiGraphics.drawString(
                font,
                emoji.getCode(),
                (int) (x + EmojiGlyph.HEIGHT + 3),
                y,
                color
        );
    }
}
