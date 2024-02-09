package io.github.aratakileo.emogg.mixin;

import java.util.regex.Pattern;

public class MixinHelpers {
    // FontMixin.preGlowOutlineRender
    // FontMixin.postGlowOutlineRender
    // StringRenderOutputMixin.noShadowAndGlowOutlineForEmojis
    public static boolean shouldSkipEmojiGlyphRender = false, hasMessageAboutUpdateBeenShown = false;

    // ResourceLocationMixin
    public static final Pattern PATTERN_NAMESPACE = Pattern.compile("/([a-z0-9_.-]+):");
}
