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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Emogg implements ClientModInitializer {
    private static Emogg INSTANCE;

    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    public final ConcurrentHashMap<String, Emoji> emojis = new ConcurrentHashMap<>();

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

                for (ResourceLocation resourceLocation: resourceManager.listResources("emoji/", path -> path.endsWith(".png")))
                    regEmoji(new Emoji(resourceLocation));

                LOGGER.info("All emojis loaded!");
            }
        });
    }

    public void regEmoji(Emoji emoji) {
        if (emoji.getWidth() == -1)
            return;

        if (emojis.containsKey(emoji.getName())) {
            LOGGER.error("Failed to load: " + emoji + ", because it is already defined!");
            return;
        }

        LOGGER.info("Loaded: " + emoji);

        emojis.put(emoji.getName(), emoji);
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.emojis.keys().asIterator())
                .stream()
                .map(name -> ":" + name + ":")
                .collect(Collectors.toList());
    }

    public static Emogg getInstance() {return INSTANCE;}
}
