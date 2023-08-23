package io.github.aratakileo.emogg.gui.component;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.gui.screen.SettingsScreen;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.handler.FrequentlyUsedEmojiController;
import io.github.aratakileo.emogg.resource.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class EmojiSelectionMenu extends AbstractWidget {
    public final static int MAX_NUMBER_OF_EMOJIS_IN_LINE = 9,
            MAX_NUMBER_OF_LINES_ON_PAGE = 8,
            SCROLLBAR_WIDTH = 5;

    private final static ResourceLocation SETTINGS_ICON = new ResourceLocation(
            Emogg.NAMESPACE_OR_ID,
            "gui/icon/settings_icon.png"
    );

    private final float emojiSize;
    private final Font font;
    private final RenderUtil.Rect2i settingsButtonRect;
    private final LinkedHashMap<Integer, CategoryContent> categoryContents = new LinkedHashMap<>();
    private final boolean isSinglePage;

    private Consumer<Emoji> onEmojiSelected = null;
    private Emoji hoveredEmoji = null;

    public final VerticalScrollbar verticalScrollbar;

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

        final var emojiHandler = EmojiHandler.getInstance();
        final var categoryNames = new java.util.ArrayList<>(emojiHandler.getCategories().stream().toList());
        var totalLinesAmount = 0;

        // Reordering categories
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_ANIME);
        moveCategoryDown(categoryNames, EmojiHandler.CATEGORY_MEMES);
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
            final var categoryContent = new CategoryContent(categoryName);

            if (categoryContent.isEmpty()) continue;

            categoryContents.put(totalLinesAmount, categoryContent);

            totalLinesAmount += categoryContent.getNumberOfLines();
        }

        totalLinesAmount--;

        this.isSinglePage = totalLinesAmount < MAX_NUMBER_OF_LINES_ON_PAGE;
        this.verticalScrollbar = new VerticalScrollbar(
                x + width - SCROLLBAR_WIDTH,
                y + headerHeight, SCROLLBAR_WIDTH,
                height - headerHeight,
                totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE,
                2,
                1
        );

        if (isSinglePage)
            width -= SCROLLBAR_WIDTH;

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
    public void setX(int x) {
        final var oldX = this.x;
        this.x = x;
        this.verticalScrollbar.setX(this.verticalScrollbar.x + (x - oldX));
    }

    @Override
    public void setY(int y) {
        final var oldY = this.y;
        this.y = y;
        this.verticalScrollbar.setY(this.verticalScrollbar.y + (y - oldY));
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

        if (!verticalScrollbar.isScrolling() && renderableSettingsButtonRect.contains(mouseX, mouseY)) {
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

        for (var categoryContentStartIndex: categoryContents.keySet()) {
            final var currentCategoryContent = categoryContents.get(categoryContentStartIndex);
            final var indexDifference = verticalScrollbar.getScrollProgress() - categoryContentStartIndex;
            final var lineLocalY = (int) Math.ceil(emojiSize + iline * (emojiSize + 1) + 1) + 1;

            if (indexDifference > 0) {
                if (indexDifference > currentCategoryContent.getNumberOfLines()) continue;

                iline = -indexDifference;
            } else {
                if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;

                if (iline >= 0)
                    renderString(
                            guiGraphics,
                            EmojiHandler.getDisplayableCategoryName(currentCategoryContent.getName()),
                            2,
                            lineLocalY + categoryNameOffsetY,
                            0x6c757d
                    );

                iline++;

                if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
            }

            var icolumn = 0;

            for (var emoji: currentCategoryContent.getEmojis()) {
                if (iline >= 0) {
                    final var emojiX = (int) (x + icolumn * (emojiSize + 1) + 1);
                    final var emojiY = (int) (y + emojiSize + iline * (emojiSize + 1) + 1);

                    if (!verticalScrollbar.isScrolling() && mouseColumn == icolumn && mouseLine == iline) {
                        hoveredEmoji = emoji;
                        setHint(emoji.getEscapedCode());
                        RenderUtil.drawRect(emojiX, emojiY, (int) emojiSize, (int) emojiSize, 0x77ffffff);
                    }

                    EmojiUtil.render(emoji, guiGraphics, emojiX + 1, emojiY + 1, (int) (emojiSize - 2));
                }

                icolumn++;

                if (icolumn > MAX_NUMBER_OF_EMOJIS_IN_LINE - 1) {
                    icolumn = 0;
                    iline++;
                    if (iline > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
                }
            }

            if (currentCategoryContent.getEmojis().size() % MAX_NUMBER_OF_EMOJIS_IN_LINE != 0) iline++;
        }

        /*
        * Start of scrollbar processing & rendering
        */
        if (isSinglePage) return;

        verticalScrollbar.render(guiGraphics, mouseX, mouseY, dt);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!isActive() || !isHovered || isSinglePage)
            return false;

        verticalScrollbar.mouseScrolled(mouseX, mouseY, scrollDelta);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return verticalScrollbar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (settingsButtonRect.move(x, y).contains((int) mouseX, (int) mouseY)) {
            playClickSound();
            Minecraft.getInstance().setScreen(new SettingsScreen());
        }

        super.onClick(mouseX, mouseY);

        if (isSinglePage) return;

        verticalScrollbar.onClick(mouseX, mouseY);
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
        verticalScrollbar.onRelease(mouseX, mouseY);
    }

    public void refreshFrequentlyUsedEmojis() {
        final var frequentlyUsedEmojis = FrequentlyUsedEmojiController.getEmojis();

        verticalScrollbar.setScrollProgress(0);

        if (frequentlyUsedEmojis.isEmpty() || categoryContents.isEmpty()) return;

        if (categoryContents.get(0).getName().equals(FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED)) {
            categoryContents.get(0).refreshEmojis();
        } else {
            final var frequentlyUsedCategoryContent = new CategoryContent(
                    FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED
            );

            if (!frequentlyUsedCategoryContent.isEmpty()) {
                final var newCategoryContents = new LinkedHashMap<Integer, CategoryContent>();
                final var indexOffset = frequentlyUsedCategoryContent.getNumberOfLines();

                newCategoryContents.put(0, frequentlyUsedCategoryContent);

                for (var index : categoryContents.keySet())
                    newCategoryContents.put(index + indexOffset, categoryContents.get(index));

                categoryContents.clear();
                categoryContents.putAll(newCategoryContents);
            }
        }

        final var lastCategoryContentEntry = categoryContents.entrySet()
                .stream()
                .toList()
                .get(categoryContents.size() - 1);

        verticalScrollbar.setNumberOfScrollingPositions(
                (lastCategoryContentEntry.getKey() + lastCategoryContentEntry.getValue().numberOfLines - 1)
                        - MAX_NUMBER_OF_LINES_ON_PAGE
        );
        verticalScrollbar.setScrollProgress(0);
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }

    private static class CategoryContent {
        private final String name;
        private List<Emoji> emojis;
        private int numberOfLines;

        public CategoryContent(String name) {
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