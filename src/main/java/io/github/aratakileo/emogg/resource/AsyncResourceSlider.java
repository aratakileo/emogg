package io.github.aratakileo.emogg.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import io.github.aratakileo.emogg.NativeGifImage;

import java.util.HashMap;

public class AsyncResourceSlider {
    private final HashMap<Integer, SlideFrame> slides;
    private final int totalDelay;

    public AsyncResourceSlider(@NotNull HashMap<Integer, SlideFrame> slides, int totalDelay) {
        this.slides = slides;
        this.totalDelay = totalDelay;
    }

    public @NotNull ResourceLocation getCurrentFrame() {
        if (slides.size() == 1)
            return slides.get(0).resourceLocation;

        var currentPart = (int)(System.currentTimeMillis() % totalDelay);

        while (!slides.containsKey(currentPart))
            currentPart--;

        return slides.get(currentPart).resourceLocation;
    }

    public static @NotNull AsyncResourceSlider from(
            @NotNull NativeGifImage nativeGifImage,
            @NotNull ResourceLocation sourceResourceLocation
    ) {
        final var slides = new HashMap<Integer, SlideFrame>();
        final var sourceFilePath = sourceResourceLocation.getPath();
        final var sourceFileName = sourceFilePath.substring(sourceFilePath.lastIndexOf('/') + 1);

        final var totalDelay = nativeGifImage.processFrames((index, delayAmountBefore, frame) -> {
            final var framePath = "emoji/" + sourceFileName.substring(
                    0,
                    sourceFileName.lastIndexOf('.')
            ) + "_frame_" + index + ".png";

            final var frameDynamicTexture = new DynamicTexture(frame.nativeImage());

            slides.put(delayAmountBefore, new SlideFrame(
                    Minecraft.getInstance().getTextureManager().register(framePath, frameDynamicTexture),
                    frame.delay()
            ));
        });

        return new AsyncResourceSlider(slides, totalDelay);
    }

    public record SlideFrame(@NotNull ResourceLocation resourceLocation, int duration) {}
}
