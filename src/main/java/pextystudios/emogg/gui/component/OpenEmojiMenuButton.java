package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.handler.EmojiHandler;

public class OpenEmojiMenuButton extends Button {
    private Emoji displayableEmoji = null, prevDisplayableEmoji = null;

    public OpenEmojiMenuButton(int x, int y, OnPress onPress) {
        this(x, y, EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE, EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE, onPress);
    }

    public OpenEmojiMenuButton(int x, int y, int width, int height, OnPress onPress) {
        super(
                x,
                y,
                width,
                height,
                TextComponent.EMPTY,
                onPress,
                (button, poseStack, mouseX, mouseY) -> {
                    var emojiButton = (OpenEmojiMenuButton)button;

                    if (emojiButton.displayableEmoji == null) return;

                    var currentScreen = Minecraft.getInstance().screen;

                    if (currentScreen == null) return;

                    var hintComponent = new TextComponent(emojiButton.displayableEmoji.getCode());

                    if (emojiButton.isHovered())
                        currentScreen.renderTooltip(poseStack, hintComponent, mouseX, mouseY);
                    else if (emojiButton.isFocusedButNotHovered())
                        currentScreen.renderTooltip(poseStack, hintComponent, button.x, button.y);
                }
        );

        changeDisplayableEmoji();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        var hasBeenHovered = isHovered;
        isHovered = collidePoint(mouseX, mouseY);

        if (!hasBeenHovered && isHovered)
            changeDisplayableEmoji();

        renderButton(poseStack, mouseX, mouseY, dt);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (displayableEmoji == null) return;

        displayableEmoji.render(x, y, width, height, poseStack);

        if (isHovered())
            renderToolTip(poseStack, mouseX, mouseY);
    }

    public boolean collidePoint(int x, int y) {
        return x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
    }

    public boolean isFocusedButNotHovered() {
        return !isHovered && isFocused();
    }

    protected void changeDisplayableEmoji() {
        prevDisplayableEmoji = displayableEmoji;

        EmojiHandler.getInstance()
                .getRandomEmoji(true)
                .ifPresent(emoji -> displayableEmoji = emoji);

        if (displayableEmoji == null || prevDisplayableEmoji == null) return;

        while (displayableEmoji.getName().equals(prevDisplayableEmoji.getName()))
            EmojiHandler.getInstance()
                    .getRandomEmoji(true)
                    .ifPresent(emoji -> displayableEmoji = emoji);
    }
}
