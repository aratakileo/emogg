package pextystudios.emogg.emoji.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.NativeGifImage;
import pextystudios.emogg.util.StringUtil;

import java.util.concurrent.ConcurrentHashMap;

public class AnimatedEmoji extends Emoji {
    protected ConcurrentHashMap<Integer, Pair<ResourceLocation, Integer>> framesData;
    protected int totalDelayTime;

    public AnimatedEmoji(String name) {
        super(name);
    }

    public AnimatedEmoji(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public AnimatedEmoji(String name, String fileName) {
        super(name, fileName);
    }

    public AnimatedEmoji(String name, ResourceLocation resourceLocation) {
        super(name, resourceLocation);
    }

    @Override
    public ResourceLocation getRenderResourceLocation() {
        if (framesData.size() == 1)
            return framesData.get(0).getA();

        var currentPart = (int)(System.currentTimeMillis() / 10D % totalDelayTime);

        while (!framesData.containsKey(currentPart))
            currentPart--;

        return framesData.get(currentPart).getA();
    }

    @Override
    public boolean isAnimated() {
        return framesData.size() > 1;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && totalDelayTime > 0;
    }

    @Override
    protected void load() {
        try {
            final var minecraft = Minecraft.getInstance();
            final var nativeGifImage = NativeGifImage.read(resourceLocation);
            final var sourceFilePath = resourceLocation.getPath();
            final var sourceFileName = sourceFilePath.substring(sourceFilePath.lastIndexOf('/') + 1);

            width = nativeGifImage.getWidth();
            height = nativeGifImage.getHeight();
            framesData = new ConcurrentHashMap<>();

            var i = 0;
            var totalDelayTime = 0;

            for (var frame: nativeGifImage.getFrames()) {
                final var framePath = "emoji/" + sourceFileName.substring(0, sourceFileName.lastIndexOf('.')) + "_frame_" + i + ".png";
                final var frameDynamicTexture = new DynamicTexture(frame.nativeImage());

                framesData.put(totalDelayTime, new Pair<>(
                        minecraft.getTextureManager().register(framePath, frameDynamicTexture),
                        frame.delay()
                ));
                totalDelayTime += frame.delay();
                i++;
            }

//            var imageData = EmojiUtil.splitGif(resourceLocation);
//
//            width = imageData.getA().getA();
//            height = imageData.getA().getB();
//            totalDelayTime = imageData.getB();
//            framesData = imageData.getC();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: " + StringUtil.repr(resourceLocation), e);
        }
    }
}
