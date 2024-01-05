package io.github.aratakileo.emogg.handler;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.StringUtil;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import io.github.aratakileo.emogg.Emogg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EmojiHandler {
    private static @Nullable EmojiHandler instance;

    public final static Predicate<@NotNull String> HAS_EMOJIS_EXTENSION
            = path -> path.endsWith(EmojiUtil.PNG_EXTENSION) || path.endsWith(NativeGifImage.GIF_EXTENSION);
    public final static Predicate<@NotNull ResourceLocation> IS_EMOJI_LOCATION
            = resourceLocation -> HAS_EMOJIS_EXTENSION.test(resourceLocation.getPath());

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

    private final ConcurrentHashMap<@NotNull String, @NotNull Emoji> allEmojis = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<@NotNull String, @NotNull List<@NotNull String>> emojiCategories
            = new ConcurrentHashMap<>();

    private EmojiHandler() {
        // All login in `init()`
    }

    public boolean isEmpty() {
        return allEmojis.isEmpty();
    }

    public boolean hasEmoji(String name) {
        return allEmojis.containsKey(name);
    }

    public @Nullable Emoji getEmoji(String name) {
        return allEmojis.get(name);
    }

    public @NotNull ConcurrentHashMap.KeySetView<@NotNull String, @NotNull List<@NotNull String>> getCategoryNames() {
        return emojiCategories.keySet();
    }

    public @Nullable List<@NotNull Emoji> getEmojisByCategory(@NotNull String name) {
        if (name.equals(FueController.CATEGORY_FREQUENTLY_USED))
            return FueController.getEmojis();

        if (!emojiCategories.containsKey(name)) return null;

        return emojiCategories.get(name).stream().map(allEmojis::get).toList();
    }

    public @NotNull Stream<@NotNull Emoji> getEmojisStream() {
        return Lists.newArrayList(allEmojis.values()).stream();
    }

    public @NotNull Optional<Emoji> getRandomEmoji() {
        return allEmojis.values()
                .stream()
                .skip((int) (allEmojis.size() * Math.random()))
                .findFirst();
    }

    public void regEmoji(@NotNull ResourceLocation resourceLocation) {
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

    private @Nullable String getUniqueName(@NotNull ResourceLocation resourceLocation, @NotNull String emojiName) {
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

    private void regEmojiInItsCategory(@NotNull Emoji emoji) {
        if (!emojiCategories.containsKey(emoji.getCategory()))
            emojiCategories.put(emoji.getCategory(), new ArrayList<>());

        final var emojiNamesInCategory = emojiCategories.get(emoji.getCategory());

        if (emojiNamesInCategory.contains(emoji.getName()))
            return;

        emojiNamesInCategory.add(emoji.getName());
    }

    private void load(@NotNull ResourceManager resourceManager) {
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

        FueController.removeAllNonExistentFue();
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

    public static @Nullable EmojiHandler getInstance() {
        return instance;
    }
}
