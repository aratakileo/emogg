package pextystudios.emogg.api;

import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;
import pextystudios.emogg.Emogg;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ModrinthApi {
    private final static String REQUEST_URL = "https://api.modrinth.com/v2/project/{identifier}/version?" +
            "game_versions=%5B%22{minecraft_version}%22%5D";
    private final static HttpClient client = HttpClient.newHttpClient();
    private static boolean needsToBeUpdated = false;
    private static boolean isRequested = false;
    private static String updateVersion;

    public static boolean needsToBeUpdated() {
        checkUpdates();

        return needsToBeUpdated;
    }

    public static @Nullable String getUpdateVersion() {
        checkUpdates();

        return updateVersion;
    }

    private static void checkUpdates() {
        if (isRequested) return;

        final var request = HttpRequest.newBuilder(URI.create(getRequestUrl()))
                .setHeader(
                        "User-Agent",
                        "github.com/aratakileo/"
                                + Emogg.NAMESPACE_OR_ID
                                + '/' + Emogg.getVersion()
                                + " (aratakileo@gmail.com)"
                )
                .build();

        try {
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            updateVersion = JsonParser
                    .parseString(response.body())
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject()
                    .get("version_number")
                    .getAsString();

            needsToBeUpdated = isVersionGreaterThanCurrent(updateVersion);
            isRequested = true;
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to check updates: ", e);
        }
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

        for (var i = 0; i < 3; i++)
            if (currentVerFirstMetadata.get(i) < otherVerFirstMetadata.get(i))
                return true;

        return false;
    }

    /*
    *
    * Supports version format that starts with: `x.x-BETA.x` or `x.x.x`, where x is digit or number
    *
    */
    private static Pair<String[], Integer> getVersionMetadata(@NotNull String version) {
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
}
