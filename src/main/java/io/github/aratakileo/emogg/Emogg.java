package io.github.aratakileo.emogg;

import io.github.aratakileo.elegantia.gui.config.Config;
import io.github.aratakileo.elegantia.updatechecker.ModrinthUpdateChecker;
import io.github.aratakileo.elegantia.util.ModInfo;
import io.github.aratakileo.elegantia.util.Platform;
import io.github.aratakileo.elegantia.util.ResourcePacksProvider;
import io.github.aratakileo.elegantia.util.Versions;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.gui.EmojiSuggestion;
import io.github.aratakileo.suggestionsapi.SuggestionsAPI;
import io.github.aratakileo.suggestionsapi.injector.Injector;
import io.github.aratakileo.suggestionsapi.util.Cast;
import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Emogg implements ClientModInitializer {
    public final static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);
    public final static String NAMESPACE_OR_ID = "emogg";
    public final static ModrinthUpdateChecker UPDATE_CHECKER = new ModrinthUpdateChecker(NAMESPACE_OR_ID);

    @Override
    public void onInitializeClient() {
        final var lastResponse = UPDATE_CHECKER.check();
        final var modInfo = ModInfo.get(NAMESPACE_OR_ID).orElseThrow();

        SuggestionsAPI.registerInjector(Injector.simple(
                Pattern.compile("[:：][A-Za-z0-9_]*[:：]?$"),
                (currentExpression, startOffset) -> {
                    List<EmojiSuggestion> suggestions = new LinkedList<>();
                    EmojiManager.getInstance().getEmojisStream().forEach(emoji -> {
                        suggestions.add(new EmojiSuggestion(emoji, ":%s:".formatted(emoji.getName())));
                        suggestions.add(new EmojiSuggestion(emoji, "：%s：".formatted(emoji.getName())));
                    });
                   return Cast.of(suggestions);
                }
        ));

        LOGGER.info(
                "emogg kernel platform: {}, loading platform: {}",
                modInfo.getKernelPlatform(),
                Platform.getCurrent()
        );

        LOGGER.info(
                "[emogg] Installed v{}; {} to download (from modrinth.com){}",
                Versions.getVersionKernel(modInfo.getVersion()).orElse("-invalid"),
                switch (lastResponse.responseCode) {
                    case SUCCESSFUL, NEW_VERSION_IS_AVAILABLE -> "available";
                    default -> "not available";
                },
                switch (lastResponse.responseCode) {
                    case NO_VERSIONS_FOUND -> ", because does not exist for Minecraft v"
                            + Platform.getMinecraftVersion();
                    case SUCCESSFUL, NEW_VERSION_IS_AVAILABLE -> " v%s - %s".formatted(
                            UPDATE_CHECKER.getLastResponseAsSuccessful()
                                    .flatMap(response -> Versions.getVersionKernel(response.getDefinedVersion()))
                                    .orElse("-unknown"),
                            lastResponse.isNewVersionAvailable() ? "needs to be updated" : "not needs to be updated"
                    );
                    case DOES_NOT_EXIST_AT_MODRINTH -> ", because no longer available on modrinth";
                    default -> ", because something went wrong";
                }
        );

        if (lastResponse.isNoVersionsFound())
            LOGGER.warn("[emogg] It looks like you are using an unofficial version port!");

        EmoggConfig.instance = Config.init(EmoggConfig.class, NAMESPACE_OR_ID);
        EmojiManager.init();

        registerBuiltinResourcePack("builtin");
        registerBuiltinResourcePack("twemogg");
    }

    private void registerBuiltinResourcePack(@NotNull String name) {
        ResourcePacksProvider.defineBuiltin(NAMESPACE_OR_ID, name).setAutoCompatibilityCompliance(true).register();
    }
}
