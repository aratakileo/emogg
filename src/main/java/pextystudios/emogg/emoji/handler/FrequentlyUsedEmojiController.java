package pextystudios.emogg.emoji.handler;

import org.jetbrains.annotations.NotNull;
import pextystudios.emogg.EmoggConfig;
import pextystudios.emogg.emoji.resource.Emoji;

import java.util.List;

public class FrequentlyUsedEmojiController {
    public final static String CATEGORY_FREQUENTLY_USED = "$frequently_used";

    public final static int MAX_NUMBER_OF_RECENTLY_USED_EMOJIS = 50;

    public static List<Emoji> getRecentlyUsedEmojis() {
        return EmojiHandler.getInstance().getEmojisByCategory(CATEGORY_FREQUENTLY_USED);
    }

    public static void markEmojiAsRecentlyUsed(@NotNull Emoji emoji) {
        final var emojiName = emoji.getName();
        final var recentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
        final var recentlyUsedEmojiNames = getRecentlyUsedEmojiNames();

        if (recentlyUsedEmojiNames.contains(emojiName)) {
            final var increasableEmojiIndex = recentlyUsedEmojiNames.indexOf(emojiName);
            final var increasableEmojiStatistic = recentlyUsedEmojis.get(increasableEmojiIndex);

            increasableEmojiStatistic.useAmount++;

            if (increasableEmojiIndex != 0 && recentlyUsedEmojis.get(increasableEmojiIndex - 1).useAmount < increasableEmojiStatistic.useAmount) {
                recentlyUsedEmojis.remove(increasableEmojiIndex);
                recentlyUsedEmojis.set(increasableEmojiIndex - 1, increasableEmojiStatistic);
            }

            EmoggConfig.save();
            return;
        }

        if (recentlyUsedEmojis.size() == MAX_NUMBER_OF_RECENTLY_USED_EMOJIS)
            recentlyUsedEmojis.remove(MAX_NUMBER_OF_RECENTLY_USED_EMOJIS - 1);

        recentlyUsedEmojis.add(0, new EmojiStatistic(emojiName));
        EmoggConfig.save();
    }

    private static List<String> getRecentlyUsedEmojiNames() {
        if (EmoggConfig.instance.frequentlyUsedEmojis.isEmpty()) return List.of();

        return EmoggConfig.instance.frequentlyUsedEmojis.stream().map(EmojiStatistic::getEmojiName).toList();
    }

    public static class EmojiStatistic {
        public final String emojiName;
        public int useAmount = 1;

        public EmojiStatistic(String emojiName) {
            this.emojiName = emojiName;
        }

        public String getEmojiName() {
            return emojiName;
        }
    }
}
