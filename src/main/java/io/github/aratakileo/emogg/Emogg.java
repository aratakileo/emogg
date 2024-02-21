package io.github.aratakileo.emogg;

import io.github.aratakileo.elegantia.gui.config.Config;
import io.github.aratakileo.elegantia.updatechecker.ModrinthUpdateChecker;
import io.github.aratakileo.elegantia.updatechecker.SuccessfulResponse;
import io.github.aratakileo.elegantia.util.ModInfo;
import io.github.aratakileo.elegantia.util.Platform;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.gui.EmojiSuggestion;
import io.github.aratakileo.suggestionsapi.SuggestionsAPI;
import io.github.aratakileo.suggestionsapi.injector.Injector;
import io.github.aratakileo.suggestionsapi.util.Cast;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

        LOGGER.info("[emogg] Installed v%s; %s to download (from modrinth.com)%s".formatted(
                ModInfo.getVersion(NAMESPACE_OR_ID).orElse("unknown").split("\\+")[0],
                switch (lastResponse.getResponseCode()) {
                    case SUCCESSFUL, NEW_VERSION_IS_AVAILABLE -> "available";
                    default -> "not available";
                },
                switch (lastResponse.getResponseCode()) {
                    case DOES_NOT_EXIST_AT_MODRINTH -> ", because does not exist for Minecraft v"
                            + Platform.getCurrentMinecraftVersion();
                    case SUCCESSFUL, NEW_VERSION_IS_AVAILABLE -> " v%s - %s".formatted(
                            UPDATE_CHECKER.getLastResponseAsSuccessful()
                                    .map(SuccessfulResponse::getDefinedVersion)
                                    .orElse("unknown"),
                            lastResponse.isNewVersionAvailable() ? "needs to be updated" : "not needs to be updated"
                    );
                    default -> ", because something went wrong";
                }
        ));

        if (lastResponse.doesNotExistAtModrinth())
            LOGGER.warn("[emogg] It looks like you are using an unofficial version port!");

        EmoggConfig.instance = Config.init(EmoggConfig.class, NAMESPACE_OR_ID);
        EmojiManager.init();

        registerBuiltinResourcePack("builtin");
        registerBuiltinResourcePack("twemogg");
    }

    private void registerBuiltinResourcePack(@NotNull String resourcepackName) {
        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(NAMESPACE_OR_ID, resourcepackName),
                FabricLoader.getInstance().getModContainer(NAMESPACE_OR_ID).orElseThrow(),
                Component.translatable(String.format("emogg.resourcepack.%s.name", resourcepackName)),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
    }
}
