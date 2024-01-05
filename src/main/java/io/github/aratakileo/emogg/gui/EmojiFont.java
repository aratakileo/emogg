package io.github.aratakileo.emogg.gui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EmojiFont extends Font {
    public EmojiFont(@NotNull Font font) {
        super(font.fonts, font.filterFishyGlyphs);

        splitter = new EmojiStringSlitter(
                (i, style) -> getFontSet(style.getFont())
                        .getGlyphInfo(i, this.filterFishyGlyphs)
                        .getAdvance(style.isBold())
        );
    }

    @Override
    public float renderText(
            @NotNull String text,
            float x,
            float y,
            int color,
            boolean shadow,
            @NotNull Matrix4f matrix4f,
            @NotNull MultiBufferSource multiBufferSource,
            @NotNull DisplayMode displayMode,
            int underlineColor,
            int light
    ) {
        if (text.isEmpty()) return 0;

        final var emojiTextProcessor = EmojiTextProcessor.from(text);

        final var emojiCharSink = new EmojiCharSink(
                emojiTextProcessor,
                multiBufferSource,
                x,
                y,
                color,
                shadow,
                matrix4f,
                displayMode,
                light
        );

        StringDecomposer.iterateFormatted(emojiTextProcessor.getProcessedText(), Style.EMPTY, emojiCharSink);

        emojiCharSink.finish(underlineColor, x);

        return emojiCharSink.x;
    }

    @Override
    public int drawInBatch(
            @NotNull FormattedCharSequence formattedCharSequence,
            float x,
            float y,
            int color,
            boolean shadow,
            @NotNull Matrix4f matrix4f,
            @NotNull MultiBufferSource multiBufferSource,
            @NotNull DisplayMode displayMode,
            int backgroundColor,
            int light
    ) {
        final var emojiTextProcessor = EmojiTextProcessor.from(asString(formattedCharSequence));

        if (emojiTextProcessor.isEmpty()) return 0;

        color = adjustColor(color);

        final var formattedCharSequences = new ArrayList<FormattedCharSequence>();
        final var renderCharIndex = new AtomicInteger();
        final var ignore = new AtomicBoolean(false);

        formattedCharSequence.accept((index, style, ch) -> {
            if (!ignore.get()) {
                if (emojiTextProcessor.hasEmojiFor(renderCharIndex.get())) {
                    if (emojiTextProcessor.getEmojiLiteralFor(renderCharIndex.get()).isEscaped()) {
                        renderCharIndex.getAndIncrement();
                        return true;
                    } else
                        formattedCharSequences.add(
                                new FormattedEmojiSequence(renderCharIndex.get(), style, ' ')
                        );

                    ignore.set(true);

                    return true;
                }

                formattedCharSequences.add(new FormattedEmojiSequence(renderCharIndex.getAndIncrement(), style, ch));
            }

            if (ignore.get() && ch == ':') {
                ignore.set(false);
                renderCharIndex.getAndIncrement();
            }

            return true;
        });

        final var frontMatrix = new Matrix4f(matrix4f);

        if (shadow) {
            final var emojiCharSink = new EmojiCharSink(
                    emojiTextProcessor,
                    multiBufferSource,
                    x,
                    y,
                    color,
                    true,
                    matrix4f,
                    displayMode,
                    light
            );
            FormattedCharSequence.fromList(formattedCharSequences).accept(emojiCharSink);
            emojiCharSink.finish(backgroundColor, x);
            matrix4f.translate(Font.SHADOW_OFFSET);
        }

        final var emojiCharSink = new EmojiCharSink(
                emojiTextProcessor,
                multiBufferSource,
                x,
                y,
                color,
                false,
                frontMatrix,
                displayMode,
                light
        );
        FormattedCharSequence.fromList(formattedCharSequences).accept(emojiCharSink);
        emojiCharSink.finish(backgroundColor, x);

        return (int) emojiCharSink.x;
    }

    @Override
    public void drawInBatch8xOutline(
            @NotNull FormattedCharSequence formattedCharSequence,
            float x,
            float y,
            int color,
            int strokeColor,
            @NotNull Matrix4f matrix4f,
            @NotNull MultiBufferSource multiBufferSource,
            int light
    ) {
        final var emojiTextProcessor = EmojiTextProcessor.from(asString(formattedCharSequence));

        if (emojiTextProcessor.isEmpty()) return;

        final var finalStrokeColor = adjustColor(strokeColor);
        final var emojiCharSinkFromBehind = new EmojiCharSink(
                emojiTextProcessor,
                multiBufferSource,
                0.0F,
                0.0F,
                strokeColor,
                false,
                matrix4f,
                Font.DisplayMode.NORMAL,
                light
        );

        for(int localX = -1; localX <= 1; ++localX) {
            for(int localY = -1; localY <= 1; ++localY) {
                if (localX != 0 || localY != 0) {
                    AtomicReference<Float> offsettedX = new AtomicReference<>(x);
                    final var finalLocalX = localX;
                    final var finalLocalY = localY;
                    final var renderCharIndex = new AtomicInteger();
                    final var ignore = new AtomicBoolean(false);

                    formattedCharSequence.accept((index, style, ch) -> {
                        if (!ignore.get()) {
                            if (emojiTextProcessor.hasEmojiFor(renderCharIndex.get())) {
                                if (emojiTextProcessor.getEmojiLiteralFor(renderCharIndex.get()).isEscaped()) {
                                    renderCharIndex.getAndIncrement();
                                    return true;
                                } else
                                    offsettedX.updateAndGet(value -> value + EmojiLiteral.DEFAULT_RENDER_SIZE);

                                ignore.set(true);

                                return true;
                            }

                            final var glyphInfo = getFontSet(style.getFont()).getGlyphInfo(ch, this.filterFishyGlyphs);

                            emojiCharSinkFromBehind.x = offsettedX.get()
                                    + (float) finalLocalX * glyphInfo.getShadowOffset();
                            emojiCharSinkFromBehind.y = y + (float) finalLocalY * glyphInfo.getShadowOffset();

                            offsettedX.updateAndGet(value -> value + glyphInfo.getAdvance(style.isBold()));
                            renderCharIndex.incrementAndGet();

                            return emojiCharSinkFromBehind.accept(index, style.withColor(finalStrokeColor), ch);
                        }

                        if (ignore.get() && ch == ':') {
                            ignore.set(false);
                            renderCharIndex.getAndIncrement();
                        }

                        return true;
                    });
                }
            }
        }

        drawInBatch(
                formattedCharSequence,
                x,
                y,
                color,
                false,
                matrix4f,
                multiBufferSource,
                DisplayMode.POLYGON_OFFSET,
                0,
                light
        );
    }

    @Override
    public int width(@NotNull String string) {
        return width(string, true);
    }

    public int width(char ch) {
        return width(String.valueOf(ch), false);
    }

    public int width(@NotNull String text, boolean emojiSupport) {
        if (emojiSupport) return super.width(EmojiTextProcessor.processText(text));

        return super.width(text);
    }

    @Override
    public int width(@NotNull FormattedCharSequence formattedCharSequence) {
        return width(asString(formattedCharSequence), true);
    }

    @Override
    public int width(@NotNull FormattedText formattedText) {
        return this.width(formattedText.getString(), true);
    }

    public static @NotNull EmojiFont getInstance() {
        return (EmojiFont) Minecraft.getInstance().font;
    }

    class EmojiCharSink implements FormattedCharSink {
        private final EmojiTextProcessor emojiTextProcessor;
        private final MultiBufferSource multiBufferSource;
        private float x;
        private float y;
        private final int color;
        private final boolean shadow;
        private final Matrix4f matrix;
        private final DisplayMode displayMode;
        private final int light;

        private final List<BakedGlyph.Effect> effects = new ArrayList<>();

        public EmojiCharSink(
                @NotNull EmojiTextProcessor emojiTextProcessor,
                @NotNull MultiBufferSource multiBufferSource,
                float x,
                float y,
                int color,
                boolean shadow,
                @NotNull Matrix4f matrix,
                @NotNull DisplayMode displayMode,
                int light
        ) {
            this.emojiTextProcessor = emojiTextProcessor;
            this.multiBufferSource = multiBufferSource;
            this.x = x;
            this.y = y;
            this.color = color;
            this.shadow = shadow;
            this.matrix = matrix;
            this.displayMode = displayMode;
            this.light = light;
        }

        public void finish(int color, float x_off) {
            if (color != 0) {
                float r = (float) (color >> 24 & 255) / 255.0F;
                float g = (float) (color >> 16 & 255) / 255.0F;
                float b = (float) (color >> 8 & 255) / 255.0F;
                float a = (float) (color & 255) / 255.0F;

                effects.add(new BakedGlyph.Effect(
                        x_off - 1f,
                        y + 9f,
                        x + 1f,
                        y - 1f,
                        0.01f,
                        r, g, b, a
                ));
            }

            if (!effects.isEmpty()) {
                FontSet fontSet = EmojiFont.this.getFontSet(Style.DEFAULT_FONT);
                BakedGlyph bakedGlyph = fontSet.whiteGlyph();

                VertexConsumer buffer = multiBufferSource.getBuffer(
                        bakedGlyph.renderType(displayMode)
                );

                for (BakedGlyph.Effect rectangle: effects)
                    bakedGlyph.renderEffect(rectangle, matrix, buffer, light);
            }
        }

        @Override
        public boolean accept(int index, @NotNull Style style, int codePoint) {
            final EmojiLiteral emojiLiteral;

            if (emojiTextProcessor.hasEmojiFor(index) && !(
                    emojiLiteral = emojiTextProcessor.getEmojiLiteralFor(index)
            ).isEscaped()) {
                x += emojiLiteral.render(x, y, matrix, multiBufferSource, light);

                return true;
            }

            final var fontSet = EmojiFont.this.getFontSet(style.getFont());
            final var glyph = fontSet.getGlyphInfo(codePoint, EmojiFont.this.filterFishyGlyphs);
            final var bakedGlyph = style.isObfuscated()
                    && codePoint != 32 ? fontSet.getRandomGlyph(glyph) : fontSet.getGlyph(codePoint);
            final var textColor = style.getColor();

            float r, g, b, a = (float) (color >> 24 & 255) / 255.0F, dimFactor = shadow ? 0.25f : 1.0f;

            if (textColor != null) {
                int colorValue = textColor.getValue();

                r = (float) (colorValue >> 16 & 255) / 255.0F * dimFactor;
                g = (float) (colorValue >> 8 & 255) / 255.0F * dimFactor;
                b = (float) (colorValue & 255) / 255.0F * dimFactor;
            } else {
                r = (float) (color >> 16 & 255) / 255.0F * dimFactor;
                g = (float) (color >> 8 & 255) / 255.0F * dimFactor;
                b = (float) (color & 255) / 255.0F * dimFactor;
            }

            if (!(bakedGlyph instanceof EmptyGlyph)) {
                final var shadowOffset = shadow ? glyph.getShadowOffset() : 0f;

                VertexConsumer buffer = multiBufferSource.getBuffer(bakedGlyph.renderType(displayMode));
                EmojiFont.this.renderChar(
                        bakedGlyph,
                        style.isBold(),
                        style.isItalic(),
                        style.isBold() ? glyph.getBoldOffset() : 0f,
                        x + shadowOffset,
                        y + shadowOffset,
                        matrix,
                        buffer,
                        r, g, b, a,
                        light
                );
            }

            final var advanceOffset = glyph.getAdvance(style.isBold());
            final var shadowOffset = shadow ? 1f : 0f;

            if (style.isStrikethrough())
                effects.add(new BakedGlyph.Effect(
                        x + shadowOffset - 1f,
                        y + shadowOffset - 4.5f,
                        x + shadowOffset + advanceOffset,
                        y + shadowOffset + 4.5f - 1f,
                        0.01f,
                        r, g, b, a
                ));

            if (style.isUnderlined())
                effects.add(new BakedGlyph.Effect(
                        x + shadowOffset - 1f,
                        y + shadowOffset - 9f,
                        x + shadowOffset + advanceOffset,
                        y + shadowOffset + 9f - 1f,
                        0.01f,
                        r, g, b, a
                ));

            x += advanceOffset;

            return true;
        }
    }

    public static @NotNull String asString(FormattedCharSequence formattedCharSequence) {
        StringBuilder stringBuilder = new StringBuilder();
        formattedCharSequence.accept((a, b, c) -> {
            stringBuilder.append((char)c);
            return true;
        });
        return stringBuilder.toString();
    }

    record FormattedEmojiSequence(int index, @NotNull Style style, int codePoint) implements FormattedCharSequence {
        @Override
        public boolean accept(@NotNull FormattedCharSink formattedCharSink) {
            return formattedCharSink.accept(index, style, codePoint);
        }
    }
}
