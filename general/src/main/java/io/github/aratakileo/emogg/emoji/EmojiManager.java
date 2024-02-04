package io.github.aratakileo.emogg.emoji;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.util.ClientEnvironment;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.NativeGifImage;
import io.github.aratakileo.emogg.util.StringUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import io.github.aratakileo.emogg.Emogg;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ClientEnvironment
public class EmojiManager {
    private static @Nullable EmojiManager instance;

    public final static Predicate<@NotNull String> HAS_EMOJIS_EXTENSION
            = path -> path.endsWith(EmojiUtil.PNG_EXTENSION) || path.endsWith(NativeGifImage.GIF_EXTENSION);
    public final static Predicate<@NotNull ResourceLocation> IS_EMOJI_LOCATION
            = resourceLocation -> HAS_EMOJIS_EXTENSION.test(resourceLocation.getPath());

    private final ConcurrentHashMap<@NotNull Integer, @NotNull Emoji> emojiById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<@NotNull String, @NotNull Emoji> emojiByName = new ConcurrentHashMap<>();

    // Only used to keep ids consistent across resource reloads
    private final Map<@NotNull String, @NotNull Integer> nameToIdMap = new HashMap<>();
    private final ConcurrentHashMap<@NotNull String, @NotNull List<@NotNull String>> emojiCategories
            = new ConcurrentHashMap<>();

    private EmojiManager() {
        // All logic in `init()`
    }

    public boolean isEmpty() {
        return emojiByName.isEmpty();
    }

    public boolean hasEmoji(int id) {
        return emojiById.containsKey(id);
    }

    public boolean hasEmoji(@NotNull String name) {
        return emojiByName.containsKey(name);
    }

    public @Nullable Emoji getEmoji(int id) {
        return emojiById.get(id);
    }

    public @Nullable Emoji getEmoji(@NotNull String name) {
        return emojiByName.get(name);
    }

    public boolean hasCategory(@NotNull String categoryKey) {
        return emojiCategories.containsKey(categoryKey);
    }

    public @NotNull List<String> getCategoryKeys() {
        return emojiCategories.keySet().stream().toList();
    }

    public @Nullable List<@NotNull Emoji> getEmojisByCategory(@NotNull String name) {
        if (name.equals(FueController.CATEGORY_FREQUENTLY_USED))
            return FueController.getEmojis();

        if (!emojiCategories.containsKey(name)) return null;

        return emojiCategories.get(name).stream().map(emojiByName::get).toList();
    }

    public @NotNull Stream<@NotNull Emoji> getEmojisStream() {
        return Lists.newArrayList(emojiByName.values()).stream();
    }

    public @NotNull Optional<Emoji> getRandomEmoji() {
        return emojiByName.values()
                .stream()
                .skip((int) (emojiByName.size() * Math.random()))
                .findFirst();
    }

    // TODO: refactor name generation to work with different loader readers

    public void regEmoji(@NotNull ResourceLocation resourceLocation) {
        var emojiName = EmojiUtil.normalizeEmojiKeyOrCategoryKey(EmojiUtil.getNameFromPath(resourceLocation));
        emojiName = getUniqueName(resourceLocation, emojiName);

        if (Objects.isNull(emojiName)) {
            if (EmoggConfig.instance.enableDebugMode)
                Emogg.LOGGER.error(String.format(
                        "Failed to load %s, because it is already defined",
                        StringUtil.repr(resourceLocation)
                ));

            return;
        }

        int id;
        synchronized (nameToIdMap) {
            id = nameToIdMap.computeIfAbsent(emojiName, name -> nameToIdMap.size());
        }

        var emoji = Emoji.fromResource(id, emojiName, resourceLocation);

        emojiByName.put(emojiName, emoji);
        emojiById.put(emoji.getId(), emoji);

        regEmojiInItsCategory(emoji);

        if (EmoggConfig.instance.enableDebugMode)
            Emogg.LOGGER.info(String.format(
                    "Discovered %s as %s to category <%s>",
                    StringUtil.repr(resourceLocation),
                    emoji.getCode(),
                    emoji.getCategory()
            ));
   }

    private @Nullable String getUniqueName(@NotNull ResourceLocation resourceLocation, @NotNull String emojiName) {
        if (emojiByName.containsKey(emojiName)) {
//            if (emojiByName.get(emojiName).getResourceLocation().equals(resourceLocation))
//                return null;

            var emojiNameIndex = 0;
            var newEmojiName = emojiName + emojiNameIndex;

            while (emojiByName.containsKey(newEmojiName)) {
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

    private void onResourceReload(@NotNull ResourceManager resourceManager) {
        final var startsLoadingAt = System.currentTimeMillis();

        if (EmoggConfig.instance.enableDebugMode)
            Emogg.LOGGER.info("[emogg] Updating emoji lists...");

        EmojiAtlas.clear();

        emojiCategories.clear();
        emojiById.clear();
        emojiByName.clear();

        resourceManager.listResources(EmojiUtil.EMOJI_FOLDER_NAME, IS_EMOJI_LOCATION)
                .keySet()
                .forEach(this::regEmoji);

        emojiCategories.values().forEach(Collections::sort);

        Emogg.LOGGER.info(String.format(
                "[emogg] The emoji list has been updated. Discovered %s emojis in %ss!",
                emojiByName.size(),
                (System.currentTimeMillis() - startsLoadingAt) / 1000d
        ));

        FueController.removeAllNonExistentFue();
    }

    public static void init(@NotNull ResourceManager resourceManager) {
        instance = new EmojiManager();
        instance.onResourceReload(resourceManager);
    }

    public static @NotNull EmojiManager getInstance() {
        if (instance == null) throw new NullPointerException("EmojiManager not initialized!");
        return instance;
    }
}
