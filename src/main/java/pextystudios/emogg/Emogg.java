package pextystudios.emogg;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.Emoji;
import pextystudios.emogg.util.ResourceUtil;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Emogg implements ClientModInitializer {
    private static Emogg INSTANCE;

    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    public ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("customemotes", "emotes");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                LOGGER.info("Emoji load has started...");

                ResourceUtil.processModResources(
                        "emoji",
                        string -> string.endsWith(".png") || string.endsWith(".gif"),
                        resourceLocation -> regEmoji(Emoji.from(resourceLocation))
                );

                LOGGER.info("All emojis loaded!");
            }
        });
    }

    public void regEmoji(Emoji emoji) {
        if (!emoji.isValid()) {
            LOGGER.error("Invalid: " + emoji);
            return;
        }

        if (allEmojis.containsKey(emoji.getName())) {
            LOGGER.error("Failed to load: " + emoji + ", because it is already defined!");
            return;
        }

        LOGGER.info("Loaded: " + emoji);

        allEmojis.put(emoji.getName(), emoji);
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.allEmojis.values())
                .stream()
                .map(Emoji::getCode)
                .collect(Collectors.toList());
    }

    public static Emogg getInstance() {return INSTANCE;}
}
