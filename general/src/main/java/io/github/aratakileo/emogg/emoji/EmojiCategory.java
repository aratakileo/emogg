package io.github.aratakileo.emogg.emoji;

import com.google.common.collect.Lists;
import io.github.aratakileo.emogg.util.ClientEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@ClientEnvironment
public interface EmojiCategory {
    String DEFAULT = "other",
            ANIME = "anime",
            MEMES = "memes",
            PEOPLE = "people",
            NATURE = "nature",
            FOOD = "food",
            ACTIVITIES = "activities",
            TRAVEL = "travel",
            OBJECTS = "objects",
            SYMBOLS = "symbols",
            FLAGS = "flags";

    static @NotNull List<String> getCategoryKeys(boolean sort) {
        if (sort) {
            final var categoryKeys = Lists.newArrayList(EmojiManager.getInstance().getCategoryKeys());

            Collections.sort(categoryKeys);

            for (final var categoryKey: getBuiltinCategoryKeys()) {
                if (!categoryKeys.contains(categoryKey)) continue;

                categoryKeys.remove(categoryKey);
                categoryKeys.add(categoryKey);
            }

            if (categoryKeys.contains(FueController.CATEGORY_FREQUENTLY_USED)) {
                categoryKeys.remove(FueController.CATEGORY_FREQUENTLY_USED);
                categoryKeys.add(0, FueController.CATEGORY_FREQUENTLY_USED);
            }

            return categoryKeys;
        }

        return EmojiManager.getInstance().getCategoryKeys();
    }

    /**
     * @return all built-in category keys except `FueController.CATEGORY_FREQUENTLY_USED`
     */
    static @NotNull List<String> getBuiltinCategoryKeys() {
        return List.of(ANIME, MEMES, PEOPLE, NATURE, FOOD, ACTIVITIES, TRAVEL, OBJECTS, SYMBOLS, FLAGS, DEFAULT);
    }
}
