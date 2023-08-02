package pextystudios.emogg.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import pextystudios.emogg.emoji.font.EmojiLiteral;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.emoji.handler.EmojiHandler;
import pextystudios.emogg.util.EmojiUtil;

public class EmojiButton extends Button {
    private Emoji displayableEmoji = null, prevDisplayableEmoji = null;

    public EmojiButton(int x, int y) {
        this(x, y, EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE);
    }

    public EmojiButton(int x, int y, int size) {
        super(
                x,
                y,
                size,
                size
        );

        changeDisplayableEmoji();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        var hasBeenHovered = isHovered;

        super.render(guiGraphics, mouseX, mouseY, dt);

        if (!hasBeenHovered && isHovered)
            changeDisplayableEmoji();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        if (displayableEmoji == null) {
            disableHint();
            return;
        }

        setHint(displayableEmoji.getEscapedCode());

        int renderX = x, renderY = y, renderSize = width;

        if (isHovered) {
            renderX -= 1;
            renderY -= 1;
            renderSize += 2;
        }

        EmojiUtil.render(displayableEmoji, guiGraphics, renderX, renderY, renderSize);
    }

    protected void changeDisplayableEmoji() {
        prevDisplayableEmoji = displayableEmoji;

        EmojiHandler.getInstance()
                .getRandomEmoji()
                .ifPresent(emoji -> displayableEmoji = emoji);

        if (displayableEmoji == null || prevDisplayableEmoji == null) return;

        while (displayableEmoji.getName().equals(prevDisplayableEmoji.getName()))
            EmojiHandler.getInstance()
                    .getRandomEmoji()
                    .ifPresent(emoji -> displayableEmoji = emoji);
    }
}
