package io.github.aratakileo.emogg.gui.component.esm;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.gui.component.AbstractWidget;
import io.github.aratakileo.emogg.gui.component.VerticalScrollbar;
import io.github.aratakileo.emogg.handler.EmoggConfig;
import io.github.aratakileo.emogg.gui.EmojiFont;
import io.github.aratakileo.emogg.gui.screen.SettingsScreen;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.handler.FueController;
import io.github.aratakileo.emogg.handler.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.Rect2i;
import io.github.aratakileo.emogg.util.GuiUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EmojiSelectionMenu extends AbstractWidget {
    public final static int MAX_NUMBER_OF_EMOJIS_IN_LINE = 9,
            MAX_NUMBER_OF_LINES_ON_PAGE = 8,
            SCROLLBAR_WIDTH = 5;

    private final static ResourceLocation SETTINGS_ICON = new ResourceLocation(
            Emogg.NAMESPACE_OR_ID,
            "gui/icon/settings_icon.png"
    ), PLUS_ICON = new ResourceLocation(
            Emogg.NAMESPACE_OR_ID,
            "gui/icon/plus_icon.png"
    );

    private final float emojiSize, contentWidth;
    private final EmojiFont font;
    private final Rect2i settingsButtonRect, plusButtonRect;
    private final ArrayList<CategoryContent> categoryContents = new ArrayList<>();
    private final boolean isSinglePage;

    private @Nullable Consumer<Emoji> onEmojiSelected = null;
    private @Nullable EmojiOrCategoryContent hoveredEmojiOrCategoryContent = null;

    public final VerticalScrollbar verticalScrollbar;

    private void moveCategoryDown(@NotNull List<String> categoryNames, @NotNull String category) {
        moveCategoryTo(categoryNames, category, true);
    }

    private void moveCategoryTo(@NotNull List<String> categoryNames, @NotNull String category, boolean isMoveDown) {
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

        this.isSinglePage = totalLinesAmount < MAX_NUMBER_OF_LINES_ON_PAGE;
        this.verticalScrollbar = new VerticalScrollbar(
                x + width - SCROLLBAR_WIDTH,
                y + headerHeight,
                SCROLLBAR_WIDTH,
                height - headerHeight,
                totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE,
                2,
                1
        );

        if (isSinglePage)
            width -= SCROLLBAR_WIDTH;

        final var buttonSize = font.lineHeight + 2;

        this.settingsButtonRect = new Rect2i(
                width - font.lineHeight - 3,
                1,
                buttonSize
        );
        this.plusButtonRect = settingsButtonRect.copy().moveX(-buttonSize - 1);

        setTooltipPositioner(MOUSE_TOOLTIP_POSITIONER);
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
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        disableTooltip();

        /*
        * Start of header processing & rendering
        */
        GuiUtil.drawRect(guiGraphics, x, y, width, (int) emojiSize, 0xaa000000);

        renderString(
                guiGraphics,
                StringUtils.capitalize(Emogg.NAMESPACE_OR_ID),
                2,
                2,
                0xffffff,
                true
        );

        final var settingsButtonRenderRect = settingsButtonRect.move(x, y);
        final var plusButtonRenderRect = plusButtonRect.move(x, y);

        if (!verticalScrollbar.isScrolling()) {
            if (settingsButtonRenderRect.contains(mouseX, mouseY)) {
                GuiUtil.drawRect(guiGraphics, settingsButtonRenderRect, 0x77ffffff);
                setTooltip(Component.translatable("emogg.settings.title"));
            }

            if (plusButtonRenderRect.contains(mouseX, mouseY)) {
                GuiUtil.drawRect(guiGraphics, plusButtonRenderRect, 0x77ffffff);
                setTooltip(Component.translatable("emogg.tooltip.action.add_emojis"));
            }
        }

        GuiUtil.renderTexture(
                guiGraphics,
                SETTINGS_ICON,
                settingsButtonRenderRect.moveBounds(1, 1, -1, -1)
        );

        GuiUtil.renderTexture(
                guiGraphics,
                PLUS_ICON,
                plusButtonRenderRect.moveBounds(1, 1, -1, -1)
        );

        /*
        * Start of content processing & rendering
        */
        GuiUtil.drawRect(
                guiGraphics,
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

        hoveredEmojiOrCategoryContent = null;

        var renderLineIndex = 0;
        var iline = 0;

        for (final var categoryContent: categoryContents) {
            if (
                    iline < verticalScrollbar.getProgress()
                            && iline + categoryContent.getRenderLineCount() < verticalScrollbar.getProgress()
            ) {
                iline += categoryContent.getRenderLineCount();
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

                    GuiUtil.drawRect(
                            guiGraphics,
                            x + 1,
                            y + categoryTitleLocalY - 2,
                            (int) contentWidth,
                            (int) emojiSize,
                            0x77ffffff
                    );

                    setTooltip(categoryContent.getDisplayableName());
                }

                final var expandIndicatorChar = categoryContent.isExpanded() ? '-' : '+';
                final var expandIndicatorLocalX = (int) (contentWidth - font.width(expandIndicatorChar));

                renderString(
                        guiGraphics,
                        categoryContent.getDisplayableName(expandIndicatorLocalX - 2),
                        2,
                        categoryTitleLocalY,
                        isHovered ? 0xe7e7e7 : 0x6c757d
                );

                renderString(
                        guiGraphics,
                        expandIndicatorChar,
                        expandIndicatorLocalX,
                        categoryTitleLocalY,
                        0xffffff
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
                        setTooltip(emoji.getEscapedCode());
                        GuiUtil.drawRect(
                                guiGraphics,
                                emojiX,
                                emojiY,
                                (int) emojiSize,
                                (int) emojiSize,
                                0x77ffffff
                        );
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
        if (!isSinglePage) verticalScrollbar.render(guiGraphics, mouseX, mouseY, dt);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isActive() || !isHovered || isSinglePage)
            return false;

        verticalScrollbar.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return verticalScrollbar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (settingsButtonRect.move(x, y).contains(mouseX, mouseY)) {
            playClickSound();
            Minecraft.getInstance().setScreen(new SettingsScreen());
        }

        if (plusButtonRect.move(x, y).contains(mouseX, mouseY)) {
            playClickSound();
            Util.getPlatform().openUri("https://aratakileo.github.io/emogg-resourcepack-maker/");
        }

        super.onClick(mouseX, mouseY);

        if (isSinglePage) return;

        verticalScrollbar.onClick(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        super.onPress();

        if (Objects.isNull(hoveredEmojiOrCategoryContent)) return;

        if (hoveredEmojiOrCategoryContent.isEmoji() && Objects.nonNull(onEmojiSelected)) {
            playClickSound();
            onEmojiSelected.accept(hoveredEmojiOrCategoryContent.getEmoji());
            return;
        }

        if (!hoveredEmojiOrCategoryContent.isCategoryContent()) return;

        final var categoryContent = hoveredEmojiOrCategoryContent.getCategoryContent();

        categoryContent.toggleExpand();
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

        CategoryContent fueCategoryContent;

        if (
                (fueCategoryContent = categoryContents.get(0))
                        .getName()
                        .equals(FueController.CATEGORY_FREQUENTLY_USED)
        ) {
            final var oldFueCategoryLineCount = fueCategoryContent.getRenderLineCount();

            fueCategoryContent.refreshEmojis();

            verticalScrollbar.increaseMaxProgress(
                    fueCategoryContent.getRenderLineCount() - oldFueCategoryLineCount
            );
        } else {
            fueCategoryContent = new CategoryContent(FueController.CATEGORY_FREQUENTLY_USED);

            if (!fueCategoryContent.isEmpty()) {
                categoryContents.add(0, fueCategoryContent);
                verticalScrollbar.increaseMaxProgress(fueCategoryContent.getRenderLineCount());
            }
        }
    }

    public void setOnEmojiSelected(@Nullable Consumer<Emoji> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
    }
}
