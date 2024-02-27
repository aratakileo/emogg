package io.github.aratakileo.emogg.gui.widget;

import io.github.aratakileo.elegantia.graphics.GuiGraphicsUtil;
import io.github.aratakileo.elegantia.graphics.RectDrawer;
import io.github.aratakileo.elegantia.gui.WidgetBoundsBuilder;
import io.github.aratakileo.elegantia.gui.tooltip.HoveredTooltipPositioner;
import io.github.aratakileo.elegantia.gui.widget.CompositeWidget;
import io.github.aratakileo.elegantia.gui.widget.VerticalScrollbar;
import io.github.aratakileo.elegantia.math.Rect2i;
import io.github.aratakileo.elegantia.util.AnyOfPair;
import io.github.aratakileo.elegantia.util.Mouse;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.emoji.*;
import io.github.aratakileo.emogg.gui.CategoryContent;
import io.github.aratakileo.emogg.util.EmojiUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class EmojiSelectionMenu extends CompositeWidget {
    private final static String RESOURCE_PACK_MAKER_URL = "https://aratakileo.github.io/emogg-resourcepack-maker/";

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

    private final Rect2i settingsButtonRect, plusButtonRect;
    private final ArrayList<CategoryContent> categoryContents = new ArrayList<>();
    private final boolean isSinglePage;

    private @Nullable Consumer<Emoji> onEmojiSelected = null;
    private @Nullable AnyOfPair<Emoji, CategoryContent> hoveredEmojiOrCategoryContent = null;

    @CompositePart(lateInitAnchor = "verticalScrollbar")
    public final VerticalScrollbar verticalScrollbar;

    protected EmojiSelectionMenu(float emojiSize, int headerHeight, float contentWidth) {
        super(new Rect2i(
                0,
                0,
                (int) contentWidth + 1 + SCROLLBAR_WIDTH,
                (int) ((emojiSize + 1) * MAX_NUMBER_OF_LINES_ON_PAGE) + 1 + headerHeight
        ));

        this.isVisible = false;
        this.emojiSize = emojiSize;
        this.contentWidth = contentWidth;

        var totalLinesAmount = 0;

        for (final var categoryKey: EmojiCategory.getCategoryKeys(true)) {
            final var categoryContent = new CategoryContent(categoryKey);

            if (categoryContent.isEmpty()) continue;

            categoryContents.add(categoryContent);

            totalLinesAmount += categoryContent.getRenderLineCount();
        }

        this.isSinglePage = totalLinesAmount < MAX_NUMBER_OF_LINES_ON_PAGE;
        this.verticalScrollbar = new VerticalScrollbar(
                new WidgetBoundsBuilder(SCROLLBAR_WIDTH, getHeight() - headerHeight - 1)
                        .setGravity(WidgetBoundsBuilder.GRAVITY_RIGHT | WidgetBoundsBuilder.GRAVITY_BOTTOM)
                        .setParentBounds(getBounds())
                        .build(),
                totalLinesAmount - MAX_NUMBER_OF_LINES_ON_PAGE,
                2,
                1
        );

        declareAsInited("verticalScrollbar");

        if (isSinglePage)
            setWidth(getWidth() - SCROLLBAR_WIDTH);

        final var buttonSize = EmojiGlyph.HEIGHT + 2;

        this.settingsButtonRect = new Rect2i(
                (int) (getWidth() - EmojiGlyph.HEIGHT - 3),
                1,
                (int) buttonSize
        );
        this.plusButtonRect = settingsButtonRect.copy().moveX((int) (-buttonSize - 1));

        setTooltipPositionerGetter(HoveredTooltipPositioner::new);
    }

    public EmojiSelectionMenu(float emojiSize) {
        this(
                emojiSize,
                (int) (EmojiGlyph.HEIGHT + 3),
                (emojiSize + 1) * MAX_NUMBER_OF_EMOJIS_IN_LINE
        );
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        disableTooltip();

        /*
        * Start of header processing & rendering
        */
        RectDrawer.with(guiGraphics, getBounds().setIpHeight((int) emojiSize)).draw(0xaa000000);
        GuiGraphicsUtil.drawText(
                guiGraphics,
                StringUtils.capitalize(Emogg.NAMESPACE_OR_ID),
                getX() + 2,
                getY() + 2,
                0xffffff
        );

        final var settingsButtonRenderRect = settingsButtonRect.move(getX(), getY());
        final var plusButtonRenderRect = plusButtonRect.move(getX(), getY());

        if (!verticalScrollbar.isScrolling()) {
            if (settingsButtonRenderRect.contains(mouseX, mouseY)) {
                RectDrawer.with(guiGraphics, settingsButtonRenderRect).draw(0x77ffffff);
                setTooltip(Component.translatable("elegantia.gui.config.title", Emogg.NAMESPACE_OR_ID));
            }

            if (plusButtonRenderRect.contains(mouseX, mouseY)) {
                RectDrawer.with(guiGraphics, plusButtonRenderRect).draw(0x77ffffff);
                setTooltip(Component.translatable("emogg.tooltip.action.add_emojis"));
            }
        }

        RectDrawer.with(
                guiGraphics,
                settingsButtonRenderRect.moveBounds(1, 1, -1, -1)
        ).renderTexture(SETTINGS_ICON);

        RectDrawer.with(
                guiGraphics,
                plusButtonRenderRect.moveBounds(1, 1, -1, -1)
        ).renderTexture(PLUS_ICON);

        /*
        * Start of content processing & rendering
        */
        RectDrawer.with(guiGraphics, getBounds().cutIpTop((int) emojiSize))
                .draw(0xaa222222)
                .drawStroke(0xaa000000, 1);

        final var mouseColumn = (int) ((mouseX - getX()) / (emojiSize + 1));
        final var mouseLine = (int) ((mouseY - getY()) / (emojiSize + 1)) - 1;
        final var categoryTitleOffsetY = (int) ((emojiSize - EmojiGlyph.HEIGHT) / 2);
        final var font = Minecraft.getInstance().font;

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
                    hoveredEmojiOrCategoryContent = AnyOfPair.ofRight(categoryContent);

                    RectDrawer.with(
                            guiGraphics,
                            getBounds()
                                    .moveIp(1, categoryTitleLocalY - 2)
                                    .setIpSize((int) contentWidth, (int) emojiSize)
                    ).draw(0x77ffffff);

                    setTooltip(categoryContent.getDisplayableName());
                }

                final var expandIndicatorText = categoryContent.isExpanded() ? "-" : "+";
                final var expandIndicatorLocalX = (int) (contentWidth - font.width(expandIndicatorText));

                GuiGraphicsUtil.drawText(
                        guiGraphics,
                        categoryContent.getDisplayableName(expandIndicatorLocalX - 2),
                        getX() + 2,
                        getY() + categoryTitleLocalY,
                        isHovered ? 0xe7e7e7 : 0x6c757d
                );

                GuiGraphicsUtil.drawText(
                        guiGraphics,
                        expandIndicatorText,
                        getX() + expandIndicatorLocalX,
                        getY() + categoryTitleLocalY,
                        0xffffff
                );

                if (EmoggConfig.instance.enableDebugMode) {
                    final var debugString = String.valueOf(iline);
                    GuiGraphicsUtil.drawText(
                            guiGraphics,
                            debugString,
                            getX() - font.width(debugString) - 2,
                            getY() + categoryTitleLocalY,
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

            assert Objects.nonNull(categoryContent.getEmojis());

            for (final var emoji: categoryContent.getEmojis()) {
                renderLine = iline >= verticalScrollbar.getProgress();

                if (renderLine) {
                    final var emojiPos = getPosition().add(
                            (int)(icolumn * (emojiSize + 1) + 1),
                            (int)(emojiSize + renderLineIndex * (emojiSize + 1) + 1)
                    );

                    if (!verticalScrollbar.isScrolling() && mouseColumn == icolumn && mouseLine == renderLineIndex) {
                        hoveredEmojiOrCategoryContent = AnyOfPair.ofLeft(emoji);

                        setTooltip(emoji.getEscapedCode());

                        RectDrawer.with(
                                guiGraphics,
                                emojiPos.x,
                                emojiPos.y,
                                (int) emojiSize,
                                (int) emojiSize
                        ).draw(0x77ffffff);
                    }

                    EmojiUtil.render(
                            emoji.getGlyph(),
                            guiGraphics,
                            emojiPos.x + 1,
                            emojiPos.y + 1,
                            (int) (emojiSize - 2),
                            false
                    );

                    if (EmoggConfig.instance.enableDebugMode && icolumn == 0) {
                        final var debugString = String.valueOf(iline);

                        GuiGraphicsUtil.drawText(
                                guiGraphics,
                                debugString,
                                getX() - font.width(debugString) - 2,
                                emojiPos.y + 2,
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
        verticalScrollbar.isVisible = !isSinglePage;
        super.renderWidget(guiGraphics, mouseX, mouseY, dt);
    }

    @Override
    public boolean onClick(boolean byUser) {
        final var localisedMousePos = Mouse.getPosition().sub(getX(), getY());

        if (settingsButtonRect.contains(localisedMousePos)) {
            GuiGraphicsUtil.playClickSound();
            Minecraft.getInstance().setScreen(EmoggConfig.instance.getScreen());
            return true;
        }

        if (plusButtonRect.contains(localisedMousePos)) {
            GuiGraphicsUtil.playClickSound();
            Util.getPlatform().openUri(RESOURCE_PACK_MAKER_URL);
            return true;
        }

        if (Objects.isNull(hoveredEmojiOrCategoryContent)) return false;

        final var isClicked = new AtomicBoolean(false);

        hoveredEmojiOrCategoryContent.ifIs(Emoji.class, emoji -> {
            if (Objects.isNull(onEmojiSelected)) return;
            if (byUser) GuiGraphicsUtil.playClickSound();

            onEmojiSelected.accept(emoji);
            isClicked.set(true);
        });

        hoveredEmojiOrCategoryContent.ifIs(CategoryContent.class, categoryContent -> {
            if (byUser) GuiGraphicsUtil.playClickSound();

            categoryContent.toggleExpand();
            verticalScrollbar.increaseMaxProgress(
                    (categoryContent.isExpanded() ? 1 : -1) * (categoryContent.getLineCount() - 1)
            );
            isClicked.set(true);
        });

        return isClicked.get();
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
