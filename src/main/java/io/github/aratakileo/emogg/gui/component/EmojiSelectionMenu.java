package io.github.aratakileo.emogg.gui.component;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.handler.EmoggConfig;
import io.github.aratakileo.emogg.gui.EmojiFont;
import io.github.aratakileo.emogg.gui.screen.SettingsScreen;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.handler.FueController;
import io.github.aratakileo.emogg.handler.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
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

    private final float emojiSize, contentWidth;
    private final EmojiFont font;
    private final RenderUtil.Rect2i settingsButtonRect;
    private final ArrayList<CategoryContent> categoryContents = new ArrayList<>();
    private final boolean isSinglePage;

    private Consumer<Emoji> onEmojiSelected = null;
    private EmojiOrCategoryContent hoveredEmojiOrCategoryContent = null;

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

    protected EmojiSelectionMenu(float emojiSize, int headerHeight, float contentWidth) {
        super(
                0,
                0,
                (int) contentWidth + 1 + SCROLLBAR_WIDTH,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_LINES_ON_PAGE) + 1 + headerHeight
        );

        this.visible = false;
        this.emojiSize = emojiSize;
        this.contentWidth = contentWidth;
        this.font = EmojiFont.getInstance();

        final var emojiHandler = EmojiHandler.getInstance();
        final var categoryNames = new java.util.ArrayList<>(emojiHandler.getCategoryNames().stream().toList());

        Collections.sort(categoryNames);

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
        moveCategoryTo(categoryNames, FueController.CATEGORY_FREQUENTLY_USED, false);

        for (final var categoryName: categoryNames) {
            final var categoryContent = new CategoryContent(categoryName);

            if (categoryContent.isEmpty()) continue;

            categoryContents.add(categoryContent);

            totalLinesAmount += categoryContent.getRenderLineCount();
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
        this(
                emojiSize,
                EmojiFont.getInstance().lineHeight + 3,
                (emojiSize + 1) * MAX_NUMBER_OF_EMOJIS_IN_LINE
        );
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
        renderString(guiGraphics, Emogg.NAMESPACE_OR_ID, 2, 1, 0xffffff, true);

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
        final var categoryTitleOffsetY = (int) ((emojiSize - font.lineHeight) / 2);

        var renderLineIndex = 0;
        var iline = 0;

        hoveredEmojiOrCategoryContent = null;

        for (final var categoryContent: categoryContents) {
            if (
                    iline < verticalScrollbar.getProgress()
                            && iline + categoryContent.getRenderLineCount() - 1 <= verticalScrollbar.getProgress()
            ) {
                iline += categoryContent.getRenderLineCount() + 1;
                continue;
            }

            if (iline >= verticalScrollbar.getProgress()) {
                final var categoryTitleLocalY = (int) Math.ceil(
                        emojiSize + renderLineIndex * (emojiSize + 1) + 1
                ) + 1 + categoryTitleOffsetY;

                var isHovered = false;

                if (
                        !verticalScrollbar.isScrolling()
                                && mouseColumn >= 0
                                && mouseColumn < MAX_NUMBER_OF_EMOJIS_IN_LINE
                                && mouseLine == renderLineIndex
                ) {
                    isHovered = true;
                    hoveredEmojiOrCategoryContent = new EmojiOrCategoryContent(categoryContent);

                    RenderUtil.drawRect(
                            x + 1,
                            y + categoryTitleLocalY - 2,
                            (int) contentWidth,
                            (int) emojiSize,
                            0x77ffffff
                    );
                    setHint((categoryContent.isExpanded() ? "Hide" : "Expand") + " " + categoryContent.getDisplayableName().toLowerCase() + " emojis");
                }

                renderString(
                        guiGraphics,
                        categoryContent.getDisplayableName(),
                        2,
                        categoryTitleLocalY,
                        isHovered ? 0xffffff : 0x6c757d
                );

                if (EmoggConfig.instance.isDebugModeEnabled) {
                    final var debugString = String.valueOf(iline);
                    renderString(
                            guiGraphics,
                            debugString,
                            -font.width(debugString, false) - 2,
                            categoryTitleLocalY,
                            0xffffff
                    );
                }

                renderLineIndex++;
            }

            iline++;

            if (renderLineIndex > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;

            if (!categoryContent.isExpanded()) continue;

            var renderLine = false;
            var icolumn = 0;

            for (final var emoji: categoryContent.getEmojis()) {
                renderLine = iline >= verticalScrollbar.getProgress();

                if (renderLine) {
                    final var emojiX = (int) (x + icolumn * (emojiSize + 1) + 1);
                    final var emojiY = (int) (y + emojiSize + renderLineIndex * (emojiSize + 1) + 1);

                    if (!verticalScrollbar.isScrolling() && mouseColumn == icolumn && mouseLine == renderLineIndex) {
                        hoveredEmojiOrCategoryContent = new EmojiOrCategoryContent(emoji);
                        setHint(emoji.getEscapedCode());
                        RenderUtil.drawRect(emojiX, emojiY, (int) emojiSize, (int) emojiSize, 0x77ffffff);
                    }

                    EmojiUtil.render(emoji, guiGraphics, emojiX + 1, emojiY + 1, (int) (emojiSize - 2));

                    if (EmoggConfig.instance.isDebugModeEnabled && icolumn == 0) {
                        final var debugString = String.valueOf(iline);
                        renderString(
                                guiGraphics,
                                debugString,
                                -font.width(debugString, false) - 2,
                                emojiY - y + 2,
                                0xffffff
                        );
                    }
                }

                icolumn++;

                if (icolumn > MAX_NUMBER_OF_EMOJIS_IN_LINE - 1) {
                    icolumn = 0;
                    iline++;

                    if (renderLine) renderLineIndex++;
                    if (renderLineIndex > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
                }
            }

            if (categoryContent.getEmojis().size() % MAX_NUMBER_OF_EMOJIS_IN_LINE != 0) {
                iline++;

                if (renderLine) renderLineIndex++;
            }
            if (renderLineIndex > MAX_NUMBER_OF_LINES_ON_PAGE - 1) break;
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

        if (hoveredEmojiOrCategoryContent == null) return;

        if (hoveredEmojiOrCategoryContent.isEmoji() && onEmojiSelected != null) {
            playClickSound();
            onEmojiSelected.accept(hoveredEmojiOrCategoryContent.getEmoji());
            return;
        }

        if (!hoveredEmojiOrCategoryContent.isCategoryContent()) return;

        final var categoryContent = hoveredEmojiOrCategoryContent.getCategoryContent();
        categoryContent.setExpanded(!categoryContent.isExpanded());

        playClickSound();
        verticalScrollbar.increaseMaxProgress(
                (categoryContent.isExpanded() ? 1 : -1) * (categoryContent.getLineCount() - 1)
        );
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        verticalScrollbar.onRelease(mouseX, mouseY);
    }

    public void refreshFrequentlyUsedEmojis() {
        final var frequentlyUsedEmojis = FueController.getEmojis();

        verticalScrollbar.setProgress(0);

        if (frequentlyUsedEmojis.isEmpty() || categoryContents.isEmpty()) return;

        CategoryContent frequentlyUsedCategoryContent;

        if (
                (frequentlyUsedCategoryContent = categoryContents.get(0))
                        .getName()
                        .equals(FueController.CATEGORY_FREQUENTLY_USED)
        ) {
            final var oldFrequentlyUsedEmojiCount = frequentlyUsedCategoryContent.getEmojis().size();

            frequentlyUsedCategoryContent.refreshEmojis();

            final var countDifference = frequentlyUsedCategoryContent.getEmojis().size() - oldFrequentlyUsedEmojiCount;

            if (countDifference != 0) verticalScrollbar.increaseMaxProgress(countDifference);
        } else {
            frequentlyUsedCategoryContent = new CategoryContent(FueController.CATEGORY_FREQUENTLY_USED);

            if (!frequentlyUsedCategoryContent.isEmpty()) {
                categoryContents.add(0, frequentlyUsedCategoryContent);
                verticalScrollbar.increaseMaxProgress(frequentlyUsedCategoryContent.getEmojis().size());
            }
        }

        verticalScrollbar.setProgress(0);
    }

    public void setOnEmojiSelected(Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }

    private static class CategoryContent {
        private final String name;

        private List<Emoji> emojis;
        private int lineCount;
        private boolean isExpanded = true;

        public CategoryContent(String name) {
            this.name = name;

            refreshEmojis();
        }

        public boolean isEmpty() {
            return emojis == null || emojis.isEmpty();
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        public String getName() {
            return name;
        }

        public String getDisplayableName() {
            final var categoryLangKey = "emogg.category." + name;
            final var displayableName = Language.getInstance().getOrDefault(categoryLangKey);

            if (displayableName.equals(categoryLangKey)) return StringUtils.capitalize(name)
                    .replaceAll("_", " ");

            return displayableName;
        }

        public List<Emoji> getEmojis() {
            return emojis;
        }

        public int getLineCount() {
            return lineCount;
        }

        public int getRenderLineCount() {
            return isExpanded ? lineCount : 1;
        }

        public void refreshEmojis() {
            emojis = EmojiHandler.getInstance().getEmojisByCategory(name);
            lineCount = (int) (Math.ceil((double)emojis.size() / (double) MAX_NUMBER_OF_EMOJIS_IN_LINE) + 1);
        }
    }

    private static class EmojiOrCategoryContent {
        private final Emoji emoji;
        private final CategoryContent categoryContent;

        private EmojiOrCategoryContent(Emoji emoji, CategoryContent categoryContent) {
            this.emoji = emoji;
            this.categoryContent = categoryContent;
        }

        public EmojiOrCategoryContent(@NotNull Emoji emoji) {
            this(emoji, null);
        }

        public EmojiOrCategoryContent(@NotNull CategoryContent categoryContent) {
            this(null, categoryContent);
        }

        public boolean isEmoji() {
            return emoji != null;
        }

        public Emoji getEmoji() {
            return emoji;
        }

        public boolean isCategoryContent() {
            return categoryContent != null;
        }

        public CategoryContent getCategoryContent() {
            return categoryContent;
        }
    }
}
