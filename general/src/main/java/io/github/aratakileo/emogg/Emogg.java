package io.github.aratakileo.emogg;

import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.gui.EmojiSuggestion;
import io.github.aratakileo.emogg.gui.screen.SettingsScreen;
import io.github.aratakileo.emogg.util.ClientEnvironment;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.Platform;
import io.github.aratakileo.suggestionsapi.SuggestionsAPI;
import io.github.aratakileo.suggestionsapi.injector.Injector;
import io.github.aratakileo.suggestionsapi.util.Cast;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

@ClientEnvironment
public final class Emogg {

    public static final Logger LOGGER = LoggerFactory.getLogger(Emogg.class);
    public static final String NAMESPACE_OR_ID = "emogg";

    private static Platform platform;

    public static void init(@NotNull Platform platform) {
        Emogg.platform = platform;

        ModrinthApi.checkUpdates();
        EmoggConfig.load();

        SuggestionsAPI.registerInjector(Injector.simple(
                Pattern.compile(":[A-Za-z0-9_]*(:)?$"),
                (currentExpression, startOffset) -> Cast.of(
                        EmojiManager.getInstance()
                                .getEmojisStream()
                                .map(EmojiSuggestion::new)
                                .toList()
                )
        ));

        LOGGER.info(String.format(
                "[emogg] Installed v%s; %s to download (from modrinth.com)%s",
                platform.getModVersion(NAMESPACE_OR_ID),
                switch (ModrinthApi.getResponseCode()) {
                    case SUCCESSFUL, NEEDS_TO_BE_UPDATED -> "available";
                    default -> "not available";
                },
                switch (ModrinthApi.getResponseCode()) {
                    case DOES_NOT_EXIST_AT_MODRINTH -> ", because does not exist for Minecraft v"
                            + SharedConstants.getCurrentVersion().getName();
                    case SUCCESSFUL, NEEDS_TO_BE_UPDATED -> String.format(
                            " v%s - %s",
                            ModrinthApi.getUpdateVersion(),
                            ModrinthApi.getResponseCode() == ModrinthApi.ResponseCode.NEEDS_TO_BE_UPDATED
                                    ? "needs to be updated" : "not needs to be updated"
                    );
                    default -> ", because something went wrong";
                }
        ));

        if (ModrinthApi.getResponseCode() == ModrinthApi.ResponseCode.DOES_NOT_EXIST_AT_MODRINTH)
            LOGGER.warn(
                    "[emogg] It looks like you are using an unofficial version port! "
                            + "Please contact us: https://github.com/aratakileo/emogg"
            );

        platform.registerResourcesReloadListener(
                new ResourceLocation(Emogg.NAMESPACE_OR_ID, EmojiUtil.EMOJI_FOLDER_NAME),
                EmojiManager::init
        );
        platform.registerBuiltinResourcePack(Emogg.NAMESPACE_OR_ID, "builtin");
        platform.registerBuiltinResourcePack(Emogg.NAMESPACE_OR_ID, "twemogg");
        platform.setConfigScreenRegistrator(SettingsScreen::new);
    }

    public static @Nullable Platform getPlatform() {
        return platform;
    }
}
