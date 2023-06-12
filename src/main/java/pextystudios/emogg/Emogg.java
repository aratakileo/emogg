package pextystudios.emogg;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
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

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("customemotes", "emotes");
            }

            public void reload(ResourceManager manager) {
                LOGGER.info("Emoji load has started...");

                for (Identifier identifier: manager.findResources("emoji/", path -> path.endsWith(".png")))
                    regEmoji(new Emoji(identifier));

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
