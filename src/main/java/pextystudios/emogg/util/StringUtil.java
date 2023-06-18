package pextystudios.emogg.util;

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
}
