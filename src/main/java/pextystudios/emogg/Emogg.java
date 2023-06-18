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
import pextystudios.emogg.util.StringUtil;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Emogg implements ClientModInitializer {
    private static Emogg INSTANCE;

    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    public final ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(NAMESPACE, Emoji.EMOJIS_PATH_PREFIX);
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                LOGGER.info("Updating emoji lists...");

                allEmojis.clear();

                for (var resourceLocation: resourceManager.listResources(Emoji.EMOJIS_PATH_PREFIX, Emoji.HAS_EMOJIS_EXTENSION))
                    regEmoji(resourceLocation);

                LOGGER.info("Updating the lists is complete!");
            }
        });
    }

    private void regEmoji(ResourceLocation resourceLocation) {
        var emojiName = Emoji.normalizeName(Emoji.getNameFromPath(resourceLocation));

        if (allEmojis.containsKey(emojiName)) {
            if (allEmojis.get(emojiName).getResourceLocation().equals(resourceLocation)) {
                LOGGER.error(String.format("Failed to load %s, because it is already defined", StringUtil.repr(resourceLocation)));
                return;
            }

            var emojiNameIndex = 0;
            var newEmojiName = emojiName + emojiNameIndex;

            while (allEmojis.containsKey(newEmojiName)) {
                emojiNameIndex++;
                newEmojiName = emojiName + emojiNameIndex;
            }
        }

        var emoji = Emoji.from(emojiName, resourceLocation);

        if (!emoji.isValid()) {
            LOGGER.error(String.format("Failed to load %s, because it has invalid format", StringUtil.repr(resourceLocation)));
            return;
        }

        allEmojis.put(emojiName, emoji);

        LOGGER.info(String.format("Loaded %s as %s", StringUtil.repr(resourceLocation), emoji.getCode()));
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.allEmojis.values())
                .stream()
                .map(Emoji::getCode)
                .collect(Collectors.toList());
    }

    public static Emogg getInstance() {return INSTANCE;}
}
