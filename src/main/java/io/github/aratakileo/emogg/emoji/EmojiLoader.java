package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.util.NativeGifImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.*;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface EmojiLoader {
    Future<? extends EmojiGlyphProvider> load();

    static CompletableFuture<InputStream> resourceReader(ResourceLocation location) {
        return CompletableFuture.supplyAsync(() -> {

            var resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(location)
                    .orElseThrow(() -> new EmojiLoadingException("Resource not found: " + location));
            InputStream inputStream = null;
            try {
                inputStream = resource.open();
                return inputStream;
            } catch (IOException e) {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException ee) {
                    Emogg.LOGGER.warn("Failed to close resource: "+location, ee);
                }
                throw new EmojiLoadingException("Failed to open resource: "+location, e);
            }
        }, Util.ioPool());
    }

    static CompletableFuture<InputStream> downloader(URL url) {
        return null; // TODO
    }

    static CompletableFuture<InputStream> fileReader(File file) {
        return null; // TODO
    }

    static CompletableFuture<EmojiGlyphProvider> staticImageLoader(CompletableFuture<InputStream> loader) {
        return loader
                .thenApply(inputStream -> {
                    NativeImage image = null;
                    try (inputStream) {
                        image = NativeImage.read(inputStream);
                        if (image.getWidth() <= 0 || image.getHeight() <= 0)
                            throw new EmojiLoadingException("Invalid image!");
                        return image;
                    } catch (IOException e) {
                        if (image != null) image.close();
                        throw new EmojiLoadingException("Failed to load static image.", e);
                    }
                })
                .thenApplyAsync(image -> {
                    var glyph = EmojiAtlas.stitch(image);
                    image.close();
                    return () -> glyph;
                }, Minecraft.getInstance());
    }

    static CompletableFuture<MultiFrameEmojiGlyphProvider> gifLoader(CompletableFuture<InputStream> loader) {
        return loader
                .thenApply(inputStream -> {
                    NativeGifImage gif = null;
                    try (inputStream) {
                        gif = NativeGifImage.read(inputStream);
                        if (gif.getFrameCount() <= 0 || gif.getWidth() <= 0 || gif.getHeight() <= 0)
                            throw new EmojiLoadingException("Invalid GIF image!");
                        return gif;
                    } catch (IOException e) {
                        if (gif != null) gif.close();
                        throw new EmojiLoadingException("Failed to load GIF image.", e);
                    }
                })
                .thenApplyAsync(gif -> {
                    var frames = new ArrayList<MultiFrameEmojiGlyphProvider.Frame>();
                    gif.processFrames((index, time, frame) -> {
                        frames.add(new MultiFrameEmojiGlyphProvider.Frame(
                                EmojiAtlas.stitch(frame.nativeImage()), frame.delay()
                        ));
                        frame.nativeImage().close();
                    });
                    return new MultiFrameEmojiGlyphProvider(frames);
                }, Minecraft.getInstance());
    }


    class EmojiLoadingException extends RuntimeException {
        public EmojiLoadingException(String message) {
            super(message);
        }

        public EmojiLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
