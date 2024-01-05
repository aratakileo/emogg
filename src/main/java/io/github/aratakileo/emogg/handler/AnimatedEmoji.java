package io.github.aratakileo.emogg.handler;

import io.github.aratakileo.emogg.util.StringUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import io.github.aratakileo.emogg.Emogg;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AnimatedEmoji extends Emoji {
    private @Nullable AsyncResourceSlider asyncResourceSlider = null;

    protected AnimatedEmoji(
            @NotNull String name,
            @NotNull ResourceLocation resourceLocation,
            @NotNull String category
    ) {
        super(name, resourceLocation, category);
    }

    @Override
    public @Nullable ResourceLocation getRenderResourceLocation() {
        return Objects.isNull(asyncResourceSlider) ? null : asyncResourceSlider.getCurrentFrame();
    }

    @Override
    protected boolean load() {
        try {
            final var nativeGifImage = NativeGifImage.read(resourceLocation);

            width = nativeGifImage.getWidth();
            height = nativeGifImage.getHeight();

            if (nativeGifImage.getFrameCount() == 0 || width <= 0 || height <= 0)
                return false;

            asyncResourceSlider = AsyncResourceSlider.from(nativeGifImage, resourceLocation);

            return true;
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to load: " + StringUtil.repr(resourceLocation), e);
        }

        return false;
    }
}
