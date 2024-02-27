package io.github.aratakileo.emogg.gui;

import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.gui.widget.EmojiSelectionMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CategoryContent {
    private final String name;

    private @Nullable List<Emoji> emojis;
    private int lineCount;
    private boolean isExpanded;

    public CategoryContent(@NotNull String name) {
        this.name = name;
        this.isExpanded = !EmoggConfig.instance.hiddenCategoryNames.contains(name);

        refreshEmojis();
    }

    public boolean isEmpty() {
        return emojis == null || emojis.isEmpty();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;

        final var hiddenCategoryNames = EmoggConfig.instance.hiddenCategoryNames;

        if (hiddenCategoryNames.contains(name)) {
            if (isExpanded) {
                hiddenCategoryNames.remove(name);
                EmoggConfig.instance.save();
            }

            return;
        }

        if (!isExpanded) {
            hiddenCategoryNames.add(name);
            EmoggConfig.instance.save();
        }
    }

    public void toggleExpand() {
        setExpanded(!isExpanded);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDisplayableName() {
        final var categoryLangKey = "emogg.category." + name;
        final var displayableName = Language.getInstance().getOrDefault(categoryLangKey);

        if (displayableName.equals(categoryLangKey))
            return StringUtils.capitalize(name).replaceAll("_", " ");

        return displayableName;
    }

    public @NotNull String getDisplayableName(int maxWidth) {
        final var displayableName = getDisplayableName();
        final var font = Minecraft.getInstance().font;

        if (font.width(displayableName) <= maxWidth) return displayableName;

        final var stringBuilder = new StringBuilder(displayableName);

        for (var i = displayableName.length() - 1; i >= 0; i--) {
            stringBuilder.deleteCharAt(i);

            if (font.width(stringBuilder + "...") <= maxWidth) {
                stringBuilder.append("...");
                break;
            }
        }

        return stringBuilder.toString();
    }

    public @Nullable List<Emoji> getEmojis() {
        return emojis;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getRenderLineCount() {
        return isExpanded ? lineCount : 1;
    }

    public void refreshEmojis() {
        emojis = EmojiManager.getInstance().getEmojisByCategory(name);
        lineCount = (int) (
                Math.ceil((double)emojis.size() / (double) EmojiSelectionMenu.MAX_NUMBER_OF_EMOJIS_IN_LINE) + 1
        );
    }
}
