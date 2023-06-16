package pextystudios.emogg;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class Emoji {
    private final String name;
    private final ResourceLocation resourceLocation;
    private int width = -1, height = -1;

    public Emoji(String name) {
        this(name, "emoji/" + name + ".png");
    }

    public Emoji(ResourceLocation resourceLocation) {
        this(
                resourceLocation.getPath()
                        .transform(name -> name.substring(name.lastIndexOf('/') + 1))
                        .transform(name -> name.substring(0, name.lastIndexOf('.'))),
                resourceLocation
        );
    }

    public Emoji(String name, String fileName) {
        this(name, new ResourceLocation(Emogg.NAMESPACE + ":" +  fileName.replace('\\', '/')));
    }

    public Emoji(String name, ResourceLocation resourceLocation) {
        this.name = name.toLowerCase()
                .replaceAll("-+| +|\\.+", "_")
                .replaceAll("[^a-z0-9_]", "");

        this.resourceLocation = resourceLocation;

        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(getResourceLocation());
            BufferedImage bufferedImage = ImageIO.read(resource.getInputStream());

            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();

            resource.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: \"" + resourceLocation.getPath() + '"', e);
        }
    }

    public void render(
            float x,
            float y,
            Matrix4f matrix4f,
            MultiBufferSource multiBufferSource,
            int light
    ) {
        float textureSize = 16, textureX = 0, textureY = 0, textureOffset = 16 / textureSize, size = 10,
                offsetY = 1, offsetX = 0, width = size, height = size;
        if (this.width < this.height) {
            width *= ((float) this.width / this.height);
            x += (size - width) / 2;
        }
        else if (this.height < this.width) {
            height *= ((float) this.height / this.width);
            y += (size - height) / 2;
        }

        VertexConsumer buffer = multiBufferSource.getBuffer(getRenderType());

        buffer.vertex(matrix4f, x - offsetX, y - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(textureX, textureY)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX, y + height - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX, textureY + textureOffset)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX + width, y + height - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX + textureOffset, textureY + textureOffset)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX + width, y - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX + textureOffset, textureY / textureSize)
                .uv2(light)
                .endVertex();
    }

    public RenderType getRenderType() {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTextShader))
                .setTextureState(new RenderStateShard.TextureStateShard(getResourceLocation(), false, false))
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

    public String getName() {return name;}

    public String getCode() {return ':' + this.name + ':';}

    public ResourceLocation getResourceLocation() {return this.resourceLocation;}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return '{' + name + ": " + resourceLocation.getPath() + '}';
    }
}
