package io.github.aratakileo.emogg.gui.widget;

import io.github.aratakileo.elegantia.gui.widget.AbstractButton;
import io.github.aratakileo.elegantia.math.Rect2i;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class EmojiButton extends AbstractButton {
    private Emoji displayableEmoji = null, prevDisplayableEmoji = null;

    public EmojiButton(@NotNull Rect2i rect2i) {
        super(rect2i, null);

        changeDisplayableEmoji();
    }

    public @Nullable Emoji getDisplayableEmoji() {
        return displayableEmoji;
    }

    public void setDisplayableEmoji(@NotNull Emoji displayableEmoji) {
        this.prevDisplayableEmoji = this.displayableEmoji;
        this.displayableEmoji = displayableEmoji;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        if (displayableEmoji == null) {
            disableTooltip();
            return;
        }

        if (!wasHovered && isHovered)
            changeDisplayableEmoji();

        setTooltip(displayableEmoji.getEscapedCode());

        final var renderPos = getPosition().copyAsMutable();
        var renderSize = getWidth();

        if (isHovered) {
            renderPos.sub(1, 1);
            renderSize += 2;
        }

        EmojiUtil.render(
                displayableEmoji.getGlyph(),
                guiGraphics,
                renderPos,
                renderSize,
                !isHovered
        );
    }

    protected void changeDisplayableEmoji() {
        prevDisplayableEmoji = displayableEmoji;

        EmojiManager.getInstance()
                .getRandomEmoji()
                .ifPresent(emoji -> displayableEmoji = emoji);

        if (displayableEmoji == null || prevDisplayableEmoji == null) return;

        while (displayableEmoji.getName().equals(prevDisplayableEmoji.getName()))
            EmojiManager.getInstance()
                    .getRandomEmoji()
                    .ifPresent(emoji -> displayableEmoji = emoji);
    }
}
