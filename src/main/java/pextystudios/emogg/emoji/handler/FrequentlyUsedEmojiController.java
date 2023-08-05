package pextystudios.emogg.emoji.handler;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import pextystudios.emogg.EmoggConfig;
import pextystudios.emogg.emoji.font.EmojiTextProcessor;
import pextystudios.emogg.emoji.resource.Emoji;

import java.util.List;

public class FrequentlyUsedEmojiController {
    public final static String CATEGORY_FREQUENTLY_USED = "$frequently_used";

    private final static int MAX_NUMBER_OF_RECENTLY_USED_EMOJIS = 27,
            MAX_NUMBER_OF_USES = 10,
            MAX_NUMBER_OF_NOT_USES = 10;

    public static List<Emoji> getEmojis() {
        return EmojiHandler.getInstance().getEmojisByCategory(CATEGORY_FREQUENTLY_USED);
    }

    public static void removeAllNonExistentEmojisFromList() {
        EmoggConfig.instance.frequentlyUsedEmojis.removeIf(emojiStatistic -> !EmojiHandler.getInstance().hasEmoji(emojiStatistic.emojiName));
        EmoggConfig.save();
    }

    public static void collectStatisticFrom(@NotNull String text) {
        if (text.isEmpty()) return;

        final List<String> emojisInText = Lists.newArrayList();

        for (var emojiLiteral: EmojiTextProcessor.from(text).getEmojiLiterals()) {
            final var emojiName = emojiLiteral.emoji().getName();

            if (emojisInText.contains(emojiName)) continue;

            emojisInText.add(emojiName);
            markEmojiUse(emojiLiteral.emoji());
        }

        for (var frequentlyUsedEmojiStatistic: Lists.newArrayList(EmoggConfig.instance.frequentlyUsedEmojis)) {
            if (emojisInText.contains(frequentlyUsedEmojiStatistic.emojiName)) continue;

            frequentlyUsedEmojiStatistic.useAmount--;

            if (frequentlyUsedEmojiStatistic.useAmount <= -MAX_NUMBER_OF_NOT_USES)
                EmoggConfig.instance.frequentlyUsedEmojis.remove(frequentlyUsedEmojiStatistic);
        }

        bubbleSortEmojis();
        EmoggConfig.save();
    }

    private static void bubbleSortEmojis() {
        final var frequentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
        EmojiStatistic temp;
        boolean swapped;

        for (var i = 0; i < frequentlyUsedEmojis.size() - 1; i++) {
            swapped = false;

            for (var k = frequentlyUsedEmojis.size() - 1; k > i; k--) {
                temp = frequentlyUsedEmojis.get(k);

                if (temp.useAmount <= frequentlyUsedEmojis.get(k - 1).useAmount) continue;

                frequentlyUsedEmojis.set(k, frequentlyUsedEmojis.get(k - 1));
                frequentlyUsedEmojis.set(k - 1, temp);
                swapped = true;
            }

            if (!swapped) break;
        }
    }

    private static void markEmojiUse(@NotNull Emoji emoji) {
        final var frequentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
        final var frequentlyUsedEmojiNames = getFrequentlyUsedEmojiNames();
        final var emojiName = emoji.getName();

        if (frequentlyUsedEmojiNames.contains(emojiName)) {
            final var emojiStatistic = frequentlyUsedEmojis.get(frequentlyUsedEmojiNames.indexOf(emojiName));
            emojiStatistic.useAmount = Math.min(MAX_NUMBER_OF_USES, emojiStatistic.useAmount + 1);
            return;
        }

        if (frequentlyUsedEmojiNames.size() == MAX_NUMBER_OF_RECENTLY_USED_EMOJIS)
            frequentlyUsedEmojis.remove(frequentlyUsedEmojiNames.size() - 1);

        frequentlyUsedEmojis.add(new EmojiStatistic(emojiName));
    }

    private static List<String> getFrequentlyUsedEmojiNames() {
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
