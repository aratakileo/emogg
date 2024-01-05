package io.github.aratakileo.emogg.util;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class StringUtil {
    public static @NotNull String replaceStartEndIndex(
            @NotNull String source,
            int start,
            int end,
            @NotNull Character newChar
    ) {
        return source.substring(0, start) + newChar + source.substring(end);
    }

    public static @NotNull String replaceStartEndIndex(
            @NotNull String source,
            int start,
            int end,
            @NotNull String newSubstring
    ) {
        return source.substring(0, start) + newSubstring + source.substring(end);
    }

    public static @NotNull String repr(@NotNull ResourceLocation resourceLocation) {
        return repr(resourceLocation.toString());
    }

    public static @NotNull String repr(@NotNull String value) {
        return new Gson().toJson(value);
    }

    public static @NotNull String repr(char value) {
        final var output = new Gson().toJson(value);

        return "'" + output.substring(1, output.length() - 1) + "'";
    }

    public static @NotNull String repr(int value) {
        return new Gson().toJson(value);
    }

    public static @NotNull String repr(float value) {
        return new Gson().toJson(value);
    }

    public static @NotNull String repr(double value) {
        return new Gson().toJson(value);
    }

    public static @NotNull String repr(boolean value) {
        return String.valueOf(value);
    }

    public static @NotNull String repr(@NotNull Object value) {
        return new Gson().toJson(value);
    }
}
