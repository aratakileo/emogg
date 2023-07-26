package pextystudios.emogg.emoji.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.EmojiHandler;
import pextystudios.emogg.util.EmojiUtil;
import pextystudios.emogg.util.RenderUtil;
import pextystudios.emogg.util.StringUtil;

import javax.imageio.ImageIO;

public class Emoji {
    protected final String name;
    protected final ResourceLocation resourceLocation;
    protected final String category;
    protected int width = -1, height = -1;

    public Emoji(String name) {
        this(name, EmojiHandler.EMOJIS_PATH_PREFIX + '/' + name + EmojiHandler.STATIC_EMOJI_EXTENSION);
    }

    public Emoji(ResourceLocation resourceLocation) {
        this(getNameFromPath(resourceLocation.getPath()), resourceLocation);
    }

    public Emoji(String name, String fileName) {
        this(name, new ResourceLocation(Emogg.NAMESPACE, fileName.replace('\\', '/')));
    }

    public Emoji(String name, ResourceLocation resourceLocation) {
        this.name = normalizeNameOrCategory(name);
        this.resourceLocation = resourceLocation;

        var category = resourceLocation.getPath().substring(EmojiHandler.EMOJIS_PATH_PREFIX.length() + 1);
        if (category.contains("/")) {
            var splitPath = category.split("/");

            category = splitPath[splitPath.length - 2];
        } else
            category = EmojiHandler.DEFAULT_EMOJI_CATEGORY;

        this.category = normalizeNameOrCategory(category);

        load();
    }

    public void render(int x, int y, int size, GuiGraphics guiGraphics) {
        var width = size;
        var height = size;

        if (this.width < this.height) {
            width *= ((float) this.width / this.height);
            x += (size - width) / 2;
        }
        else if (this.height < this.width) {
            height *= ((float) this.height / this.width);
            y += (size - height) / 2;
        }

        RenderUtil.renderTexture(guiGraphics, getRenderResourceLocation(), x, y, width, height);
    }

    public void render(
            float x,
            float y,
            Matrix4f matrix4f,
            MultiBufferSource multiBufferSource,
            int light
    ) {
        float textureSize = 16, textureX = 0, textureY = 0, textureOffset = 16 / textureSize, offsetY = 1, offsetX = 0,
                width = EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE, height = EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE;
        if (this.width < this.height) {
            width *= ((float) this.width / this.height);
            x += (EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE - width) / 2;
        }
        else if (this.height < this.width) {
            height *= ((float) this.height / this.width);
            y += (EmojiHandler.EMOJI_DEFAULT_RENDER_SIZE - height) / 2;
        }

        var buffer = multiBufferSource.getBuffer(EmojiUtil.getRenderType(getRenderResourceLocation()));

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

    public String getCategory() {return category;}

    public String getCode() {
        return ":" + name + ':';
    }

    public String getEscapedCode() {return '\\' + getCode();}

    public ResourceLocation getResourceLocation() {return resourceLocation;}

    public ResourceLocation getRenderResourceLocation() {return  resourceLocation;}

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

    protected void load() {
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            var bufferedImage = ImageIO.read(resource.get().open());

            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load " + StringUtil.repr(resourceLocation), e);
        }
    }

    public static Emoji from(ResourceLocation resourceLocation) {
        return from(normalizeNameOrCategory(getNameFromPath(resourceLocation)), resourceLocation);
    }

    public static Emoji from(String name, ResourceLocation resourceLocation) {
        if (resourceLocation.getPath().endsWith(EmojiHandler.ANIMATED_EMOJI_EXTENSION))
            return new AnimatedEmoji(name, resourceLocation);

        return new Emoji(name, resourceLocation);
    }

    public static String normalizeNameOrCategory(String sourceValue) {
        return StringUtils.strip(
                sourceValue.toLowerCase()
                        .replaceAll("-+| +|\\.+", "_")
                        .replaceAll("[^a-z0-9_]", ""),
                "_"
        );
    }

    public static String getNameFromPath(ResourceLocation resourceLocation) {
        return getNameFromPath(resourceLocation.toString());
    }

    public static String getNameFromPath(String path) {
        return path.transform(name -> name.substring(name.lastIndexOf('/') + 1))
                .transform(name -> name.substring(0, name.lastIndexOf('.')));
    }
}
