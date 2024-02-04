package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.aratakileo.emogg.EmoggRenderTypes;
import io.github.aratakileo.emogg.util.Rect2i;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public abstract class EmojiGlyph extends BakedGlyph implements GlyphInfo {
    public static final float HEIGHT = 8f;
    public static final float ITALIC_SHEAR = 0.25f;

    public EmojiGlyph(GlyphRenderTypes glyphRenderTypes) {
        super(glyphRenderTypes,0, 0, 0, 0, 0, 0, 0, 0);
    }

    public abstract float getAspectRatio();

    // Temporary matrix reused for every render to avoid instantiating it every time
    private static final Matrix4f tempMat = new Matrix4f();

    @Override
    public final void render(boolean italic, float x, float y, Matrix4f mat, VertexConsumer builder, float r, float g, float b, float a, int packedLightCoords) {
        tempMat.set(
                HEIGHT * getAspectRatio(), 0f, 0f, 0f,
                italic ? -HEIGHT * ITALIC_SHEAR : 0f, HEIGHT, 0f, 0f,
                0f, 0f, 0f, 0f,
                x + (italic ? HEIGHT * ITALIC_SHEAR : 0f), y, 0f, 1f
        );

        tempMat.mulLocal(mat);

        renderImpl(builder, tempMat, r, g, b, a, packedLightCoords);
    }

    /**
     * Internal rendering method.
     * <p>
     * Transformation is pre-calculated and applied via the {@code Matrix4f mat} matrix,
     * so always render into a normalized coordinate system (0 to 1 on both axis, x points to the right, and y points down) in this method.
     * <p>
     * See the implementation in {@link EmojiGlyph.Atlas} for an example.
     */
    @SuppressWarnings("SameParameterValue")
    protected abstract void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords);

    @Override
    public final float getAdvance() {
        return HEIGHT * getAspectRatio();
    }
    @Override
    public final float getAdvance(boolean bl) { return getAdvance(); }
    @Override
    public final float getBoldOffset() { return 0f; }
    @Override
    public final float getShadowOffset() { return 0f; }
    @Override
    public final @NotNull BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) { return this; }

    /**
     * EmojiGlyph Implementations
     */

    private static abstract class RectEmojiGlyph extends EmojiGlyph {
        public RectEmojiGlyph(GlyphRenderTypes glyphRenderTypes) {
            super(glyphRenderTypes);
            u0 = v0 = 0f;
            u1 = v1 = 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            builder.vertex(mat, 0f, 0f, 0f).color(r, g, b, a).uv(u0, v0).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 0f, 1f, 0f).color(r, g, b, a).uv(u0, v1).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 1f, 0f).color(r, g, b, a).uv(u1, v1).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 0f, 0f).color(r, g, b, a).uv(u1, v0).uv2(packedLightCoords).endVertex();
        }
    }

    public static class Atlas extends RectEmojiGlyph {
        private final Rect2i rect;

        protected Atlas(GlyphRenderTypes glyphRenderTypes, Rect2i rect) {
            super(glyphRenderTypes);
            this.rect = rect;
        }

        void updateUV(int atlasWidth, int atlasHeight) {
            this.u0 = (float) rect.getX() / atlasWidth;
            this.v0 = (float) rect.getY() / atlasHeight;
            this.u1 = (float) rect.getRight() / atlasWidth;
            this.v1 = (float) rect.getBottom() / atlasHeight;
        }

        @Override
        public float getAspectRatio() {
            return (float) rect.getWidth() / rect.getHeight();
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            r = g = b = 1f;
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }
    }

    public static final EmojiGlyph ERROR = new EmojiGlyph.Error();

    private static class Error extends RectEmojiGlyph {
        private Error() {
            super(EmoggRenderTypes.emojiTextured(MissingTextureAtlasSprite.getLocation()));
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            r = g = b = 1f;
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }
    }

    public static final EmojiGlyph LOADING = new EmojiGlyph.Loading();

    private static class Loading extends RectEmojiGlyph {
        public Loading() {
            super(EmoggRenderTypes.EMOJI_LOADING);
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            final int ANIMATION_LENGTH = 3000;
            EmoggRenderTypes.Shaders.Uni.loadingAnimationTime.set(
                    (float) (Util.getMillis() % ANIMATION_LENGTH) / ANIMATION_LENGTH
            );
            r = g = b = 1f;
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }

//        private void centerCircle(VertexConsumer builder, Matrix4f mat, float radius, float z, float r, float g, float b, float a, int packedLightCoords) {
//            final int EDGES = 16;
//            for (int i = 0; i <= EDGES; i++) {
//                float rad = (float) i / EDGES * Mth.TWO_PI;
//                float x = Mth.cos(-rad) * radius + 0.5f;
//                float y = Mth.sin(-rad) * radius + 0.5f;
//
//                builder.vertex(mat, x, y, z)
//                        .color(r, g, b, a)
//                        .uv2(packedLightCoords)
//                        .endVertex();
//                if (i != 0) {
//                    builder.vertex(mat, 0.5f, 0.5f, z)
//                            .color(r, g, b, a)
//                            .uv2(packedLightCoords)
//                            .endVertex();
//                    if (i != EDGES) {
//                        builder.vertex(mat, x, y, z)
//                                .color(r, g, b, a)
//                                .uv2(packedLightCoords)
//                                .endVertex();
//                    }
//                }
//            }
//        }
//
//        @Override
//        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
//            r = g = b = 1f;
//
//            float time = (float) (Util.getMillis() - initialTime) / 1000f;
//            time *= 0.5f; // speed
//            time %= 1f;
//
//            centerCircle(builder, mat, 0.5f, 0f, r, g, b, 0.3f, packedLightCoords);
//
//            float radius = Math.min(time * 2f, 1f);
//            radius = Mth.sin(radius * Mth.HALF_PI);
//            radius *= 0.5f;
//            float alpha = Math.min(2f - time * 2f, 1f);
//            alpha *= 0.5f;
//
//            centerCircle(builder, mat, radius, 0.01f, r, g, b, alpha, packedLightCoords);
//        }
    }

    public static final EmojiGlyph EMPTY = new EmojiGlyph.Empty();

    private static class Empty extends EmojiGlyph {
        private Empty() {
            super(EmoggRenderTypes.emojiNoTexture());
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
        }
    }
}
