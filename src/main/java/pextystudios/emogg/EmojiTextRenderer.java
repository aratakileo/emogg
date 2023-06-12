package pextystudios.emogg;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiTextRenderer extends TextRenderer {
    public static LoadingCache<String, Pair<String, HashMap<Integer, Emoji>>> TEXT_DATA_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Pair<String, HashMap<Integer, Emoji>> load(@NotNull String key) {
                    return getTextDataForRender(key);
                }
            });

    public EmojiTextRenderer() {
        super(MinecraftClient.getInstance().textRenderer.fontStorageAccessor);
    }

    public EmojiTextRenderer(TextRenderer textRenderer) {
        super(textRenderer.fontStorageAccessor);
    }

    @Override
    public float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, boolean seeThrough, int underlineColor, int light) {
        if (text.isEmpty()) return 0;

        try {
            Pair<String, HashMap<Integer, Emoji>> cache = TEXT_DATA_CACHE.get(text);

            EmojiLiteralVisitor emojiLiteralVisitor = new EmojiLiteralVisitor(cache.getB(), vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);

            TextVisitFactory.visitFormatted(cache.getA(), Style.EMPTY, emojiLiteralVisitor);

            emojiLiteralVisitor.finish(underlineColor, x);

            return emojiLiteralVisitor.x;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getWidth(String text) {
        try {
            text = TEXT_DATA_CACHE.get(text).getA();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return super.getWidth(text);
    }

    @Override
    public int getWidth(OrderedText text) {
        StringBuilder stringBuilder = new StringBuilder();
        text.accept((a, b, c) -> {
            stringBuilder.append((char)c);
            return true;
        });
        return getWidth(stringBuilder.toString());
    }

    @Override
    public int getWidth(StringVisitable text) {
        return this.getWidth(text.getString());
    }

    public static Pair<String, HashMap<Integer, Emoji>> getTextDataForRender(String text) {
        HashMap<Integer, Emoji> emojiIndexes = new LinkedHashMap<>();

        Matcher matcher = Pattern.compile("(:([_A-Za-z0-9]+):)").matcher(text);
        int sourceEmojiIndexesOffset = 0;

        while (matcher.find()) {
            if (!Emogg.getInstance().emojis.containsKey(matcher.group(2)))
                continue;

            int lengthBeforeChanges = text.length();

            text = text.replaceFirst(matcher.group(1), "   ");

            emojiIndexes.put(
                    matcher.start(1) - sourceEmojiIndexesOffset,
                    Emogg.getInstance().emojis.get(matcher.group(2))
            );

            sourceEmojiIndexesOffset += lengthBeforeChanges - text.length();
        }

        return new Pair<>(text, emojiIndexes);
    }

    class EmojiLiteralVisitor implements CharacterVisitor {
        private final HashMap<Integer, Emoji> emojiIndexes;
        private final VertexConsumerProvider vertexConsumers;
        private float x;
        private final float y;
        private final int color;
        private final boolean shadow;
        private final Matrix4f matrix;
        private final boolean seeThrough;
        private final int light;
        
        private List<GlyphRenderer.Rectangle> rectangles = List.of();

        public EmojiLiteralVisitor(
                HashMap<Integer, Emoji> emojiIndexes,
                VertexConsumerProvider vertexConsumers,
                float x,
                float y,
                int color,
                boolean shadow,
                Matrix4f matrix,
                boolean seeThrough,
                int light
        ) {
            this.emojiIndexes = emojiIndexes;
            this.vertexConsumers = vertexConsumers;
            this.x = x;
            this.y = y;
            this.color = color;
            this.shadow = shadow;
            this.matrix = matrix;
            this.seeThrough = seeThrough;
            this.light = light;
        }
        
        public void finish(int color, float x_off) {
            if (color != 0) {
                float r = (float) (color >> 24 & 255) / 255.0F;
                float g = (float) (color >> 16 & 255) / 255.0F;
                float b = (float) (color >> 8 & 255) / 255.0F;
                float a = (float) (color & 255) / 255.0F;

                rectangles.add(new GlyphRenderer.Rectangle(
                        x_off - 1f,
                        y + 9f,
                        x + 1f,
                        y - 1f,
                        0.01f,
                        r, g, b, a
                ));
            }

            if (!rectangles.isEmpty()) {
                FontStorage fontStorage = EmojiTextRenderer.this.getFontStorage(Style.DEFAULT_FONT_ID);
                GlyphRenderer rectangleRenderer = fontStorage.getRectangleRenderer();

                VertexConsumer buffer = vertexConsumers.getBuffer(rectangleRenderer.getLayer(seeThrough ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL));

                for (GlyphRenderer.Rectangle rectangle: rectangles)
                    rectangleRenderer.drawRectangle(rectangle, matrix, buffer, light);
            }
        }

        @Override
        public boolean accept(int index, Style style, int codePoint) {
            if (emojiIndexes.containsKey(index)) {
                ((MatrixStack)vertexConsumers).translate(0D, -2D, 0D);

                int emojiWidth = EmojiTextRenderer.super.getWidth("   ");

                emojiIndexes.get(index).draw(
                        (MatrixStack) vertexConsumers,
                        x,
                        y,
                        emojiWidth,
                        (color >> 24 & 0xff) / 255f);

                x += emojiWidth;

                ((MatrixStack)vertexConsumers).translate(0D, 2D, 0D);
                return true;
            }

            FontStorage fontStorage = EmojiTextRenderer.this.getFontStorage(style.getFont());
            Glyph glyph = fontStorage.getGlyph(codePoint);
            GlyphRenderer glyphRenderer = style.isObfuscated() && codePoint != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph) : fontStorage.getGlyphRenderer(codePoint);

            TextColor textColor = style.getColor();

            float r, g, b, a = (float) (color >> 24 & 255) / 255.0F, dimFactor = shadow ? 0.25f : 1.0f;

            if (textColor != null) {
                int colorValue = textColor.getRgb();

                r = (float) (colorValue >> 16 & 255) / 255.0F * dimFactor;
                g = (float) (colorValue >> 8 & 255) / 255.0F * dimFactor;
                b = (float) (colorValue & 255) / 255.0F * dimFactor;
            } else {
                r = (float) (color >> 16 & 255) / 255.0F * dimFactor;
                g = (float) (color >> 8 & 255) / 255.0F * dimFactor;
                b = (float) (color & 255) / 255.0F * dimFactor;
            }

            if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
                float shadowOffset = shadow ? glyph.getShadowOffset() : 0f;

                VertexConsumer buffer = vertexConsumers.getBuffer(glyphRenderer.getLayer(seeThrough ? TextLayerType.SEE_THROUGH : TextLayerType.NORMAL));
                EmojiTextRenderer.this.drawGlyph(
                        glyphRenderer,
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
            
            float advanceOffset = glyph.getAdvance(style.isBold());
            float shadowOffset = shadow ? 1f : 0f;
            
            if (style.isStrikethrough())
                rectangles.add(new GlyphRenderer.Rectangle(
                        x + shadowOffset - 1f,
                        y + shadowOffset - 4.5f,
                        x + shadowOffset + advanceOffset,
                        y + shadowOffset + 4.5f - 1f,
                        0.01f,
                        r, g, b, a
                ));
            
            if (style.isUnderlined())
                rectangles.add(new GlyphRenderer.Rectangle(
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
}
