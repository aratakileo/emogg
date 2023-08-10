package pextystudios.emogg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.HashMap;

public class AsyncResourceSlider {
    private final HashMap<Integer, SlideFrame> slides;
    private final int totalDelayTime;
    private Pair<Integer, ResourceLocation> frameBuffer = null;

    public AsyncResourceSlider(@NotNull HashMap<Integer, SlideFrame> slides, int totalDelayTime) {
        this.slides = slides;
        this.totalDelayTime = totalDelayTime;
    }

    public @NotNull ResourceLocation getCurrentFrame() {
        if (slides.size() == 1)
            return slides.get(0).resourceLocation;

        var currentPart = (int)(System.currentTimeMillis() % totalDelayTime);

        if (frameBuffer != null && frameBuffer.getA() == currentPart)
            return frameBuffer.getB();

        while (!slides.containsKey(currentPart))
            currentPart--;

        frameBuffer = new Pair<>(currentPart, slides.get(currentPart).resourceLocation);

        return frameBuffer.getB();
    }

    public static @NotNull AsyncResourceSlider from(@NotNull NativeGifImage nativeGifImage, @NotNull ResourceLocation sourceResourceLocation) {
        final var slides = new HashMap<Integer, SlideFrame>();
        final var sourceFilePath = sourceResourceLocation.getPath();
        final var sourceFileName = sourceFilePath.substring(sourceFilePath.lastIndexOf('/') + 1);

        var totalDelayTime = 0;
        var i = 0;

        for (var frame: nativeGifImage.getFrames()) {
            final var framePath = "emoji/" + sourceFileName.substring(0, sourceFileName.lastIndexOf('.')) + "_frame_" + i + ".png";
            final var frameDynamicTexture = new DynamicTexture(frame.nativeImage());

            slides.put(totalDelayTime, new SlideFrame(
                    Minecraft.getInstance().getTextureManager().register(framePath, frameDynamicTexture),
                    frame.delay()
            ));

            totalDelayTime += frame.delay();
            i++;
        }

        return new AsyncResourceSlider(slides, totalDelayTime);
    }

    public record SlideFrame(@NotNull ResourceLocation resourceLocation, int duration) {}
}
