package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import io.github.aratakileo.emogg.Emogg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EmojiFontSet extends FontSet {
    public static final ResourceLocation NAME = new ResourceLocation(Emogg.NAMESPACE_OR_ID, "emoji");

    // Don't uncomment :)
//    public static final ResourceLocation NAME = new ResourceLocation("default");

    private static EmojiFontSet instance = null;

    public EmojiFontSet(TextureManager textureManager) {
        super(textureManager, NAME);
        EmojiFontSet.instance = this;
    }

    public static int codePointToId(int codePoint) {
        return codePoint - 33;
    }
    public static int idToCodePoint(int id) {
        return id + 33;
    }

    @Override
    public @NotNull GlyphInfo getGlyphInfo(int iChar, boolean bl) {
        return getGlyph(iChar);
    }

    @Override
    public @NotNull EmojiGlyph getGlyph(int iChar) {
        var emoji = EmojiManager.getInstance().getEmoji(codePointToId(iChar));
        if (emoji == null) return EmojiGlyph.ERROR;
        return emoji.getGlyph();
    }

    @Override
    public @NotNull BakedGlyph whiteGlyph() {
        return EmojiGlyph.EMPTY;
    }

    @Override
    public @NotNull BakedGlyph getRandomGlyph(GlyphInfo glyphInfo) {
        return EmojiManager.getInstance()
                .getRandomEmoji()
                .map(Emoji::getGlyph)
                .orElse(EmojiGlyph.ERROR);
    }

    @Override
    public void reload(List<GlyphProvider> list) { }

    @Override
    public void close() { }

    public static EmojiFontSet getInstance() {
        return EmojiFontSet.instance;
    }
}
