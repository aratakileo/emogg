package io.github.aratakileo.emogg;

import com.google.gson.Gson;
import io.github.aratakileo.emogg.handler.FrequentlyUsedEmojiController;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class EmoggConfig {
    // Non-JSON values
    private final static File file = new File("config/emogg.json");
    private final static Gson gson = new Gson();

    public static EmoggConfig instance = new EmoggConfig();

    // JSON values
    public boolean isDebugModeEnabled = false;
    public List<FrequentlyUsedEmojiController.EmojiStatistic> frequentlyUsedEmojis = new ArrayList<>();

    public static void load() {
        if (file.exists())
            try {
                final var fileReader = new FileReader(file);
                instance = gson.fromJson(fileReader, EmoggConfig.class);
                fileReader.close();
            } catch (Exception e) {
                Emogg.LOGGER.error("Failed to load emogg config: ", e);
                save();
            }
    }

    public static void save() {
        final File parentFile;

        if (!(parentFile = file.getParentFile()).exists())
            parentFile.mkdir();

        try {
            final var fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(instance));
            fileWriter.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to save emogg config: ", e);
        }
    }
}
