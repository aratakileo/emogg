package pextystudios.emogg.api;

import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pextystudios.emogg.Emogg;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModrinthUpdateChecker {
    private final static String REQUEST_URL = "https://api.modrinth.com/v2/project/{identifier}/version?game_versions=%5B%22{minecraft_version}%22%5D";
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
        if (!isRequested) {
            final var request = HttpRequest.newBuilder(URI.create(getRequestUrl(Emogg.NAMESPACE_OR_ID))).setHeader("User-Agent", "github.com/aratakileo/" + Emogg.NAMESPACE_OR_ID + '/' + Emogg.getVersion() + " (aratakileo@gmail.com)").build();

            try {
                final var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                updateVersion = JsonParser
                        .parseString(response.body())
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("version_number")
                        .getAsString();
            } catch (Exception e) {
                Emogg.LOGGER.error("Failed to check updates: ", e);
            }

            needsToBeUpdated = isVersionGreaterThanCurrent(updateVersion);
            isRequested = true;
        }
    }

    private static boolean isVersionGreaterThanCurrent(@Nullable String version) {
        if (version == null)
            return false;

        final var currentVersionComparisonPart = Emogg.getVersion().split("-")[1];
        final var comparisonPart = version.split("-")[1];

        return Integer.parseInt(comparisonPart.replaceAll("\\D+", "")) > Integer.parseInt(currentVersionComparisonPart.replaceAll("\\D+", ""));
    }

    private static String getRequestUrl(@NotNull String identifier) {
        return REQUEST_URL.replace("{identifier}", identifier).replace("{minecraft_version}", SharedConstants.getCurrentVersion().getName());
    }
}
