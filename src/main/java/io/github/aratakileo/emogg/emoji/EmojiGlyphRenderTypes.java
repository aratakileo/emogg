package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import io.github.aratakileo.emogg.EmoggConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.*;

@Environment(EnvType.CLIENT)
public class EmojiGlyphRenderTypes {
    // ##### Emoji Custom Begin #####

    private static final Function<ResourceLocation, RenderType> RT_EMOJI = texture -> init(RenderType.create(
            "emoji",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    ));
    private static final Function<ResourceLocation, RenderType> RT_EMOJI_SEE_THROUGH = texture -> init(RenderType.create(
            "emoji_see_through",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI_SEE_THROUGH)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    ));
    private static final Function<ResourceLocation, RenderType> RT_EMOJI_POLYGON_OFFSET = texture -> init(RenderType.create(
            "emoji_polygon_offset",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    ));

    private static final Function<ResourceLocation, GlyphRenderTypes> EMOJI_CUSTOM =
            texture -> new GlyphRenderTypes(
                    RT_EMOJI.apply(texture),
                    RT_EMOJI_SEE_THROUGH.apply(texture),
                    RT_EMOJI_POLYGON_OFFSET.apply(texture)
            );

    // ##### Emoji Vanilla Begin #####

    private static final Function<ResourceLocation, RenderType> RT_VANILLA_EMOJI = texture -> init(RenderType.create(
            "emoji",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    ));
    private static final Function<ResourceLocation, RenderType> RT_VANILLA_EMOJI_SEE_THROUGH = texture -> init(RenderType.create(
            "emoji_see_through",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    ));
    private static final Function<ResourceLocation, RenderType> RT_VANILLA_EMOJI_POLYGON_OFFSET = texture -> init(RenderType.create(
            "emoji_polygon_offset",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    ));

    private static final Function<ResourceLocation, GlyphRenderTypes> EMOJI_VANILLA =
            texture -> new GlyphRenderTypes(
                    RT_VANILLA_EMOJI.apply(texture),
                    RT_VANILLA_EMOJI_SEE_THROUGH.apply(texture),
                    RT_VANILLA_EMOJI_POLYGON_OFFSET.apply(texture)
            );

    public static GlyphRenderTypes emoji(ResourceLocation texture) {
        if (EmoggConfig.instance.useCustomShaders) {
            return EMOJI_CUSTOM.apply(texture);
        } else {
            return EMOJI_VANILLA.apply(texture);
        }
    }

    // ##### Emoji No Texture Custom Begin #####

    private static final int NO_TEXTURE_BUFFER_SIZE = 256;
    private static final VertexFormat.Mode NO_TEXTURE_VERTEX_FORMAT = VertexFormat.Mode.TRIANGLES;
    private static final RenderType RT_EMOJI_NO_TEXTURE = init(RenderType.create(
            "emoji_no_texture",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI_NO_TEXTURE)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    ));
    private static final RenderType RT_EMOJI_NO_TEXTURE_SEE_THROUGH = init(RenderType.create(
            "emoji_no_texture_see_through",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI_NO_TEXTURE_SEE_THROUGH)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    ));
    private static final RenderType RT_EMOJI_NO_TEXTURE_POLYGON_OFFSET = init(RenderType.create(
            "emoji_no_texture_polygon_offset",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.EMOJI_NO_TEXTURE)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    ));

    // ##### Emoji No Texture Vanilla Begin #####

    private static final RenderType RT_VANILLA_EMOJI_NO_TEXTURE = init(RenderType.create(
            "emoji_no_texture",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    ));
    private static final RenderType RT_VANILLA_EMOJI_NO_TEXTURE_SEE_THROUGH = init(RenderType.create(
            "emoji_no_texture_see_through",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    ));
    private static final RenderType RT_VANILLA_EMOJI_NO_TEXTURE_POLYGON_OFFSET = init(RenderType.create(
            "emoji_no_texture_polygon_offset",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            NO_TEXTURE_VERTEX_FORMAT,
            NO_TEXTURE_BUFFER_SIZE,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_BACKGROUND_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setLayeringState(POLYGON_OFFSET_LAYERING)
                    .createCompositeState(false)
    ));

    private static final GlyphRenderTypes EMOJI_NO_TEXTURE_CUSTOM = new GlyphRenderTypes(
            RT_EMOJI_NO_TEXTURE,
            RT_EMOJI_NO_TEXTURE_SEE_THROUGH,
            RT_EMOJI_NO_TEXTURE_POLYGON_OFFSET
    );
    private static final GlyphRenderTypes EMOJI_NO_TEXTURE_VANILLA = new GlyphRenderTypes(
            RT_VANILLA_EMOJI_NO_TEXTURE,
            RT_VANILLA_EMOJI_NO_TEXTURE_SEE_THROUGH,
            RT_VANILLA_EMOJI_NO_TEXTURE_POLYGON_OFFSET
    );

    public static GlyphRenderTypes emojiNoTexture() {
        if (EmoggConfig.instance.useCustomShaders) {
            return EMOJI_NO_TEXTURE_CUSTOM;
        } else {
            return EMOJI_NO_TEXTURE_VANILLA;
        }
    }

    private static RenderType init(RenderType renderType) {
        Minecraft.getInstance()
                .renderBuffers()
                .fixedBuffers
                .put(renderType, new BufferBuilder(renderType.bufferSize()));
        return renderType;
    }

    public static class Shaders {
        private static @Nullable ShaderInstance emoji;
        private static @Nullable ShaderInstance emojiSeeThrough;
        private static @Nullable ShaderInstance emojiNoTexture;
        private static @Nullable ShaderInstance emojiNoTextureSeeThrough;

        private static final ShaderStateShard EMOJI = new ShaderStateShard(() -> emoji);
        private static final ShaderStateShard EMOJI_SEE_THROUGH = new ShaderStateShard(() -> emojiSeeThrough);
        private static final ShaderStateShard EMOJI_NO_TEXTURE = new ShaderStateShard(() -> emojiNoTexture);
        private static final ShaderStateShard EMOJI_NO_TEXTURE_SEE_THROUGH = new ShaderStateShard(() -> emojiNoTextureSeeThrough);

        public static void loadShaders(ResourceProvider resourceProvider, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list) throws IOException {
            list.add(Pair.of(
                    new ShaderInstance(resourceProvider, "emoji", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    shader -> emoji = shader
            ));
            list.add(Pair.of(
                    new ShaderInstance(resourceProvider, "emoji_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                    shader -> emojiSeeThrough = shader
            ));
            list.add(Pair.of(
                    new ShaderInstance(resourceProvider, "emoji_no_texture", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP),
                    shader -> emojiNoTexture = shader
            ));
            list.add(Pair.of(
                    new ShaderInstance(resourceProvider, "emoji_no_texture_see_through", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP),
                    shader -> emojiNoTextureSeeThrough = shader
            ));
        }
    }
}
