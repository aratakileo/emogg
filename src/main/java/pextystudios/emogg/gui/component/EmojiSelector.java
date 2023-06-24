package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.handler.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;

import java.util.function.Consumer;

public class EmojiSelector extends AbstractWidget {
    protected Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;

    public EmojiSelector() {
        super(0, 0, Minecraft.getInstance().screen.height / 3, Minecraft.getInstance().screen.height / 3);
        visible = false;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);

        final var emojiSize = (width - 10) / 9;
        final var mouseColumn = (mouseX - x) / (emojiSize + 1);
        final var mouseLine = (mouseY - y) / (emojiSize + 1);

        var column = 0;
        var line = 0;

        disableHint();
        hoveredEmoji = null;
        playClickSound = false;

        for (var emoji: EmojiHandler.getInstance().getEmojis()) {
            var emojiX = x + column * (emojiSize + 1) + 1;
            var emojiY = y + line * (emojiSize + 1) + 1;

            if (mouseColumn == column && mouseLine == line) {
                hoveredEmoji = emoji;
                playClickSound = true;
                setHint(emoji.getCode());
                RenderUtil.drawRect(emojiX, emojiY, emojiSize, emojiSize, 0xaaffffff);
            }

            emoji.render(emojiX + 1, emojiY + 1, emojiSize - 2, poseStack);

            column++;

            if (column > 8) {
                column = 0;
                line++;
            }
        }
    }

    @Override
    public void onPress() {
        super.onPress();

        if (onEmojiSelected != null && hoveredEmoji != null) onEmojiSelected.accept(hoveredEmoji);
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }
}
