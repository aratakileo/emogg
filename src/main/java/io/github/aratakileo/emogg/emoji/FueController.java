package io.github.aratakileo.emogg.emoji;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.EmoggConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * Deciphering:
 * <p>
 * Fue -> FUE -> Frequently Used Emojis
 */
@Environment(EnvType.CLIENT)
public class FueController {
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

    /**
     * Deciphering:
     * <p>
     * Fue -> FUE -> Frequently Used Emojis
     */
    public static void removeAllNonExistentFue() {
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

        EmoggConfig.instance.frequentlyUsedEmojis.sort(
                Comparator.<EmojiStatistic>comparingInt(e -> e.usePoints).reversed()
        );
        EmoggConfig.save();
    }

    private static void markEmojiUse(@NotNull Emoji emoji) {
        final var frequentlyUsedEmojis = EmoggConfig.instance.frequentlyUsedEmojis;
        final var frequentlyUsedEmojiNames = getFueNames();
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

    /**
     * Deciphering:
     * <p>
     * Fue -> FUE -> Frequently Used Emojis
     */
    private static @NotNull List<@NotNull String> getFueNames() {
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
