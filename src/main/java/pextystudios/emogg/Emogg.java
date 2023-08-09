package pextystudios.emogg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.handler.EmojiHandler;

import java.util.Objects;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    public static boolean hasMessageAboutUpdateBeenShown = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info(String.format(
                "emogg client v%s, emogg modrinth v%s - %s",
                getVersion(),
                Objects.requireNonNullElse(ModrinthUpdateChecker.getUpdateVersion(), "-unknown"),
                ModrinthUpdateChecker.needsToBeUpdated() ? "needs to be updated": "not needs to be updated"
        ));

        new EmojiHandler();

        EmoggConfig.load();

        registerBuiltinResourcePack("builtin");
        registerBuiltinResourcePack("twemogg");
    }

    private void registerBuiltinResourcePack(String resourcepackName) {
        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(NAMESPACE, resourcepackName),
                FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow(),
                Component.translatable(String.format("emogg.resourcepack.%s.name", resourcepackName)),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
    }

    public static String getVersion() {
        return FabricLoader.getInstance().getModContainer(NAMESPACE).get().getMetadata().getVersion().getFriendlyString();
    }
}
