package pextystudios.emogg.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.gui.screen.SettingsScreen;
import pextystudios.emogg.emoji.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;

import java.util.function.Consumer;

public class EmojiSelectionMenu extends AbstractWidget {
    private final static ResourceLocation settingsIcon = new ResourceLocation(
            Emogg.NAMESPACE,
            "gui/icon/settings_icon.png"
    );

    private final float emojiSize;
    private final Font font;

    private RenderUtil.Rect2i settingsButtonRect = null;
    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;

    public EmojiSelectionMenu(float emojiSize) {
        super(
                0,
                0,
                (int) ((emojiSize + 1) * 9) + 3,
                (int) (emojiSize * 9) + 4 + Minecraft.getInstance().font.lineHeight
        );

        this.visible = false;
        this.emojiSize = emojiSize;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        disableHint();

        if (settingsButtonRect == null)
            settingsButtonRect = new RenderUtil.Rect2i(
                    x + width - font.lineHeight - 1,
                    y + 1,
                    font.lineHeight
            );

        /*
        * Start of header processing & rendering
        */
        RenderUtil.drawRect(x, y, width, (int) emojiSize, 0xaa000000);
        renderString(guiGraphics, "Emogg", 2, 2, 0x6c757d);

        if (settingsButtonRect.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(settingsButtonRect.move(-1, -1).expand(2, 2), 0x77ffffff);
            setHint(Component.translatable("emogg.settings.title"));
        }

        RenderUtil.renderTexture(
                guiGraphics,
                settingsIcon,
                settingsButtonRect
        );

        /*
        * Start of content processing & rendering
        */
        RenderUtil.drawRect(
                x,
                (int) (y + emojiSize),
                width,
                (int) (height - emojiSize),
                0xaa222222,
                1,
                0xaa000000
        );

        final var mouseColumn = (int) ((mouseX - x) / (emojiSize + 1));
        final var mouseLine = (int) ((mouseY - y) / (emojiSize + 1)) - 1;

        var icolumn = 0;
        var iline = 0;

        hoveredEmoji = null;

        for (var emoji: EmojiHandler.getInstance().getEmojis()) {
            var emojiX = x + icolumn * (emojiSize + 1) + 1;
            var emojiY = y + emojiSize + iline * (emojiSize + 1) + 1;

            if (mouseColumn == icolumn && mouseLine == iline) {
                hoveredEmoji = emoji;
                setHint(emoji.getEscapedCode());
                RenderUtil.drawRect((int) emojiX, (int) emojiY, (int) emojiSize, (int) emojiSize, 0x77ffffff);
            }

            emoji.render((int) (emojiX + 1), (int) (emojiY + 1), (int) (emojiSize - 2), guiGraphics);

            icolumn++;

            if (icolumn > 8) {
                icolumn = 0;
                iline++;
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
