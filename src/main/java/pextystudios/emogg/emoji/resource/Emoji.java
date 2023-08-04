package pextystudios.emogg.emoji.resource;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.handler.EmojiHandler;
import pextystudios.emogg.emoji.font.EmojiLiteral;
import pextystudios.emogg.util.StringUtil;


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
            category = EmojiHandler.CATEGORY_DEFAULT;

        this.category = normalizeNameOrCategory(category);

        load();
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
            var bufferedImage = NativeImage.read(resource.get().open());

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
