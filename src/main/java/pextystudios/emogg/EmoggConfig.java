package pextystudios.emogg;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class EmoggConfig {
    // Non-JSON values
    private final static File file = new File("config/emogg.json");
    private final static Gson gson = new Gson();

    public static EmoggConfig instance = new EmoggConfig();

    // JSON values
    public boolean isUsingBuiltinEmojis = true;
    public boolean isExperimentalExperienceEnabled = true;
    public int emojiSelectionMenuScrollStateValue = 0;

    public static void load() {
        if (file.exists())
            try {
                var fileReader = new FileReader(file);
                instance = gson.fromJson(fileReader, EmoggConfig.class);
                fileReader.close();
            } catch (Exception e) {
                Emogg.LOGGER.error("Failed to load emogg config: ", e);
                save();
            }
    }

    public static void save() {
        File parentFile;

        if (!(parentFile = file.getParentFile()).exists())
            parentFile.mkdir();

        try {
            var fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(instance));
            fileWriter.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to save emogg config: ", e);
        }
    }
}
