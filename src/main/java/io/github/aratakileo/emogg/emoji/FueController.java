package io.github.aratakileo.emogg.emoji;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.EmoggConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FueController { // Fue = FUE = Frequently Used Emojis
    public final static String CATEGORY_FREQUENTLY_USED = "$frequently_used";

    private final static int MAX_NUMBER_OF_RECENTLY_USED_EMOJIS = 45,
            MAX_USES_POINTS = 20,
            INITIAL_POINTS = 10;

    public static @NotNull List<@NotNull Emoji> getEmojis() {
        //noinspection DataFlowIssue
        return EmoggConfig.instance.frequentlyUsedEmojis
                .stream()
                .filter(emojiStatistic -> EmojiManager.getInstance().hasEmoji(emojiStatistic.emojiName))
                .map(emojiStatistic -> EmojiManager.getInstance().getEmoji(emojiStatistic.emojiName))
                .toList();
    }

    public static void removeAllNonExistentFue() { // Fue = FUE = Frequently Used Emojis
        EmoggConfig.instance.frequentlyUsedEmojis.removeIf(emojiStatistic -> {
            final var itExists = EmojiManager.getInstance().hasEmoji(emojiStatistic.emojiName);

            return !emojiStatistic.isUnderRemoveProtection(itExists) && !itExists;
        });

        EmoggConfig.save();
    }

    public static void collectStatisticFrom(@NotNull String text) {
        if (text.isEmpty()) return;

        final List<String> emojisNamesInText = Lists.newArrayList();

        for (var section : EmojiParser.getEmojiSections(text)) {
            if (!section.escaped()) {
                final var emoji = EmojiManager.getInstance().getEmoji(section.emoji());

                if (emoji == null || emojisNamesInText.contains(emoji.getName())) continue;

                emojisNamesInText.add(emoji.getName());
                markEmojiUse(emoji);
            }
        }

        if (emojisNamesInText.isEmpty()) return;

        for (var frequentlyUsedEmojiStatistic: Lists.newArrayList(EmoggConfig.instance.frequentlyUsedEmojis)) {
            if (emojisNamesInText.contains(frequentlyUsedEmojiStatistic.emojiName)) continue;

            frequentlyUsedEmojiStatistic.usePoints--;

            if (frequentlyUsedEmojiStatistic.usePoints == 0)
                EmoggConfig.instance.frequentlyUsedEmojis.remove(frequentlyUsedEmojiStatistic);
        }

//        bubbleSortEmojis();
        EmoggConfig.instance.frequentlyUsedEmojis.sort(
                Comparator.<EmojiStatistic>comparingInt(e -> e.usePoints).reversed()
        );
        EmoggConfig.save();
    }

//    private static void bubbleSortEmojis() {
//        final var frequentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
//        EmojiStatistic temp;
//        boolean swapped;
//
//        for (var i = 0; i < frequentlyUsedEmojis.size() - 1; i++) {
//            swapped = false;
//
//            for (var k = frequentlyUsedEmojis.size() - 1; k > i; k--) {
//                temp = frequentlyUsedEmojis.get(k);
//
//                if (temp.usePoints <= frequentlyUsedEmojis.get(k - 1).usePoints) continue;
//
//                frequentlyUsedEmojis.set(k, frequentlyUsedEmojis.get(k - 1));
//                frequentlyUsedEmojis.set(k - 1, temp);
//                swapped = true;
//            }
//
//            if (!swapped) break;
//        }
//    }

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

    private static @NotNull List<@NotNull String> getFrequentlyUsedEmojiNames() {
        return EmoggConfig.instance.frequentlyUsedEmojis.stream().map(EmojiStatistic::getEmojiName).toList();
    }

    public static class EmojiStatistic {
        public final String emojiName;
        public int usePoints = INITIAL_POINTS;
        private boolean isUnderRemoveProtection = true;

        public EmojiStatistic(@NotNull String emojiName) {
            this.emojiName = emojiName;
        }

        public @NotNull String getEmojiName() {
            return emojiName;
        }

        public boolean isUnderRemoveProtection(boolean enableRemoveProtection) {
            final var isUnderRemoveProtectionOld = isUnderRemoveProtection;

            isUnderRemoveProtection = enableRemoveProtection;

            return isUnderRemoveProtectionOld;
        }
    }
}
