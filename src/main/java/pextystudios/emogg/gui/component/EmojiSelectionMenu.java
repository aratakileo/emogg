package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.gui.screen.SettingsScreen;
import pextystudios.emogg.handler.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;

import java.util.function.Consumer;

public class EmojiSelectionMenu extends AbstractWidget {
    private final static ResourceLocation settingsIcon = new ResourceLocation(
            Emogg.NAMESPACE,
            "gui/icon/emoji-selector-settings.png"
    );

    private final int emojiSize;
    private final float headerPadding;
    private final Font font;

    private RenderUtil.Rect2i settingsButtonRect = null;
    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;

    protected EmojiSelectionMenu(int width, int height) {
        super(0, 0, width, height);
        this.visible = false;
        this.emojiSize = height - Minecraft.getInstance().screen.height / 3;
        this.font = Minecraft.getInstance().font;
        this.headerPadding = (float) (emojiSize - font.lineHeight) / 2;
    }

    public EmojiSelectionMenu() {
        this(
                Minecraft.getInstance().screen.height / 3,
                Minecraft.getInstance().screen.height / 3 + (Minecraft.getInstance().screen.height / 3 - 10) / 9
        );
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        disableHint();

        if (settingsButtonRect == null)
            settingsButtonRect = new RenderUtil.Rect2i(
                    x + width - font.lineHeight - 2,
                    y + 2,
                    font.lineHeight
            );

        RenderUtil.drawRect(x, y, width, emojiSize, 0xaa000000);
        font.draw(poseStack, "Emogg", x + headerPadding * 2, y + headerPadding, 0x6c757d);

        if (settingsButtonRect.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(settingsButtonRect.move(-2, -2).expand(4, 4), 0x77ffffff);
            setHint(new TranslatableComponent("emogg.settings.title"));
        }

        RenderUtil.renderTexture(
                poseStack,
                settingsIcon,
                settingsButtonRect
        );
        RenderUtil.drawRect(
                x,
                y + emojiSize,
                width,
                height - emojiSize,
                0xaa222222,
                1,
                0xaa000000
        );

        final var mouseColumn = (mouseX - x) / (emojiSize + 1);
        final var mouseLine = (mouseY - y) / (emojiSize + 1) - 1;

        var column = 0;
        var line = 0;

        hoveredEmoji = null;

        for (var emoji: EmojiHandler.getInstance().getEmojis()) {
            var emojiX = x + column * (emojiSize + 1) + 1;
            var emojiY = y + emojiSize + line * (emojiSize + 1) + 1;

            if (mouseColumn == column && mouseLine == line) {
                hoveredEmoji = emoji;
                setHint(emoji.getEscapedCode());
                RenderUtil.drawRect(emojiX, emojiY, emojiSize, emojiSize, 0x77ffffff);
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
    public void onClick(double mouseX, double mouseY) {
        if (settingsButtonRect.contains((int) mouseX, (int) mouseY)) {
            playClickSound();
            Minecraft.getInstance().setScreen(new SettingsScreen());
        }

        super.onClick(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        super.onPress();

        if (onEmojiSelected != null && hoveredEmoji != null) {
            playClickSound();
            onEmojiSelected.accept(hoveredEmoji);
        }
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }
}
