package pextystudios.emogg.emoji;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.StringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Emoji {
    protected final String name;
    protected final ResourceLocation resourceLocation;
    protected int width = -1, height = -1;

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
        this(name, new ResourceLocation(Emogg.NAMESPACE, fileName.replace('\\', '/')));
    }

    public Emoji(String name, ResourceLocation resourceLocation) {
        this.name = StringUtil.strip(
                name.toLowerCase()
                        .replaceAll("-+| +|\\.+", "_")
                        .replaceAll("[^a-z0-9_]", ""),
                '_'
        );

        this.resourceLocation = resourceLocation;

        load();
    }

    public void render(
            float x,
            float y,
            Matrix4f matrix4f,
            MultiBufferSource multiBufferSource,
            int light
    ) {
        render(resourceLocation, x, y, matrix4f, multiBufferSource, light);
    }

    protected void render(
            ResourceLocation resourceLocation,
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

        var buffer = multiBufferSource.getBuffer(EmojiUtil.getRenderType(resourceLocation));

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

    public String getName() {return name;}

    public String getCode() {return ':' + name + ':';}

    public ResourceLocation getResourceLocation() {return resourceLocation;}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAnimated() {
        return false;
    }

    public boolean isValid() {
        return width != -1 && height != -1;
    }

    @Override
    public String toString() {
        return '{' + name + ": " + resourceLocation.getPath() + '}';
    }

    protected void load() {
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            var bufferedImage = ImageIO.read(resource.getInputStream());

            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();

            resource.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: \"" + resourceLocation.getPath() + '"', e);
        }
    }
}
