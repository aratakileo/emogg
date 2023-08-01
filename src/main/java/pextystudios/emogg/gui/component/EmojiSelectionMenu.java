package pextystudios.emogg.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.handler.FrequentlyUsedEmojiController;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.gui.screen.SettingsScreen;
import pextystudios.emogg.emoji.handler.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class EmojiSelectionMenu extends AbstractWidget {
    public final static int MAX_NUMBER_OF_EMOJIS_IN_LINE = 9,
            MAX_NUMBER_OF_LINES_ON_PAGE = 8,
            SCROLLBAR_WIDTH = 5;

    private final static ResourceLocation SETTINGS_ICON = new ResourceLocation(
            Emogg.NAMESPACE,
            "gui/icon/settings_icon.png"
    );

    private final float emojiSize;
    private final Font font;
    private final int headerHeight;
    private final RenderUtil.Rect2i settingsButtonRect, scrollbarRect, scrollbarThumbRect;
    private final LinkedHashMap<Integer, Segment> segments = new LinkedHashMap<>();
    private final boolean isSinglePage;

    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;
    private int scrollLinesAmount = 0, totalLinesAmount, scrollingThumbMouseOffset = -1;

    private void moveCategoryDown(List<String> categoryNames, String category) {
        moveCategoryTo(categoryNames, category, true);
    }

    private void moveCategoryTo(List<String> categoryNames, String category, boolean isMoveDown) {
        if (categoryNames.contains(category)) {
            categoryNames.remove(category);

            if (isMoveDown)
                categoryNames.add(category);
            else
                categoryNames.add(0, category);
        }
    }

    protected EmojiSelectionMenu(float emojiSize, int headerHeight) {
        super(
                0,
                0,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_EMOJIS_IN_LINE) + 1 + SCROLLBAR_WIDTH,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_LINES_ON_PAGE) + 1 + headerHeight
        );

        this.visible = false;
        this.emojiSize = emojiSize;
        this.font = Minecraft.getInstance().font;
        this.headerHeight = headerHeight;

        final var emojiHandler = EmojiHandler.getInstance();
        final var categoryNames = new java.util.ArrayList<>(emojiHandler.getCategories().stream().toList());
        var totalLinesAmount = 0;

        // Reordering categories
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_ANIME);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_PEOPLE);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_NATURE);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_FOOD);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_ACTIVITIES);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_TRAVEL);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_OBJECTS);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_SYMBOLS);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_FLAGS);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_DEFAULT);
        moveCategoryTo(categoryNames, FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED, false);

        for (var categoryName: categoryNames) {
            final var segment = new Segment(categoryName);

            if (segment.isEmpty()) continue;

            segments.put(totalLinesAmount, segment);

            totalLinesAmount += segment.getNumberOfLines();
        }

        totalLinesAmount--;

        this.isSinglePage = totalLinesAmount < MAX_NUMBER_OF_LINES_ON_PAGE;
        this.totalLinesAmount = totalLinesAmount;

        if (isSinglePage)
            width -= SCROLLBAR_WIDTH;

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
                Math.min(scrollbarRect.height - 12, Math.max(8, scrollbarRect.height - 2 - (totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE) / 2))
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
                SETTINGS_ICON,
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
        final var categoryNameOffsetY = (int) ((emojiSize - font.lineHeight) / 2);

        var iline = 0;

        hoveredEmoji = null;

        for (var segmentStartIndex: segments.keySet()) {
            final var currentSegment = segments.get(segmentStartIndex);
            final var indexDifference = scrollLinesAmount - segmentStartIndex;
            final var lineLocalY = (int) Math.ceil(emojiSize + iline * (emojiSize + 1) + 1) + 1;

            if (indexDifference > 0) {
                if (indexDifference > currentSegment.getNumberOfLines()) continue;

                iline = -indexDifference;
            } else {
                if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;

                if (iline >= 0)
                    renderString(
                            guiGraphics,
                            EmojiHandler.getDisplayableCategoryName(currentSegment.getName()),
                            2,
                            lineLocalY + categoryNameOffsetY,
                            0x6c757d
                    );

                iline++;

                if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
            }

            var icolumn = 0;

            for (var emoji: currentSegment.getEmojis()) {
                if (iline >= 0) {
                    final var emojiX = (int) (x + icolumn * (emojiSize + 1) + 1);
                    final var emojiY = (int) (y + emojiSize + iline * (emojiSize + 1) + 1);

                    if (!isScrolling() && mouseColumn == icolumn && mouseLine == iline) {
                        hoveredEmoji = emoji;
                        setHint(emoji.getEscapedCode());
                        RenderUtil.drawRect(emojiX, emojiY, (int) emojiSize, (int) emojiSize, 0x77ffffff);
                    }

                    emoji.getRenderer().render(guiGraphics, emojiX + 1, emojiY + 1, (int) (emojiSize - 2));
                }

                icolumn++;

                if (icolumn > MAX_NUMBER_OF_EMOJIS_IN_LINE - 1) {
                    icolumn = 0;
                    iline++;
                    if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
                }
            }

            if (currentSegment.getEmojis().size() % MAX_NUMBER_OF_EMOJIS_IN_LINE != 0) iline++;
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
                totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE,
                Math.max(0, scrollLinesAmount - (int)scrollDelta)
        );

        scrollbarThumbRect.setY(
                (int) (scrollbarRect.yPos + 1 + (scrollbarRect.height - 2 - scrollbarThumbRect.height) * (
                        (double)scrollLinesAmount / (double)(totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE)
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

    public void refreshRecentlyUsedEmojis() {
        final var recentlyUsedEmojis = FrequentlyUsedEmojiController.getEmojis();

        if (recentlyUsedEmojis.isEmpty() || segments.isEmpty()) return;
        else applyScrollByThumbY(headerHeight + 1);

        if (segments.get(0).getName().equals(FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED)) {
            segments.get(0).refreshEmojis();
        } else {
            final var recentlyUsedEmojisSegment = new Segment(FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED);

            if (!recentlyUsedEmojisSegment.isEmpty()) {
                final var newSegments = new LinkedHashMap<Integer, Segment>();
                final var indexOffset = recentlyUsedEmojisSegment.getNumberOfLines();

                newSegments.put(0, recentlyUsedEmojisSegment);

                for (var index : segments.keySet())
                    newSegments.put(index + indexOffset, segments.get(index));

                segments.clear();
                segments.putAll(newSegments);
            }
        }

        final var lastSegmentEntry = segments.entrySet().stream().toList().get(segments.size() - 1);
        totalLinesAmount = lastSegmentEntry.getKey() + lastSegmentEntry.getValue().numberOfLines - 1;
        scrollbarThumbRect.height = Math.min(scrollbarRect.height - 12, Math.max(8, scrollbarRect.height - 2 - (totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE) / 2));
        applyScrollByThumbY(headerHeight + 1);
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }

    private void applyScrollByThumbY(int localY) {
        final var scrollbarFieldHeight = scrollbarRect.height - 2 - scrollbarThumbRect.height;

        scrollbarThumbRect.setY(Math.min(Math.max(localY, headerHeight + 1), scrollbarFieldHeight + headerHeight + 1));
        scrollLinesAmount = (int) ((
                totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE
        ) * (
                (double)(scrollbarThumbRect.yPos - headerHeight - 1) / (double)(scrollbarFieldHeight))
        );
    }

    private boolean isScrolling() {
        return scrollingThumbMouseOffset >= 0;
    }

    private static class Segment {
        private final String name;
        private List<Emoji> emojis;
        private int numberOfLines;

        public Segment(String name) {
            this.name = name;

            refreshEmojis();
        }

        public boolean isEmpty() {
            return emojis == null || emojis.isEmpty();
        }

        public String getName() {
            return name;
        }

        public List<Emoji> getEmojis() {
            return emojis;
        }

        public int getNumberOfLines() {
            return numberOfLines;
        }

        public void refreshEmojis() {
            emojis = EmojiHandler.getInstance().getEmojisByCategory(name);
            numberOfLines = (int) (Math.ceil((double)emojis.size() / (double) MAX_NUMBER_OF_EMOJIS_IN_LINE) + 1);
        }
    }
}
