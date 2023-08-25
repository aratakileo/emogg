package io.github.aratakileo.emogg.handler;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.gui.EmojiTextProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FrequentlyUsedEmojiController {
    public final static String CATEGORY_FREQUENTLY_USED = "$frequently_used";

    private final static int MAX_NUMBER_OF_RECENTLY_USED_EMOJIS = 45,
            MAX_USES_POINTS = 20,
            INITIAL_POINTS = 10;

    public static List<Emoji> getEmojis() {
        return EmojiHandler.getInstance().getEmojisByCategory(CATEGORY_FREQUENTLY_USED);
    }

    public static void removeAllNonExistentEmojisFromList() {
        EmoggConfig.instance.frequentlyUsedEmojis.removeIf(emojiStatistic -> {
            final var itExists = EmojiHandler.getInstance().hasEmoji(emojiStatistic.emojiName);

            return !emojiStatistic.isUnderRemoveProtection(itExists) && !itExists;
        });

        EmoggConfig.save();
    }

    public static void collectStatisticFrom(@NotNull String text) {
        if (text.isEmpty()) return;

        final List<String> emojisInText = Lists.newArrayList();

        for (var emojiLiteral: EmojiTextProcessor.from(text).getEmojiLiterals()) {
            final var emojiName = emojiLiteral.getEmoji().getName();

            if (emojisInText.contains(emojiName)) continue;

            emojisInText.add(emojiName);
            markEmojiUse(emojiLiteral.getEmoji());
        }
        
        if (emojisInText.isEmpty())
            return;

        for (var frequentlyUsedEmojiStatistic: Lists.newArrayList(EmoggConfig.instance.frequentlyUsedEmojis)) {
            if (emojisInText.contains(frequentlyUsedEmojiStatistic.emojiName)) continue;

            frequentlyUsedEmojiStatistic.usePoints--;

            if (frequentlyUsedEmojiStatistic.usePoints == 0)
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

                if (temp.usePoints <= frequentlyUsedEmojis.get(k - 1).usePoints) continue;

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
            emojiStatistic.usePoints = Math.min(MAX_USES_POINTS, emojiStatistic.usePoints + 1);
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
        public int usePoints = INITIAL_POINTS;
        private boolean isUnderRemoveProtection = true;

        public EmojiStatistic(String emojiName) {
            this.emojiName = emojiName;
        }

        public String getEmojiName() {
            return emojiName;
        }

        public boolean isUnderRemoveProtection(boolean enableRemoveProtection) {
            final var isUnderRemoveProtectionOld = isUnderRemoveProtection;

            isUnderRemoveProtection = enableRemoveProtection;

            return isUnderRemoveProtectionOld;
        }
    }
}
