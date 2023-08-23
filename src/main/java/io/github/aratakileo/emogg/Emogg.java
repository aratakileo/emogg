package io.github.aratakileo.emogg;

import io.github.aratakileo.emogg.api.ModrinthApi;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE_OR_ID = "emogg";

    public static boolean hasMessageAboutUpdateBeenShown = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info(String.format(
                "[emogg] installed v%s, available to download (from modrinth.com) v%s - %s",
                getVersion(),
                Objects.requireNonNullElse(ModrinthApi.getUpdateVersion(), "-unknown"),
                ModrinthApi.needsToBeUpdated() ? "needs to be updated": "not needs to be updated"
        ));

        EmojiHandler.init();
        EmoggConfig.load();

        registerBuiltinResourcePack("builtin");
        registerBuiltinResourcePack("twemogg");
    }

    private void registerBuiltinResourcePack(String resourcepackName) {
        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(NAMESPACE_OR_ID, resourcepackName),
                FabricLoader.getInstance().getModContainer(NAMESPACE_OR_ID).orElseThrow(),
                Component.translatable(String.format("emogg.resourcepack.%s.name", resourcepackName)),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
    }

    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(NAMESPACE_OR_ID)
                .get()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}
