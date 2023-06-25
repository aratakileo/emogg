package pextystudios.emogg.util;

import net.minecraft.resources.ResourceLocation;

public final class StringUtil {
    public static String replaceStartEndIndex(String source, int start, int end, Character newChar) {
        return source.substring(0, start) + newChar + source.substring(end);
    }

    public static String replaceStartEndIndex(String source, int start, int end, String newSubstring) {
        return source.substring(0, start) + newSubstring + source.substring(end);
    }

    public static String repr(ResourceLocation resourceLocation) {
        return '"' + resourceLocation.toString() + '"';
    }

    public static String repr(String value) {
        return '"' + value + '"';
    }
}
