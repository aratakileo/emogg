package io.github.aratakileo.emogg;

import io.github.aratakileo.elegantia.gui.config.Config;
import io.github.aratakileo.elegantia.gui.config.ConfigEntry;
import io.github.aratakileo.elegantia.gui.config.Trigger;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.emoji.FueController;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class EmoggConfig extends Config {
    public static EmoggConfig instance;

    @Trigger("debug")
    @ConfigEntry
    public boolean enableDebugMode = false;
    @ConfigEntry(triggeredBy = "debug")
    public boolean enableAtlasDebugHUD = false;
    public boolean enableCustomShaders = true;

    public @NotNull ArrayList<FueController.EmojiStatistic> frequentlyUsedEmojis = new ArrayList<>();
    public @NotNull ArrayList<String> hiddenCategoryNames = new ArrayList<>();

    @ConfigEntry(triggeredBy = "debug")
    public static void reloadAllEmojis() {
        EmojiManager.getInstance().getEmojisStream()
                .forEach(e -> e.reload(false));
    }

    @ConfigEntry(triggeredBy = "debug")
    public static void forceLoadAllEmojis() {
        EmojiManager.getInstance().getEmojisStream()
                .forEach(Emoji::forceLoad);
    }
}
