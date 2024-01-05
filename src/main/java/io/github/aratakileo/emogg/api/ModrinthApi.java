package io.github.aratakileo.emogg.api;

import com.google.gson.JsonParser;
import io.github.aratakileo.emogg.Emogg;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ModrinthApi {
    private final static String REQUEST_URL = "https://api.modrinth.com/v2/project/{identifier}/version?" +
            "game_versions=%5B%22{minecraft_version}%22%5D";
    private final static HttpClient client = HttpClient.newHttpClient();
    private static @NotNull ResponseCode responseCode = ResponseCode.NO_RESPONSE;
    private static @Nullable String updateVersion;

    public static @NotNull ResponseCode getResponseCode() {
        return responseCode;
    }

    public static @Nullable String getUpdateVersion() {
        checkUpdates();

        return updateVersion;
    }

    public static void checkUpdates() {
        if (responseCode != ResponseCode.NO_RESPONSE) return;

        final var request = HttpRequest.newBuilder(URI.create(getRequestUrl()))
                .setHeader(
                        "User-Agent",
                        "github.com/aratakileo/"
                                + Emogg.NAMESPACE_OR_ID
                                + '@' + Emogg.getVersion()
                                + " (aratakileo@gmail.com)"
                )
                .build();

        try {
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            final var versionMetadatas = JsonParser
                    .parseString(response.body())
                    .getAsJsonArray();

            if (versionMetadatas.isEmpty()) responseCode = ResponseCode.DOES_NOT_EXIST_AT_MODRINTH;
            else {
                updateVersion = versionMetadatas.get(0)
                        .getAsJsonObject()
                        .get("version_number")
                        .getAsString();

                responseCode = isVersionGreaterThanCurrent(
                        updateVersion
                ) ? ResponseCode.NEEDS_TO_BE_UPDATED : ResponseCode.SUCCESSFUL;
            }
        } catch (Exception e) {
            responseCode = ResponseCode.FAILED;
            Emogg.LOGGER.error("Failed to check updates: ", e);
        }
    }

    public static @NotNull String getLinkForUpdate() {
        return "https://modrinth.com/mod/emogg/versions?g=" + SharedConstants.getCurrentVersion().getName();
    }

    private static boolean isVersionGreaterThanCurrent(@Nullable String version) {
        if (version == null) return false;

        final var currentVerMetadata = getVersionMetadata(Emogg.getVersion());
        final var otherVerMetadata = getVersionMetadata(version);
        final var currentVerSecondDigit = currentVerMetadata.getB();
        final var otherVerSecondDigit = otherVerMetadata.getB();

        if (currentVerSecondDigit != -1)
            return otherVerSecondDigit == -1 || otherVerSecondDigit > currentVerSecondDigit;

        final var currentVerFirstMetadata = Arrays.stream(currentVerMetadata.getA()).map(Integer::parseInt).toList();
        final var otherVerFirstMetadata = Arrays.stream(otherVerMetadata.getA()).map(Integer::parseInt).toList();

        if (currentVerFirstMetadata.size() < otherVerFirstMetadata.size())
            return true;

        if (currentVerFirstMetadata.size() > otherVerFirstMetadata.size())
            return false;

        for (var i = 0; i < currentVerFirstMetadata.size(); i++)
            if (currentVerFirstMetadata.get(i) < otherVerFirstMetadata.get(i))
                return true;

        return false;
    }

    /*
     *
     * Supports version format that starts with: `x.x-BETA.x` or `x.x.x`, with `-` or `+` and Minecraft verison name, or without it, where x is digit or number
     *
     */
    private static @NotNull Pair<String[], Integer> getVersionMetadata(@NotNull String version) {
        if (version.contains("+"))
            version = version.split("\\+")[0];

        final var basicVersionSegments = version.split("-");

        var metadataSecondDigit = -1;

        if (basicVersionSegments.length >= 2 && basicVersionSegments[1].startsWith("BETA"))
            metadataSecondDigit = Integer.parseInt(basicVersionSegments[1].split("\\.")[1]);

        return new Pair<>(basicVersionSegments[0].split("\\."), metadataSecondDigit);
    }

    private static String getRequestUrl() {
        return REQUEST_URL.replace("{identifier}", Emogg.NAMESPACE_OR_ID)
                .replace("{minecraft_version}", SharedConstants.getCurrentVersion().getName());
    }

    public enum ResponseCode {
        NO_RESPONSE,
        SUCCESSFUL,
        NEEDS_TO_BE_UPDATED,
        FAILED,
        DOES_NOT_EXIST_AT_MODRINTH
    }
}
