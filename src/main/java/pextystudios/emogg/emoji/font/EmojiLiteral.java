package pextystudios.emogg.emoji.font;

import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.util.EmojiUtil;

public class EmojiLiteral {
    public final static int EMOJI_DEFAULT_RENDER_SIZE = 10;
    
    private final Emoji emoji;
    private final boolean isEscaped;

    public EmojiLiteral(@NotNull Emoji emoji, boolean isEscaped) {
        this.emoji = emoji;
        this.isEscaped = isEscaped;
    }

    public EmojiLiteral(@NotNull Emoji emoji) {
        this(emoji, false);
    }

    public float render(
            float x,
            float y,
            Matrix4f matrix4f,
            MultiBufferSource multiBufferSource,
            int light
    ) {
        if (isEscaped) return 0;

        float textureSize = 16, textureX = 0, textureY = 0, textureOffset = 16 / textureSize, offsetY = 1, offsetX = 0,
                width = EMOJI_DEFAULT_RENDER_SIZE, height = EMOJI_DEFAULT_RENDER_SIZE;
        if (emoji.getWidth() < emoji.getHeight()) {
            width *= ((float) emoji.getWidth() / emoji.getHeight());
            x += (EMOJI_DEFAULT_RENDER_SIZE - width) / 2;
        }
        else if (emoji.getHeight() < emoji.getWidth()) {
            height *= ((float) emoji.getHeight() / emoji.getWidth());
            y += (EMOJI_DEFAULT_RENDER_SIZE - height) / 2;
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

        return EMOJI_DEFAULT_RENDER_SIZE;
    }

    public boolean isEscaped() {
        return isEscaped;
    }

    public String getEmojiName() {
        return emoji.getName();
    }
}
