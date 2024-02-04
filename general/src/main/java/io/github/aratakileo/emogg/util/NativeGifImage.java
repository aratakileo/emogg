package io.github.aratakileo.emogg.util;

import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;

@ClientEnvironment
public class NativeGifImage implements AutoCloseable {
    public final static String GIF_EXTENSION = ".gif";

    private final int width;
    private final int height;
    private final int frames;
    private final int[] pixels;
    private final int[] delays;

    public NativeGifImage(int width, int height, int frames, int[] pixels, int[] delays) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.pixels = pixels;
        this.delays = delays;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFrameCount() {
        return delays.length;
    }

    public @NotNull NativeImage getVerticalScroll() {
        final var nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height * frames, false);

        for (var y = 0; y < height * frames; y++)
            for (var x = 0; x < width; x++)
                nativeImage.setPixelRGBA(x, y, pixels[y * width + x]);

        return nativeImage;
    }

    public int processFrames(@NotNull FrameProcessor frameProcessor) {
        var totalDelay = 0;

        for (var i = 0; i < getFrameCount(); i++) {
            final var currentNativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

            for (var x = 0; x < width; x++)
                for (var y = 0; y < height; y++)
                    currentNativeImage.setPixelRGBA(x, y, pixels[(i * height + y) * width + x]);

            frameProcessor.process(i, totalDelay, new Frame(currentNativeImage, delays[i]));

            totalDelay += delays[i];
        }

        return totalDelay;
    }

    public static @NotNull NativeGifImage read(@NotNull InputStream inputStream) throws IOException {
        var nativeGifImage = (NativeGifImage)null;

        try {
            nativeGifImage = read(inputStream.readAllBytes());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return nativeGifImage;
    }

    public static @NotNull NativeGifImage read(byte[] fileData) throws IOException {
        final var byteBuffer = MemoryUtil.memAlloc(fileData.length);
        final NativeGifImage nativeGifImage;

        try {
            byteBuffer.put(fileData);
            byteBuffer.position(0);

            try (final MemoryStack memoryStack = MemoryStack.stackPush()) {
                final var delayBuffer = memoryStack.mallocPointer(1);
                final var widthBuffer = memoryStack.mallocInt(1);
                final var heightBuffer = memoryStack.mallocInt(1);
                final var frameBuffer = memoryStack.mallocInt(1);
                final var channelBuffer = memoryStack.mallocInt(1);
                final var gifByteBuffer = STBImage.stbi_load_gif_from_memory(
                        byteBuffer,
                        delayBuffer,
                        widthBuffer,
                        heightBuffer,
                        frameBuffer,
                        channelBuffer,
                        0
                );

                try {
                    if (gifByteBuffer == null)
                        throw new IOException("Could not load gif image: " + STBImage.stbi_failure_reason());

                    final var channels = channelBuffer.get();

                    if (channels != 4)
                        throw new RuntimeException(String.format(
                                "Could not load gif image with less than 4 channels (%s)",
                                channels
                        ));

                    final var width = widthBuffer.get();
                    final var height = heightBuffer.get();
                    final var frames = frameBuffer.get();
                    final var delaysIntBuffer = delayBuffer.getIntBuffer(frames);
                    final var pixelData = gifByteBuffer.asIntBuffer();
                    final var delays = new int[frames];
                    final var pixels = new int[width * height * frames];

                    delaysIntBuffer.get(delays);
                    pixelData.get(pixels);

                    nativeGifImage = new NativeGifImage(width, height, frames, pixels, delays);
                } finally {
                    if (gifByteBuffer != null) STBImage.stbi_image_free(gifByteBuffer);
                }
            }
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }

        return nativeGifImage;
    }

    @Override
    public void close() {
        // Handled by GC
    }

    public interface FrameProcessor {
        void process(int index, int delayAmountBefore, @NotNull Frame frame);
    }

    public record Frame(@NotNull NativeImage nativeImage, int delay) {}
}
