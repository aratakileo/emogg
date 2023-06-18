package pextystudios.emogg.util;

import net.minecraft.resources.ResourceLocation;

public final class StringUtil {
    public static String lstrip(String source, char stripChar) {
        return source.replaceAll("^" + stripChar + "+", "");
    }

    public static String rstrip(String source, char stripChar) {
        return source.replaceAll(stripChar + "+$", "");
    }

    public static String strip(String source, char stripChar) {
        return source.replaceAll("^" + stripChar + "+", "")
                .replaceAll(stripChar + "+$", "");
    }

    public static String repr(ResourceLocation resourceLocation) {
        return '"' + resourceLocation.toString() + '"';
    }

    public static String repr(String value) {
        return '"' + value + '"';
    }
}
