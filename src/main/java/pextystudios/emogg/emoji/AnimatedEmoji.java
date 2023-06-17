package pextystudios.emogg.emoji;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import oshi.util.tuples.Pair;
import pextystudios.emogg.Emogg;

import java.util.List;

public class AnimatedEmoji extends Emoji {
    protected List<Pair<ResourceLocation, Integer>> framesData;
    protected int totalDelayTime, delayTimeBehind = 0, currentFrameIndex = 0;

    public AnimatedEmoji(String name) {
        super(name, "emoji/" + name + ".gif");
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

    public ResourceLocation getCurrentFrameResourceLocation() {
        if (framesData.size() == 1)
            return framesData.get(0).getA();

        var currentFrameData = framesData.get(currentFrameIndex);
        var currentTimeDelay = (int)(System.currentTimeMillis() / 10D % totalDelayTime);

        if (currentTimeDelay <= 1 && currentFrameIndex == framesData.size() - 1) {
            delayTimeBehind = 0;
            currentFrameIndex = 0;
        }

        if (currentTimeDelay >= (currentFrameData.getB() + delayTimeBehind)) {
            delayTimeBehind += currentFrameData.getB();
            currentFrameIndex++;
        }

        return framesData.get(currentFrameIndex).getA();
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
    public void render(float x, float y, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int light) {
        render(getCurrentFrameResourceLocation(), x, y, matrix4f, multiBufferSource, light);
    }

    @Override
    protected void load() {
        try {
            var imageData = EmojiUtil.splitGif(resourceLocation);
            width = imageData.getA().getA();
            height = imageData.getA().getB();
            totalDelayTime = imageData.getB();
            framesData = imageData.getC();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: \"" + resourceLocation.getPath() + '"', e);
        }
    }
}
