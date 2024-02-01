package io.github.aratakileo.emogg;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.aratakileo.emogg.emoji.FueController;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class EmoggConfig {
    // Non-JSON values
    private final static File file = new File("config/emogg.json");
    private final static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    public static @NotNull EmoggConfig instance = new EmoggConfig();

    // JSON values
    public boolean isDebugModeEnabled = false;
    public @NotNull ArrayList<FueController.EmojiStatistic> frequentlyUsedEmojis = new ArrayList<>();
    public @NotNull ArrayList<String> hiddenCategoryNames = new ArrayList<>();
    public boolean useCustomShaders = true;

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
