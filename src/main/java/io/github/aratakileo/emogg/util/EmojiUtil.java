package io.github.aratakileo.emogg.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.aratakileo.suggestionsapi.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import io.github.aratakileo.emogg.handler.Emoji;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public final class EmojiUtil {
    public final static String PNG_EXTENSION = ".png",
            EMOJI_FOLDER_NAME = "emoji";

    public static void render(@NotNull Emoji emoji, @NotNull GuiGraphics guiGraphics, int x, int y, int size) {
        if (Objects.isNull(emoji.getRenderResourceLocation())) return;

        RenderUtil.renderFittedCenterTexture(
                guiGraphics,
                emoji.getRenderResourceLocation(),
                x,
                y,
                emoji.getWidth(),
                emoji.getHeight(),
                size,
                size
        );
    }

    public static @NotNull RenderType getRenderType(@NotNull ResourceLocation resourceLocation) {
        var compositeState = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTextShader))
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                        "translucent_transparency",
                        () -> {
                            RenderSystem.enableBlend();
                            RenderSystem.blendFuncSeparate(
                                    GlStateManager.SourceFactor.SRC_ALPHA,
                                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                    GlStateManager.SourceFactor.ONE,
                                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                            );
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                            },
                        () -> {
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                            RenderSystem.disableBlend();
                            RenderSystem.defaultBlendFunc();
                        }
                ))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .createCompositeState(false);

        return RenderType.create(
                "emogg_renderer",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                compositeState
        );
    }

    public static @NotNull String normalizeNameOrCategory(@NotNull String sourceValue) {
        return StringUtils.strip(
                sourceValue.toLowerCase()
                        .replaceAll("-+| +|\\.+", "_")
                        .replaceAll("[^a-z0-9_]", ""),
                "_"
        );
    }

    public static @NotNull String getNameFromPath(@NotNull ResourceLocation resourceLocation) {
        return getNameFromPath(resourceLocation.toString());
    }

    public static @NotNull String getNameFromPath(@NotNull String path) {
        return path.transform(name -> name.substring(name.lastIndexOf('/') + 1))
                .transform(name -> name.substring(0, name.lastIndexOf('.')));
    }
}
