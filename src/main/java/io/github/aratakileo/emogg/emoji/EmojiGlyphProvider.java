package io.github.aratakileo.emogg.emoji;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface EmojiGlyphProvider {
    EmojiGlyph getGlyph();
}
