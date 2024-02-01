package io.github.aratakileo.emogg.emoji;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MultiFrameEmojiGlyphProvider implements EmojiGlyphProvider {
    private final List<Frame> frames;
    private final int totalDuration;

    public MultiFrameEmojiGlyphProvider(List<Frame> frames) {
        this.frames = List.copyOf(frames);
        int time = 0;
        for (var frame : this.frames) {
            frame.time = time;
            time += frame.duration;
        }
        this.totalDuration = time;
    }

    @Override
    public EmojiGlyph getGlyph() {
        int time = (int) (Util.getMillis() % totalDuration);
        EmojiGlyph last = null;
        for (var frame : frames) {
            if (frame.time > time) return last;
            last = frame.glyph;
        }
        return last;
    }

    public static class Frame {
        private final EmojiGlyph glyph;
        private final int duration;
        private int time;

        public Frame(EmojiGlyph glyph, int duration) {
            this.glyph = glyph;
            this.duration = duration;
        }
    }
}
