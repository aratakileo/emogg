package io.github.aratakileo.emogg.handler;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.NativeGifImage;
import io.github.aratakileo.emogg.resource.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.StringUtil;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;
import io.github.aratakileo.emogg.Emogg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EmojiHandler {
    private static EmojiHandler instance;

    public final static Predicate<String> HAS_EMOJIS_EXTENSION = path -> path.endsWith(EmojiUtil.PNG_EXTENSION)
            || path.endsWith(NativeGifImage.GIF_EXTENSION);
    public final static Predicate<ResourceLocation> IS_EMOJI_LOCATION = resourceLocation -> HAS_EMOJIS_EXTENSION.test(
            resourceLocation.getPath()
    );

    public final static String CATEGORY_DEFAULT = "other",
            CATEGORY_ANIME = "anime",
            CATEGORY_MEMES = "memes",
            CATEGORY_PEOPLE = "people",
            CATEGORY_NATURE = "nature",
            CATEGORY_FOOD = "food",
            CATEGORY_ACTIVITIES = "activities",
            CATEGORY_TRAVEL = "travel",
            CATEGORY_OBJECTS = "objects",
            CATEGORY_SYMBOLS = "symbols",
            CATEGORY_FLAGS = "flags";

    private final ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> emojiCategories = new ConcurrentHashMap<>();

    private EmojiHandler() {
        // All login in `init()`
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

    public ConcurrentHashMap.KeySetView<String, List<String>> getCategoryNames() {
        return emojiCategories.keySet();
    }

    public List<Emoji> getEmojisByCategory(String name) {
        if (name.equals(FrequentlyUsedEmojiController.CATEGORY_FREQUENTLY_USED))
            return EmoggConfig.instance.frequentlyUsedEmojis
                    .stream()
                    .filter(emojiStatistic -> allEmojis.containsKey(emojiStatistic.emojiName))
                    .map(emojiStatistic -> allEmojis.get(emojiStatistic.emojiName))
                    .toList();

        if (!emojiCategories.containsKey(name)) return null;

        return emojiCategories.get(name).stream().map(allEmojis::get).toList();
    }

    public Stream<Emoji> getEmojisStream() {
        return Lists.newArrayList(allEmojis.values()).stream();
    }

    public Optional<Emoji> getRandomEmoji() {
        return allEmojis.values()
                .stream()
                .skip((int) (allEmojis.size() * Math.random()))
                .findFirst();
    }

    public void regEmoji(ResourceLocation resourceLocation) {
        var emojiName = EmojiUtil.normalizeNameOrCategory(EmojiUtil.getNameFromPath(resourceLocation));
        emojiName = getUniqueName(resourceLocation, emojiName);

        if (emojiName == null) {
            if (EmoggConfig.instance.isDebugModeEnabled)
                Emogg.LOGGER.error(String.format(
                        "Failed to load %s, because it is already defined",
                        StringUtil.repr(resourceLocation)
                ));
            return;
        }

        var emoji = Emoji.from(emojiName, resourceLocation);

        if (emoji == null) {
            Emogg.LOGGER.error(String.format(
                    "Failed to load %s, because it is invalid",
                    StringUtil.repr(resourceLocation)
            ));
            return;
        }

        allEmojis.put(emojiName, emoji);

        regEmojiInItsCategory(emoji);

        if (EmoggConfig.instance.isDebugModeEnabled)
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
        final var startsLoadingAt = System.currentTimeMillis();

        if (EmoggConfig.instance.isDebugModeEnabled)
            Emogg.LOGGER.info("[emogg] Updating emoji lists...");

        emojiCategories.clear();
        allEmojis.clear();

        resourceManager.listResources(EmojiUtil.EMOJI_FOLDER_NAME, IS_EMOJI_LOCATION)
                .keySet()
                .parallelStream()
                .forEach(this::regEmoji);

        emojiCategories.values().parallelStream().forEach(Collections::sort);

        if (!allEmojis.isEmpty())
            Emogg.LOGGER.info(String.format(
                    "[emogg] Updating the lists is complete. %s emojis have been defined and loaded in %ss!",
                    allEmojis.size(),
                    (System.currentTimeMillis() - startsLoadingAt) / 1000d
            ));
        else
            Emogg.LOGGER.info("[emogg] Updating the lists is complete. No emojis has been defined!");

        FrequentlyUsedEmojiController.removeAllNonExistentEmojisFromList();
    }

    public static void init() {
        instance = new EmojiHandler();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(Emogg.NAMESPACE_OR_ID, EmojiUtil.EMOJI_FOLDER_NAME);
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        instance.load(resourceManager);
                    }
                }
        );
    }

    public static EmojiHandler getInstance() {
        return instance;
    }

    public static String getDisplayableCategoryName(String category) {
        final var categoryLangKey = "emogg.category." + category;
        final var displayableName = Language.getInstance().getOrDefault(categoryLangKey);

        if (displayableName.equals(categoryLangKey)) return StringUtils.capitalize(category)
                .replaceAll("_", " ");

        return displayableName;
    }
}
