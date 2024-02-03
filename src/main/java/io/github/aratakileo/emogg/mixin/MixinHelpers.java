package io.github.aratakileo.emogg.mixin;

public class MixinHelpers {
    // FontMixin.preGlowOutlineRender
    // FontMixin.postGlowOutlineRender
    // StringRenderOutputMixin.noShadowAndGlowOutlineForEmojis
    public static boolean shouldSkipEmojiGlyphRender = false;
}
