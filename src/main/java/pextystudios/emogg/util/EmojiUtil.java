package pextystudios.emogg.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.Node;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public final class EmojiUtil {
    public static Triplet<Pair<Integer, Integer>, Integer, ConcurrentHashMap<Integer, Pair<ResourceLocation, Integer>>> splitGif(
            ResourceLocation resourceLocation
    ) throws IOException {
        var resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        var frames = new ConcurrentHashMap<Integer, Pair<ResourceLocation, Integer>>();

        var reader = ImageIO.getImageReadersBySuffix("gif").next();
        reader.setInput(ImageIO.createImageInputStream(resource.get().open()), false);

        var metadata = reader.getImageMetadata(0);
        var metaFormatName = metadata.getNativeMetadataFormatName();
        var imageSize = new Pair<>(0, 0);
        var totalDelayTime = -1;
        var frameDelayTime = 1;

        for (var i = 0; i < reader.getNumImages(true); i++) {
            totalDelayTime += frameDelayTime;
            frameDelayTime = 1;
            var image = reader.read(i);

            var newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            newImage.getGraphics().drawImage(image, 0, 0, null);

            var root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree(metaFormatName);

            for (int j = 0; j < root.getLength(); j++) {
                Node node = root.item(j);

                if (node.getNodeName().equalsIgnoreCase("GraphicControlExtension")) {
                    frameDelayTime = Integer.parseInt(((IIOMetadataNode) node).getAttribute("delayTime"));
                    break;
                }
            }

            var originalFileName = resourceLocation.getPath();
            originalFileName = originalFileName.substring(originalFileName.lastIndexOf('/') + 1);

            var newImageOutputStream = new ByteArrayOutputStream();
            ImageIO.write(newImage, "png", newImageOutputStream);

            var newImageStream = new ByteArrayInputStream(newImageOutputStream.toByteArray());

            imageSize = new Pair<>(
                    Math.max(imageSize.getA(), newImage.getWidth()),
                    Math.max(imageSize.getB(), newImage.getHeight())
            );
            
            frames.put(
                    totalDelayTime,
                    new Pair<>(
                            Minecraft.getInstance()
                                    .getTextureManager()
                                    .register(
                                            "emoji/" + originalFileName.substring(
                                                    0, originalFileName.lastIndexOf('.')
                                            ) + "_frame_" + i + ".png",
                                            new DynamicTexture(NativeImage.read(newImageStream))
                                    ),
                            frameDelayTime
                    )
            );
        }

        totalDelayTime += frameDelayTime;

        return new Triplet<>(imageSize, totalDelayTime, frames);
    }

    public static RenderType getRenderType(ResourceLocation resourceLocation) {
        var compositeState = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTextShader))
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(
                            GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                            GlStateManager.SourceFactor.ONE,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                    );
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                }, () -> {
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
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
}
