package pextystudios.emogg;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class NativeGifImage {
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

    public int[] getDelays() {
        return delays;
    }

    public int getFrameCount() {
        return delays.length;
    }

    public NativeImage getVerticalScroll() {
        final var nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height * frames, false);

        for (var y = 0; y < height * frames; y++)
            for (var x = 0; x < width; x++)
                nativeImage.setPixelRGBA(x, y, pixels[y * width + x]);

        return nativeImage;
    }

    public List<Frame> getFrames() {
        final var slicedFrames = new ArrayList<Frame>();

        for (var i = 0; i < getFrameCount(); i++) {
            final var currentNativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);

            for (var x = 0; x < width; x++)
                for (var y = 0; y < height; y++)
                    currentNativeImage.setPixelRGBA(x, y, pixels[(i * height + y) * width + x]);

            slicedFrames.add(new Frame(currentNativeImage, delays[i]));
        }

        return slicedFrames;
    }

    public static NativeGifImage read(ResourceLocation resourceLocation) throws IOException {
        final var optionalResource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);

        if (optionalResource.isEmpty())
            throw new IOException("The resource cannot be found: " + resourceLocation.getPath());

        return read(optionalResource.get().open());
    }

    public static NativeGifImage read(InputStream inputStream) throws IOException {
        NativeGifImage nativeGifImage;

        try {
            nativeGifImage = read(inputStream.readAllBytes());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return nativeGifImage;
    }

    public static NativeGifImage read(byte[] fileData) throws IOException {
        final ByteBuffer byteBuffer = MemoryUtil.memAlloc(fileData.length);
        NativeGifImage nativeGifImage;

        try {
            byteBuffer.put(fileData);
            byteBuffer.position(0);

            try (final MemoryStack memoryStack = MemoryStack.stackPush()) {
                final PointerBuffer delayBuffer = memoryStack.mallocPointer(1);

                final IntBuffer widthBuffer = memoryStack.mallocInt(1),
                        heightBuffer = memoryStack.mallocInt(1),
                        frameBuffer = memoryStack.mallocInt(1),
                        channelBuffer = memoryStack.mallocInt(1);

                ByteBuffer gifByteBuffer = STBImage.stbi_load_gif_from_memory(
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

                    int channels = channelBuffer.get();

                    if (channels != 4)
                        throw new RuntimeException(String.format("Could not load gif image with less than 4 channels (%s)", channels));

                    int width = widthBuffer.get(), height = heightBuffer.get(), frames = frameBuffer.get();

                    IntBuffer delaysIntBuffer = delayBuffer.getIntBuffer(frames);

                    int[] delays = new int[frames];
                    delaysIntBuffer.get(delays);

                    IntBuffer pixelData = gifByteBuffer.asIntBuffer();
                    int[] pixels = new int[width * height * frames];
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

    public record Frame(NativeImage nativeImage, int delay) {}
}
