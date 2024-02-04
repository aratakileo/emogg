package io.github.aratakileo.emogg.emoji;

import io.github.aratakileo.emogg.util.ClientEnvironment;

@ClientEnvironment
@FunctionalInterface
public interface EmojiGlyphProvider {
    EmojiGlyph getGlyph();
}
