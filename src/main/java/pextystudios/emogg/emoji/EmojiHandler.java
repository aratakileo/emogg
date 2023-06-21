package pextystudios.emogg.emoji;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.util.StringUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EmojiHandler {
    private static EmojiHandler INSTANCE;

    public static final String STATIC_EMOJI_EXTENSION = ".png";
    public static final String ANIMATED_EMOJI_EXTENSION = ".gif";
    public static final Predicate<String> HAS_EMOJIS_EXTENSION = path -> path.endsWith(STATIC_EMOJI_EXTENSION) || path.endsWith(ANIMATED_EMOJI_EXTENSION);
    public static final String EMOJIS_PATH_PREFIX = "emoji";

    private final ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();

    public EmojiHandler() {
        INSTANCE = this;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(Emogg.NAMESPACE, EMOJIS_PATH_PREFIX);
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                Emogg.LOGGER.info("Updating emoji lists...");

                allEmojis.clear();

                for (var resourceLocation: resourceManager.listResources(EMOJIS_PATH_PREFIX, HAS_EMOJIS_EXTENSION))
                    regEmoji(resourceLocation);

                Emogg.LOGGER.info("Updating the lists is complete!");
            }
        });
    }

    public boolean hasEmoji(String name) {
        return allEmojis.containsKey(name);
    }

    public Emoji getEmoji(String name) {
        return allEmojis.get(name);
    }

    public Optional<Emoji> getRandomEmoji() {
        return allEmojis.values().stream().skip((int) (allEmojis.size() * Math.random())).findFirst();
    }

    public void regEmoji(ResourceLocation resourceLocation) {
        var emojiName = Emoji.normalizeName(Emoji.getNameFromPath(resourceLocation));

        if (allEmojis.containsKey(emojiName)) {
            if (allEmojis.get(emojiName).getResourceLocation().equals(resourceLocation)) {
                Emogg.LOGGER.error(String.format("Failed to load %s, because it is already defined", StringUtil.repr(resourceLocation)));
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
            Emogg.LOGGER.error(String.format("Failed to load %s, because it has invalid format", StringUtil.repr(resourceLocation)));
            return;
        }

        allEmojis.put(emojiName, emoji);

        Emogg.LOGGER.info(String.format("Loaded %s as %s", StringUtil.repr(resourceLocation), emoji.getCode()));
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.allEmojis.values())
                .stream()
                .map(Emoji::getCode)
                .collect(Collectors.toList());
    }

    public static EmojiHandler getInstance() {
        return INSTANCE;
    }
}
