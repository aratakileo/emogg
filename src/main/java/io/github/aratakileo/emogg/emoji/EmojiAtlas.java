package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.util.Rect2i;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;

@Environment(EnvType.CLIENT)
public class EmojiAtlas {
    private static final String NAME = "emoji_atlas_%d";
    private static final List<EmojiAtlasTexture> textures = new ArrayList<>();

    public static @NotNull EmojiGlyph.Atlas stitch(NativeImage image) {
        RenderSystem.assertOnRenderThreadOrInit();
        for (var texture : textures) {
            var glyph = texture.stitch(image);
            if (glyph != null) return glyph;
        }

        var texture = new EmojiAtlasTexture(NAME.formatted(textures.size()));
        textures.add(texture);
        return Objects.requireNonNull(texture.stitch(image));
    }

    public static void clear() {
        textures.forEach(EmojiAtlasTexture::close);
        textures.clear();
    }

    static {
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            if (EmoggConfig.instance.isDebugModeEnabled && !textures.isEmpty()) {
                textures.get(textures.size() - 1).drawDebugHUD(guiGraphics);
            }
        });
    }

    private static class EmojiAtlasTexture extends AbstractTexture implements Dumpable {
        private final ResourceLocation name;
        private int totalWidth = 256, totalHeight = 256;
        private final LinkedList<Rect2i> freeSpace = new LinkedList<>();

        private final Collection<EmojiGlyph.Atlas> stitchedGlyphs = new ArrayList<>();

        private final GlyphRenderTypes glyphRenderTypes;

        // Can be changed to fill the bg with a specific color for debugging
        private static final int BG_FILL_COLOR = 0x00000000;

        public EmojiAtlasTexture(String name) {
            RenderSystem.assertOnRenderThreadOrInit();

            this.name = new ResourceLocation(Emogg.NAMESPACE_OR_ID, name);
            Minecraft.getInstance().getTextureManager().register(this.name, this);

            TextureUtil.prepareImage(
                    NativeImage.InternalGlFormat.RGBA,
                    getId(),
                    totalWidth, totalHeight
            );
            setFilter(
                    true, // Linear Filtering
                    false // No Mipmap
            );
            fillBackground();

            freeSpace.add(new Rect2i(0, 0, totalWidth, totalHeight));
            glyphRenderTypes = EmojiGlyphRenderTypes.emoji(this.name);

            Emogg.LOGGER.info("Created emoji atlas texture: {}x{} {}",
                    totalWidth, totalHeight, getName());
        }

        @Override
        public void load(ResourceManager resourceManager) {
        }

        public @Nullable EmojiGlyph.Atlas stitch(NativeImage image) {
            RenderSystem.assertOnRenderThreadOrInit();

            Vector2i pos;
            while ((pos = fit(image.getWidth(), image.getHeight(), 1)) == null) {
                if (!expand()) return null;
            }

            if (EmoggConfig.instance.isDebugModeEnabled)
                Emogg.LOGGER.info("Stitching emoji texture to ({},{})", pos.x, pos.y);

            bind();
            image.upload(0, pos.x, pos.y, false);

            var glyph = new EmojiGlyph.Atlas(
                    glyphRenderTypes,
                    new Rect2i(pos.x, pos.y, image.getWidth(), image.getHeight())
            );
            glyph.updateUV(totalWidth, totalHeight);
            stitchedGlyphs.add(glyph);
            return glyph;
        }

        private @Nullable Vector2i fit(int width, int height, int padding) {
            width += padding * 2;
            height += padding * 2;

            var iter = freeSpace.listIterator();
            while (iter.hasNext()) {
                var rect = iter.next();
                if (rect.getWidth() >= width && rect.getHeight() >= height) {
                    var pos = new Vector2i(rect.getX() + padding, rect.getY() + padding);

                    // #########  |  #########
                    // ##### 0 #  |  #####   #
                    // #########  |  ##### 1 #
                    // #   1   #  |  # 0 #   #
                    // #########  |  #########
                    // Solution A |  Solution B

                    // Try to keep the rects square-like
                    double ratioA0 = (double) (rect.getWidth() - width) / (height);
                    double ratioA1 = (double) (rect.getWidth()) / (rect.getHeight() - height);
                    double ratioB0 = (double) (width) / (rect.getHeight() - height);
                    double ratioB1 = (double) (rect.getWidth() - width) / (rect.getHeight());
                    double ratioA = Math.max(
                            ratioA0 > 1 ? ratioA0 : (1 / ratioA0),
                            ratioA1 > 1 ? ratioA1 : (1 / ratioA1)
                    );
                    double ratioB = Math.max(
                            ratioB0 > 1 ? ratioB0 : (1 / ratioB0),
                            ratioB1 > 1 ? ratioB1 : (1 / ratioB1)
                    );

                    Rect2i toAdd;
                    // toAdd -> 0
                    // rect -> 1
                    if (ratioA < ratioB) {
                        // Solution A
                        toAdd = new Rect2i(
                                rect.getX() + width,
                                rect.getY(),
                                rect.getWidth() - width,
                                height
                        );
                        rect.setY(rect.getY() + height);
                        rect.setHeight(rect.getHeight() - height);
                    } else {
                        // Solution B
                        toAdd = new Rect2i(
                                rect.getX(),
                                rect.getY() + height,
                                width,
                                rect.getHeight() - height
                        );
                        rect.setX(rect.getX() + width);
                        rect.setWidth(rect.getWidth() - width);
                    }

                    // Keep small rectangular spaces in the front reduce fragmentation
                    iter.remove();
                    if (rect.hasArea()) freeSpace.addFirst(rect);
                    if (toAdd.hasArea()) freeSpace.addFirst(toAdd);

                    return pos;
                }
            }
            return null;
        }

        private boolean expand() {
            RenderSystem.assertOnRenderThreadOrInit();

            final int LIMIT = RenderSystem.maxSupportedTextureSize();
            if (totalWidth == LIMIT && totalHeight == LIMIT)
                return false;

            int oldWidth = totalWidth, oldHeight = totalHeight;
            if (totalWidth <= totalHeight) {
                totalWidth = Math.min(totalWidth * 2, LIMIT);
                freeSpace.add(new Rect2i(oldWidth, 0, totalWidth - oldWidth, totalHeight));
            } else {
                totalHeight = Math.min(totalHeight * 2, LIMIT);
                freeSpace.add(new Rect2i(0, oldHeight, totalWidth, totalHeight - oldHeight));
            }

            Emogg.LOGGER.info("Expanding emoji atlas: %dx%d -> %dx%d".formatted(
                    oldWidth, oldHeight,
                    totalWidth, totalHeight
            ));

            // Copy data
            bind();
            var image = new NativeImage(NativeImage.Format.RGBA, oldWidth, oldHeight, false);
            image.downloadTexture(0, false);

            // Create new texture
            releaseId();
            TextureUtil.prepareImage(
                    NativeImage.InternalGlFormat.RGBA,
                    getId(),
                    totalWidth, totalHeight
            );
            setFilter(
                    true, // Linear Filtering
                    false // No Mipmap
            );
            fillBackground();

            // Apply old data
            bind();
            image.upload(0, 0, 0, false);
            image.close();

            // Update current glyphs
            stitchedGlyphs.forEach(glyph -> glyph.updateUV(totalWidth, totalHeight));

            return true;
        }

        private void fillBackground() {
            try (var image = new NativeImage(NativeImage.Format.RGBA, totalWidth, totalHeight, false)) {
                image.fillRect(0, 0, totalWidth, totalHeight, BG_FILL_COLOR);
                bind();
                image.upload(0, 0, 0, false);
            }
        }

        private void _debugDrawFreeSpace() {
            RenderSystem.assertOnRenderThreadOrInit();
            var image = new NativeImage(NativeImage.Format.RGBA, totalWidth, totalHeight, false);
            var random = new Random();
            for (var rect : freeSpace) {
                try {
                    image.fillRect(
                            rect.getX(), rect.getY(),
                            rect.getWidth() - 1, rect.getHeight() - 1,
                            random.nextInt(0xFFFFFF + 1) | 0xFF000000
                    );
                } catch (IllegalArgumentException e) {
                    Emogg.LOGGER.warn(e.toString());
                }
            }
//        image.drawPixels();
            bind();
            image.upload(0, 0, 0, false);
            image.close();
        }

        @Override
        public void dumpContents(ResourceLocation resourceLocation, Path path) {
//        _debugDrawFreeSpace();

            TextureUtil.writeAsPNG(
                    path, resourceLocation.toDebugFileName(),
                    getId(),
                    0,
                    totalWidth, totalHeight
            );
        }

        public ResourceLocation getName() {
            return name;
        }

        public void drawDebugHUD(GuiGraphics guiGraphics) {
            double scale = (double) guiGraphics.guiHeight() / totalHeight;

            guiGraphics.blit(
                    getName(),
                    0, 0,
                    (int) (totalWidth * scale), (int) (totalHeight * scale),
                    0f, 0f,
                    totalWidth, totalHeight,
                    totalWidth, totalHeight
            );

            var random = new Random();
            for (var rect : freeSpace) {
                random.setSeed(rect.hashCode());
                guiGraphics.fill(
                        (int) (rect.getX() * scale), (int) (rect.getY() * scale),
                        (int) (rect.getRight() * scale), (int) (rect.getBottom() * scale),
                        random.nextInt(0, 0xFFFFFF+1) | (128 << 24)
                );
            }
        }
    }
}
