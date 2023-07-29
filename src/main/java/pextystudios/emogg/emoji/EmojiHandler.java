package pextystudios.emogg.emoji;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.EmoggConfig;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmojiHandler {

    private static EmojiHandler INSTANCE;

    public final static String STATIC_EMOJI_EXTENSION = ".png";
    public final static String ANIMATED_EMOJI_EXTENSION = ".gif";
    public final static Predicate<String> HAS_EMOJIS_EXTENSION = path -> {
        return path.endsWith(STATIC_EMOJI_EXTENSION) || path.endsWith(ANIMATED_EMOJI_EXTENSION);
    };
    public final static Predicate<ResourceLocation> IS_EMOJI_LOCATION = resourceLocation -> HAS_EMOJIS_EXTENSION.test(resourceLocation.getPath());
    public final static String EMOJIS_PATH_PREFIX = "emoji";
    public final static String DEFAULT_CATEGORY_NAME = "other";
    public final static int EMOJI_DEFAULT_RENDER_SIZE = 10;

    private final ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> emojiCategories = new ConcurrentHashMap<>();

    public EmojiHandler() {
        INSTANCE = this;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return new ResourceLocation(Emogg.NAMESPACE, EMOJIS_PATH_PREFIX);
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    load(resourceManager);
                }
            }
        );
    }

    public boolean isEmpty() {
        return allEmojis.isEmpty();
    }

    public boolean hasEmoji(String name) {
        return allEmojis.containsKey(name);
    }

    public Emoji getEmoji(String name) {
        return allEmojis.get(name);
    }

    public Stream<Emoji> getEmojiStream() {
        return allEmojis.values().stream();
    }

    public int getNumberOfEmojis() {
        return allEmojis.size();
    }

    public ConcurrentHashMap.KeySetView<String, List<String>> getCategories() {return emojiCategories.keySet();}

    public List<Emoji> getEmojisByCategory(String category) {
        if (!emojiCategories.containsKey(category)) return null;

        return emojiCategories.get(category).stream().map(allEmojis::get).toList();
    }

    public Optional<Emoji> getRandomEmoji() {
        return allEmojis.values()
                .stream()
                .skip(
                        (int) (
                                (
                                        allEmojis.size()
                                ) * Math.random()
                        )
                )
                .findFirst();
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.allEmojis.values())
                .stream()
                .map(Emoji::getCode)
                .collect(Collectors.toList());
    }

    public void regEmoji(ResourceLocation resourceLocation) {
        var emojiName = Emoji.normalizeNameOrCategory(Emoji.getNameFromPath(resourceLocation));
        emojiName = getUniqueName(resourceLocation, emojiName);

        if (emojiName == null) {
            Emogg.LOGGER.error(String.format(
                    "Failed to load %s, because it is already defined",
                    StringUtil.repr(resourceLocation)
            ));
            return;
        }

        var emoji = Emoji.from(emojiName, resourceLocation);

        if (!emoji.isValid()) {
            Emogg.LOGGER.error(String.format(
                    "Failed to load %s, because it has invalid format",
                    StringUtil.repr(resourceLocation)
            ));
            return;
        }

        allEmojis.put(emojiName, emoji);

        regEmojiInItsCategory(emoji);

        Emogg.LOGGER.info(String.format(
                "Loaded %s as %s to %s",
                StringUtil.repr(resourceLocation),
                emoji.getCode(),
                emoji.getCategory()
        ));
    }

    private String getUniqueName(ResourceLocation resourceLocation, String emojiName) {
        if (allEmojis.containsKey(emojiName)) {
            if (allEmojis.get(emojiName).getResourceLocation().equals(resourceLocation))
                return null;

            var emojiNameIndex = 0;
            var newEmojiName = emojiName + emojiNameIndex;

            while (allEmojis.containsKey(newEmojiName)) {
                emojiNameIndex++;
                newEmojiName = emojiName + emojiNameIndex;
            }

            return newEmojiName;
        }

        return emojiName;
    }

    private void regEmojiInItsCategory(Emoji emoji) {
        if (!emojiCategories.containsKey(emoji.getCategory()))
            emojiCategories.put(emoji.getCategory(), new ArrayList<>());

        final var emojiNamesInCategory = emojiCategories.get(emoji.getCategory());

        if (emojiNamesInCategory.contains(emoji.getName()))
            return;

        emojiNamesInCategory.add(emoji.getName());
    }

    private void load(ResourceManager resourceManager) {
        Emogg.LOGGER.info("Updating emoji lists...");

        emojiCategories.clear();
        allEmojis.clear();

        for (var resourceLocation: resourceManager.listResources(EMOJIS_PATH_PREFIX, IS_EMOJI_LOCATION).keySet())
            regEmoji(resourceLocation);

        Emogg.LOGGER.info(String.format(
                "Updating the lists is complete. %s emojis have been defined!",
                allEmojis.size()
        ));
    }

    public static EmojiHandler getInstance() {
        return INSTANCE;
    }

    public static String getDisplayableCategoryName(String category) {
        final var categoryLangKey = "emogg.category." + category;
        final var displayableName = Language.getInstance().getOrDefault(categoryLangKey);

        if (displayableName.equals(categoryLangKey)) return StringUtils.capitalize(category);

        return displayableName;
    }
}
