package io.github.aratakileo.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.resource.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import org.jetbrains.annotations.NotNull;

public class EmojiButton extends Button {
    private Emoji displayableEmoji = null, prevDisplayableEmoji = null;

    public EmojiButton(int x, int y, int size) {
        super(
                x,
                y,
                size,
                size
        );

        changeDisplayableEmoji();
    }

    public Emoji getDisplayableEmoji() {
        return displayableEmoji;
    }

    public void setDisplayableEmoji(@NotNull Emoji displayableEmoji) {
        this.prevDisplayableEmoji = this.displayableEmoji;
        this.displayableEmoji = displayableEmoji;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        var hasBeenHovered = isHovered;

        super.render(poseStack, mouseX, mouseY, dt);

        if (!hasBeenHovered && isHovered)
            changeDisplayableEmoji();
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
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

        EmojiUtil.render(displayableEmoji, poseStack, renderX, renderY, renderSize);
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
