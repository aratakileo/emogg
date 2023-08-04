package pextystudios.emogg.emoji.handler;

import org.jetbrains.annotations.NotNull;
import pextystudios.emogg.EmoggConfig;
import pextystudios.emogg.emoji.resource.Emoji;

import java.util.List;

public class FrequentlyUsedEmojiController {
    public final static String CATEGORY_FREQUENTLY_USED = "$frequently_used";

    public final static int MAX_NUMBER_OF_RECENTLY_USED_EMOJIS = 27;

    public static List<Emoji> getEmojis() {
        return EmojiHandler.getInstance().getEmojisByCategory(CATEGORY_FREQUENTLY_USED);
    }

    public static int getNumberOfEmojis() {
        return EmoggConfig.instance.frequentlyUsedEmojis.size();
    }

    public static void removeAllNonExistentEmojisFromList() {
        EmoggConfig.instance.frequentlyUsedEmojis.removeIf(emojiStatistic -> !EmojiHandler.getInstance().hasEmoji(emojiStatistic.emojiName));
        EmoggConfig.save();
    }

    public static void markEmojiUse(@NotNull Emoji emoji) {
        final var emojiName = emoji.getName();
        final var recentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
        final var recentlyUsedEmojiNames = EmoggConfig.instance.frequentlyUsedEmojis.stream().map(EmojiStatistic::getEmojiName).toList();

        if (recentlyUsedEmojiNames.contains(emojiName)) {
            final var increasableEmojiIndex = recentlyUsedEmojiNames.indexOf(emojiName);
            final var increasableEmojiStatistic = recentlyUsedEmojis.get(increasableEmojiIndex);

            increasableEmojiStatistic.useAmount++;

            if (increasableEmojiIndex == 1 && recentlyUsedEmojis.get(0).useAmount == increasableEmojiStatistic.useAmount) {
                EmoggConfig.save();
                return;
            }

            if (increasableEmojiIndex != 0) {
                var prevIndex = -1;

                for (var i = 0; i < increasableEmojiIndex; i++) {
                    final var iEmojiStatistic = recentlyUsedEmojis.get(i);

                    prevIndex++;
                    if (iEmojiStatistic.useAmount < increasableEmojiStatistic.useAmount) break;
                }

                recentlyUsedEmojis.remove(increasableEmojiIndex);
                recentlyUsedEmojis.add(prevIndex, increasableEmojiStatistic);
            }

            EmoggConfig.save();
            return;
        }

        if (recentlyUsedEmojis.size() == MAX_NUMBER_OF_RECENTLY_USED_EMOJIS)
            recentlyUsedEmojis.remove(MAX_NUMBER_OF_RECENTLY_USED_EMOJIS - 1);

        recentlyUsedEmojis.add(new EmojiStatistic(emojiName));
        EmoggConfig.save();
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
