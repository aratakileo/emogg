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
    public final static int MAX_NUMBER_OF_EMOJIS_IN_LINE = 9,
            MAX_NUMBER_OF_EMOJIS_IN_COLUMN = 8,
            MAX_NUMBER_OF_EMOJIS_IN_GRID = MAX_NUMBER_OF_EMOJIS_IN_LINE * MAX_NUMBER_OF_EMOJIS_IN_COLUMN,
            SCROLLBAR_WIDTH = 5;

    private final static ResourceLocation settingsIcon = new ResourceLocation(
            Emogg.NAMESPACE,
            "gui/icon/settings_icon.png"
    );

    private final float emojiSize;
    private final Font font;
    private final int headerHeight, emojiLinesAmount;
    private final RenderUtil.Rect2i settingsButtonRect;
    private final boolean isSinglePage;

    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;
    private int scrollLinesAmount = 0;

    protected EmojiSelectionMenu(float emojiSize, int headerHeight) {
        super(
                0,
                0,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_EMOJIS_IN_LINE) + 1 + SCROLLBAR_WIDTH,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_EMOJIS_IN_COLUMN) + 1 + headerHeight
        );

        this.isSinglePage = EmojiHandler.getInstance().getNumberOfEmojis() < MAX_NUMBER_OF_EMOJIS_IN_GRID;

        if (isSinglePage)
            width -= SCROLLBAR_WIDTH;

        this.visible = false;
        this.emojiSize = emojiSize;
        this.font = Minecraft.getInstance().font;
        this.headerHeight = headerHeight;
        this.emojiLinesAmount = (int) Math.ceil(
                (double) EmojiHandler.getInstance().getNumberOfEmojis() / (double) MAX_NUMBER_OF_EMOJIS_IN_LINE
        );
        this.settingsButtonRect = new RenderUtil.Rect2i(
                width - font.lineHeight - 3,
                1,
                font.lineHeight
        );

        setHintPositioner(MOUSE_HINT_POSITIONER);
    }

    public EmojiSelectionMenu(float emojiSize) {
        this(emojiSize, Minecraft.getInstance().font.lineHeight + 3);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        disableHint();

        final var settingsButtonRenderRect = settingsButtonRect.move(x, y);

        /*
        * Start of header processing & rendering
        */
        RenderUtil.drawRect(x, y, width, (int) emojiSize, 0xaa000000);
        renderString(guiGraphics, "Emogg", 2, 2, 0x6c757d);

        if (settingsButtonRenderRect.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(settingsButtonRenderRect.expand(2, 2), 0x77ffffff);
            setHint(Component.translatable("emogg.settings.title"));
        }

        RenderUtil.renderTexture(
                guiGraphics,
                settingsIcon,
                settingsButtonRenderRect.move(1, 1)
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

        for (
                var emoji: EmojiHandler.getInstance()
                .getEmojiStream()
                .skip((long) scrollLinesAmount * MAX_NUMBER_OF_EMOJIS_IN_LINE)
                .limit(MAX_NUMBER_OF_EMOJIS_IN_GRID)
                .toList()
        ) {
            var emojiX = x + icolumn * (emojiSize + 1) + 1;
            var emojiY = y + emojiSize + iline * (emojiSize + 1) + 1;

            if (mouseColumn == icolumn && mouseLine == iline) {
                hoveredEmoji = emoji;
                setHint(emoji.getEscapedCode());
                RenderUtil.drawRect((int) emojiX, (int) emojiY, (int) emojiSize, (int) emojiSize, 0x77ffffff);
            }

            emoji.render((int) (emojiX + 1), (int) (emojiY + 1), (int) (emojiSize - 2), guiGraphics);

            icolumn++;

            if (icolumn > MAX_NUMBER_OF_EMOJIS_IN_LINE - 1) {
                icolumn = 0;
                iline++;
            }
        }

        /*
        * Start of scrollbar processing & rendering
        */
        if (isSinglePage) return;

        final var scrollbarX = getRight() - SCROLLBAR_WIDTH;
        final var scrollbarY = y + headerHeight;
        final var scrollbarHeight = height - headerHeight;
        final var scrollbarThumbHeight = Math.max(
                8,
                scrollbarHeight - 2 - (emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN) / 2
        );

        RenderUtil.drawRect(
                scrollbarX,
                scrollbarY,
                SCROLLBAR_WIDTH,
                scrollbarHeight,
                0xaa222222,
                1,
                0xaa000000
        );

        RenderUtil.drawRect(
                scrollbarX,
                (int) (scrollbarY + 1 + (
                        scrollbarHeight - 2 - scrollbarThumbHeight
                ) * (
                        (double)scrollLinesAmount / (double)(emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN)
                )),
                SCROLLBAR_WIDTH,
                scrollbarThumbHeight,
                0xaa222222,
                1,
                0xffffffff
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!isActive() || !isHovered || isSinglePage)
            return false;

        scrollLinesAmount = Math.min(
                emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN,
                Math.max(0, scrollLinesAmount - (int)scrollDelta)
        );

        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (settingsButtonRect.move(x, y).contains((int) mouseX, (int) mouseY)) {
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
