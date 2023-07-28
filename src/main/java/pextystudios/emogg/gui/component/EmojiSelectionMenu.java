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
    private final RenderUtil.Rect2i settingsButtonRect, scrollbarRect, scrollbarThumbRect;
    private final boolean isSinglePage;

    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;
    private int scrollLinesAmount = 0;
    private int scrollingThumbMouseOffset = -1;

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
        this.scrollbarRect = new RenderUtil.Rect2i(
                width - SCROLLBAR_WIDTH,
                headerHeight,
                SCROLLBAR_WIDTH,
                height - headerHeight
        );
        this.scrollbarThumbRect = new RenderUtil.Rect2i(
                scrollbarRect.xPos + 1,
                scrollbarRect.yPos + 1,
                SCROLLBAR_WIDTH - 2,
                Math.max(8, scrollbarRect.height - 2 - (emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN) / 2)
        );

        setHintPositioner(MOUSE_HINT_POSITIONER);
    }

    public EmojiSelectionMenu(float emojiSize) {
        this(emojiSize, Minecraft.getInstance().font.lineHeight + 3);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        disableHint();

        final var renderableSettingsButtonRect = settingsButtonRect.move(x, y);

        /*
        * Start of header processing & rendering
        */
        RenderUtil.drawRect(x, y, width, (int) emojiSize, 0xaa000000);
        renderString(guiGraphics, "Emogg", 2, 2, 0x6c757d);

        if (!isScrolling() && renderableSettingsButtonRect.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(renderableSettingsButtonRect.expand(2, 2), 0x77ffffff);
            setHint(Component.translatable("emogg.settings.title"));
        }

        RenderUtil.renderTexture(
                guiGraphics,
                settingsIcon,
                renderableSettingsButtonRect.move(1, 1)
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

            if (!isScrolling() && mouseColumn == icolumn && mouseLine == iline) {
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

        RenderUtil.drawRect(
                scrollbarRect.move(x, y),
                0xaa222222,
                1,
                0xaa000000
        );

        final var renderableScrollbarThumbRect = scrollbarThumbRect.move(x, y);

        RenderUtil.drawRect(
                renderableScrollbarThumbRect,
                (renderableScrollbarThumbRect.contains(mouseX, mouseY)) ? 0xaacbcbcb : 0xaa6c757d
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!isActive() || !isHovered || isSinglePage)
            return false;

        scrollingThumbMouseOffset = -1;
        scrollLinesAmount = Math.min(
                emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN,
                Math.max(0, scrollLinesAmount - (int)scrollDelta)
        );

        scrollbarThumbRect.setY(
                (int) (scrollbarRect.yPos + 1 + (scrollbarRect.height - 2 - scrollbarThumbRect.height) * (
                        (double)scrollLinesAmount / (double)(emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN)
                ))
        );

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isScrolling()) return false;

        applyScrollByThumbY((int)(mouseY - y - scrollingThumbMouseOffset));

        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (settingsButtonRect.move(x, y).contains((int) mouseX, (int) mouseY)) {
            playClickSound();
            Minecraft.getInstance().setScreen(new SettingsScreen());
        }

        super.onClick(mouseX, mouseY);

        if (isSinglePage) return;

        final var renderableScrollbarThumbRect = scrollbarThumbRect.move(x, y);

        if (renderableScrollbarThumbRect.contains((int)mouseX, (int)mouseY)) {
            scrollingThumbMouseOffset = (int) (mouseY - renderableScrollbarThumbRect.yPos);
            return;
        }

        if (!scrollbarRect.move(x, y).contains((int)mouseX, (int)mouseY)) return;

        applyScrollByThumbY((int) (mouseY - y));
    }

    @Override
    public void onPress() {
        super.onPress();

        if (onEmojiSelected != null && hoveredEmoji != null) {
            playClickSound();
            onEmojiSelected.accept(hoveredEmoji);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        scrollingThumbMouseOffset = -1;
    }

    private void applyScrollByThumbY(int y) {
        final var scrollbarFieldHeight =  scrollbarRect.height - 2 - scrollbarThumbRect.height;

        scrollbarThumbRect.setY(Math.min(Math.max(y, headerHeight + 1), scrollbarFieldHeight + headerHeight + 1));
        scrollLinesAmount = (int) ((
                emojiLinesAmount - MAX_NUMBER_OF_EMOJIS_IN_COLUMN
        ) * (
                (double)(scrollbarThumbRect.yPos - headerHeight - 1) / (double)(scrollbarFieldHeight))
        );
    }

    private boolean isScrolling() {
        return scrollingThumbMouseOffset >= 0;
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }
}
