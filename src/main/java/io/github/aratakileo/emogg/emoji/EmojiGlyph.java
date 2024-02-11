package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.aratakileo.elegantia.util.Rect2i;
import io.github.aratakileo.emogg.EmoggRenderTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
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

    protected boolean coloredByDefault() {
        return false;
    }

    // Temporary matrix reused for every render to avoid instantiating it every time
    private static final Matrix4f tempMat = new Matrix4f();

    private void setupMatrix(boolean italic, float x, float y, Matrix4f mat) {
        tempMat.set(
                HEIGHT * getAspectRatio(), 0f, 0f, 0f,
                italic ? -HEIGHT * ITALIC_SHEAR : 0f, HEIGHT, 0f, 0f,
                0f, 0f, 0f, 0f,
                x + (italic ? HEIGHT * ITALIC_SHEAR : 0f), y, 0f, 1f
        );
        tempMat.mulLocal(mat);
    }

    @Override
    public final void render(boolean italic, float x, float y, Matrix4f mat, VertexConsumer builder, float r, float g, float b, float a, int packedLightCoords) {
        setupMatrix(italic, x, y, mat);
        if (!coloredByDefault())
            r = g = b = 1f;
        renderImpl(builder, tempMat, r, g, b, a, packedLightCoords);
    }

    public final void renderColored(boolean italic, float x, float y, Matrix4f mat, VertexConsumer builder, float r, float g, float b, float a, int packedLightCoords) {
        setupMatrix(italic, x, y, mat);
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

    public static abstract class TexturedEmojiGlyph extends RectEmojiGlyph {
        private final ResourceLocation texture;

        public TexturedEmojiGlyph(ResourceLocation texture) {
            super(EmoggRenderTypes.emojiTextured(texture));
            this.texture = texture;
        }

        public RenderType grayScaleRenderType(Font.DisplayMode mode) {
            return EmoggRenderTypes.emojiTexturedGrayscale(this.texture).select(mode);
        }
    }

    public static class Atlas extends TexturedEmojiGlyph {
        private final Rect2i rect;

        protected Atlas(ResourceLocation texture, Rect2i rect) {
            super(texture);
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
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }
    }

    public static final EmojiGlyph ERROR = new EmojiGlyph.Error();

    private static class Error extends TexturedEmojiGlyph {
        private Error() {
            super(MissingTextureAtlasSprite.getLocation());
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }
    }

    public static final EmojiGlyph LOADING = new EmojiGlyph.Loading();

    private static class Loading extends RectEmojiGlyph {
        public Loading() {
            super(EmoggRenderTypes.emojiLoading());
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            final int ANIMATION_LENGTH = 2000;
            EmoggRenderTypes.Shaders.Uniform.loadingAnimationTime.set(
                    (float) (Util.getMillis() % ANIMATION_LENGTH) / ANIMATION_LENGTH
            );
            super.renderImpl(builder, mat, r, g, b, a, packedLightCoords);
        }
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
