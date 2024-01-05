package io.github.aratakileo.emogg.gui;

import io.github.aratakileo.emogg.handler.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.regex.Pattern;

public class EmojiLiteral {
    public final static Pattern EMOJI_CODE_PATTERN = Pattern.compile("(:([_A-Za-z0-9]+):)"),
            EMOJI_LITERAL_PATTERN = Pattern.compile("(\\\\?)" + EMOJI_CODE_PATTERN.pattern());

    public final static int DEFAULT_RENDER_SIZE = 8;

    public final static char DUMMY_CHAR = '\u2603';

    private final Emoji emoji;
    private final boolean isEscaped;

    public EmojiLiteral(@NotNull Emoji emoji, boolean isEscaped) {
        this.emoji = emoji;
        this.isEscaped = isEscaped;
    }

    public float render(
            float x,
            float y,
            @NotNull Matrix4f matrix4f,
            @NotNull MultiBufferSource multiBufferSource,
            int light
    ) {
        if (Objects.isNull(emoji.getRenderResourceLocation())) return 0;

        float textureSize = 16, textureX = 0, textureY = 0, textureOffset = 16 / textureSize, offsetY = 1, offsetX = 0,
                width = DEFAULT_RENDER_SIZE, height = DEFAULT_RENDER_SIZE;
        if (emoji.getWidth() < emoji.getHeight()) {
            width *= ((float) emoji.getWidth() / emoji.getHeight());
            x += (DEFAULT_RENDER_SIZE - width) / 2;
        }
        else if (emoji.getHeight() < emoji.getWidth()) {
            height *= ((float) emoji.getHeight() / emoji.getWidth());
            y += (DEFAULT_RENDER_SIZE - height) / 2;
        }

        var buffer = multiBufferSource.getBuffer(EmojiUtil.getRenderType(emoji.getRenderResourceLocation()));

        buffer.vertex(matrix4f, x - offsetX, y - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(textureX, textureY)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX, y + height - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX, textureY + textureOffset)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX + width, y + height - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX + textureOffset, textureY + textureOffset)
                .uv2(light)
                .endVertex();
        buffer.vertex(matrix4f, x - offsetX + width, y - offsetY, 0.0F)
                .color(255, 255, 255, 255)
                .uv(textureX + textureOffset, textureY / textureSize)
                .uv2(light)
                .endVertex();

        return DEFAULT_RENDER_SIZE;
    }

    public @NotNull Emoji getEmoji() {
        return emoji;
    }

    public boolean isEscaped() {
        return isEscaped;
    }
}
