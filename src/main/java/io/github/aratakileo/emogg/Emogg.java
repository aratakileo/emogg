package io.github.aratakileo.emogg;

import io.github.aratakileo.emogg.api.ModrinthApi;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
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
        ModrinthApi.checkUpdates();

        LOGGER.info(String.format(
                "[emogg] Installed v%s; %s to download (from modrinth.com)%s",
                getVersion(),
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
            LOGGER.warn("[emogg] It looks like you are using an unofficial version port!");

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
