package io.github.aratakileo.emogg.util;

import io.github.aratakileo.emogg.emoji.EmojiGlyph;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import io.github.aratakileo.emogg.emoji.Emoji;

public final class EmojiUtil {
    public final static String PNG_EXTENSION = ".png",
            EMOJI_FOLDER_NAME = "emoji";

    public static void render(@NotNull EmojiGlyph emojiGlyph, @NotNull GuiGraphics guiGraphics, int x, int y, int size) {
        var builder = guiGraphics.bufferSource().getBuffer(
                emojiGlyph.renderType(Font.DisplayMode.NORMAL)
        );

        var scale = size / Math.max(emojiGlyph.getAdvance(), EmojiGlyph.HEIGHT);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0f);
        guiGraphics.pose().scale(scale, scale, 0f);
        emojiGlyph.render(
                false,
                0f, 0f,
                guiGraphics.pose().last().pose(),
                builder,
                1f, 1f, 1f, 1f,
                LightTexture.FULL_BRIGHT
        );
        guiGraphics.pose().popPose();
    }

    public static void render(@NotNull Emoji emoji, @NotNull GuiGraphics guiGraphics, int x, int y, int size) {
        render(emoji.getGlyph(), guiGraphics, x, y, size);
    }

    public static @NotNull String normalizeNameOrCategory(@NotNull String sourceValue) {
        return StringUtils.strip(
                sourceValue.toLowerCase()
                        .replaceAll("-+| +|\\.+", "_")
                        .replaceAll("[^a-z0-9_]", ""),
                "_"
        );
    }

    public static @NotNull String getNameFromPath(@NotNull ResourceLocation resourceLocation) {
        return getNameFromPath(resourceLocation.toString());
    }

    public static @NotNull String getNameFromPath(@NotNull String path) {
        return path.transform(name -> name.substring(name.lastIndexOf('/') + 1))
                .transform(name -> name.substring(0, name.lastIndexOf('.')));
    }
}
