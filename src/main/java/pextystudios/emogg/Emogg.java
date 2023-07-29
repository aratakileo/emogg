package pextystudios.emogg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.EmojiHandler;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    @Override
    public void onInitializeClient() {
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
}
