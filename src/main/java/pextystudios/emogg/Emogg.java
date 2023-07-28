package pextystudios.emogg;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    @Override
    public void onInitializeClient() {
        new EmojiHandler();

        EmoggConfig.load();

        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(NAMESPACE, "builtin_emojis"),
                FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow(),
                ResourcePackActivationType.DEFAULT_ENABLED
        );
    }
}
