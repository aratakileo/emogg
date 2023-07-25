package pextystudios.emogg.handler;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EmojiHandler {
    private static final Predicate<Emoji> IS_NOT_BUILTIN_EMOJI = emoji -> {
        return ConfigHandler.data.useBuiltinEmojiEnabled || !getInstance().builtinEmojis.containsKey(emoji.getName());
    };

    private static EmojiHandler INSTANCE;

    public static final String STATIC_EMOJI_EXTENSION = ".png";
    public static final String ANIMATED_EMOJI_EXTENSION = ".gif";
    public static final Predicate<String> HAS_EMOJIS_EXTENSION = path -> {
        return path.endsWith(STATIC_EMOJI_EXTENSION) || path.endsWith(ANIMATED_EMOJI_EXTENSION);
    };
    public static final Predicate<ResourceLocation> IS_EMOJI_LOCATION = resourceLocation -> HAS_EMOJIS_EXTENSION.test(resourceLocation.getPath());
    public static final String EMOJIS_PATH_PREFIX = "emoji";
    public static final String DEFAULT_EMOJI_CATEGORY = "other";
    public static final int EMOJI_DEFAULT_RENDER_SIZE = 10;

    private final ConcurrentHashMap<String, Emoji> allEmojis = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Emoji> builtinEmojis = new ConcurrentHashMap<>();
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

    public boolean hasEmoji(String name) {
        return allEmojis.containsKey(name);
    }

    public Emoji getEmoji(String name) {
        return allEmojis.get(name);
    }

    public List<Emoji> getEmojis() {
        return allEmojis.values().stream().filter(IS_NOT_BUILTIN_EMOJI).toList();
    }

    public Enumeration<String> getCategories() {return emojiCategories.keys();}

    public List<Emoji> getEmojisFromCategory(String category) {
        if (!emojiCategories.containsKey(category)) return null;

        return emojiCategories.get(category).stream().map(allEmojis::get).toList();
    }

    public String getDisplayableCategoryName(String category) {
        final var categoryLangKey = "emogg.category." + category;
        final var displayableName = Language.getInstance().getOrDefault(categoryLangKey);

        if (displayableName.equals(categoryLangKey)) return StringUtils.capitalize(category);

        return displayableName;
    }

    public Optional<Emoji> getRandomEmoji() {
        return getRandomEmoji(false);
    }

    public Optional<Emoji> getRandomEmoji(boolean includeBuiltinEmojisIfThereIsNoUserEmojis) {
        return allEmojis.values()
                .stream()
                .filter(emoji -> (
                        builtinEmojis.size() == allEmojis.size() && includeBuiltinEmojisIfThereIsNoUserEmojis
                ) || IS_NOT_BUILTIN_EMOJI.test(emoji))
                .skip(
                        (int) (
                                (
                                        allEmojis.size() - (
                                                ConfigHandler.data.useBuiltinEmojiEnabled ? 0 : builtinEmojis.size()
                                        )
                                ) * Math.random()
                        )
                )
                .findFirst();
    }

    public Collection<String> getEmojiSuggestions() {
        return Lists.newArrayList(this.allEmojis.values())
                .stream()
                .filter(IS_NOT_BUILTIN_EMOJI)
                .map(Emoji::getCode)
                .collect(Collectors.toList());
    }

    public void regEmoji(ResourceLocation resourceLocation) {
        var emojiName = Emoji.normalizeNameOrCategory(Emoji.getNameFromPath(resourceLocation));
        emojiName = getUniqueName(resourceLocation, emojiName, allEmojis);

        if (emojiName == null) {
            Emogg.LOGGER.error(String.format(
                    "Failed to load %s, because it is already defined",
                    StringUtil.repr(resourceLocation)
            ));
            return;
        }

        final var newEmojiName = getUniqueName(resourceLocation, emojiName, builtinEmojis);

        if (newEmojiName == null) {
            regEmojiInItsCategory(builtinEmojis.get(emojiName));
            return;
        }

        var emoji = Emoji.from(newEmojiName, resourceLocation);

        if (!emoji.isValid()) {
            Emogg.LOGGER.error(String.format(
                    "Failed to load %s, because it has invalid format",
                    StringUtil.repr(resourceLocation)
            ));
            return;
        }

        allEmojis.put(newEmojiName, emoji);

        regEmojiInItsCategory(emoji);

        Emogg.LOGGER.info(String.format(
                "Loaded %s as %s in %s",
                StringUtil.repr(resourceLocation),
                emoji.getCode(),
                emoji.getCategory()
        ));
    }

    private String getUniqueName(ResourceLocation resourceLocation, String emojiName, ConcurrentHashMap<String, Emoji> emojis) {
        if (emojis.containsKey(emojiName)) {
            if (emojis.get(emojiName).getResourceLocation().equals(resourceLocation))
                return null;

            var emojiNameIndex = 0;
            var newEmojiName = emojiName + emojiNameIndex;

            while (emojis.containsKey(newEmojiName)) {
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

        if (builtinEmojis.isEmpty()) {
            Emoji emoji;

            // Start of autogenerated code (list of built-in emojis)
            // The code below is autogenerated

            //!START
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/boykisser.png"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/minecraft.gif"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/nezukorun.gif"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/blob-skeleton-dance.gif"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/cutie.png"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/huh.png"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/im-okay.png"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/stupid_exited.png"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/turn-into-clown.gif"));
            builtinEmojis.put(emoji.getName(), emoji);
            emoji = Emoji.from(new ResourceLocation(Emogg.NAMESPACE, "emoji/people/waving_hand.gif"));
            builtinEmojis.put(emoji.getName(), emoji);
            //!END

            // End of auto generated code
        }

        emojiCategories.clear();
        allEmojis.clear();

        for (var resourceLocation: resourceManager.listResources(EMOJIS_PATH_PREFIX, IS_EMOJI_LOCATION).keySet())
            regEmoji(resourceLocation);

        allEmojis.putAll(builtinEmojis);

        Emogg.LOGGER.info("Updating the lists is complete!");
    }

    public static EmojiHandler getInstance() {
        return INSTANCE;
    }
}
