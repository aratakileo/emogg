package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.aratakileo.emogg.util.Rect2i;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    public static class Atlas extends EmojiGlyph {
        private final Rect2i rect;

        public Atlas(GlyphRenderTypes glyphRenderTypes, Rect2i rect) {
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
            builder.vertex(mat, 0f, 0f, 0f).color(r, g, b, a).uv(u0, v0).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 0f, 1f, 0f).color(r, g, b, a).uv(u0, v1).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 1f, 0f).color(r, g, b, a).uv(u1, v1).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 0f, 0f).color(r, g, b, a).uv(u1, v0).uv2(packedLightCoords).endVertex();
        }

        public Rect2i getRect() {
            return rect;
        }
    }

    public static final EmojiGlyph ERROR = new EmojiGlyph.Error();

    private static class Error extends EmojiGlyph {
        private static final GlyphRenderTypes RENDERTYPES =
                GlyphRenderTypes.createForColorTexture(MissingTextureAtlasSprite.getLocation());

        private Error() {
            super(RENDERTYPES);
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            r = g = b = 1f;
            builder.vertex(mat, 0f, 0f, 0f).color(r, g, b, a).uv(0f, 0f).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 0f, 1f, 0f).color(r, g, b, a).uv(0f, 1f).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 1f, 0f).color(r, g, b, a).uv(1f, 1f).uv2(packedLightCoords).endVertex();
            builder.vertex(mat, 1f, 0f, 0f).color(r, g, b, a).uv(1f, 0f).uv2(packedLightCoords).endVertex();
        }
    }

    public static class Loading extends EmojiGlyph {
        public Loading() {
            super(EmojiGlyphRenderTypes.emojiNoTexture());
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(VertexConsumer builder, Matrix4f mat, float r, float g, float b, float a, int packedLightCoords) {
            r = g = b = 1f;
            //TODO proper loading animation
            for (int i = 0; i < 9; i++) {
                builder.vertex(mat, (float) Math.random(), (float) Math.random(), 0f).color(r, g, b, a).uv2(packedLightCoords).endVertex();
            }
        }
    }

    public static final EmojiGlyph EMPTY = new EmojiGlyph.Empty();

    private static class Empty extends EmojiGlyph {
        private Empty() {
            super(EmojiGlyphRenderTypes.emojiNoTexture());
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
