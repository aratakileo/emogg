package pextystudios.emogg.handler;

import com.google.gson.Gson;
import pextystudios.emogg.Emogg;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigHandler {
    private final static File file = new File("config/emogg.json");
    private final static Gson gson = new Gson();
    
    public static Config data = new Config();

    public static class Config {
        public boolean useBuiltinEmojiEnabled = true;
        public boolean isExperimentalExperienceEnabled = true;
    }

    public static void load() {
        if (file.exists())
            try {
                var fileReader = new FileReader(file);
                data = gson.fromJson(fileReader, Config.class);
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
            fileWriter.write(gson.toJson(data));
            fileWriter.close();
        } catch (Exception e) {
            Emogg.LOGGER.error("Failed to save emogg config: ", e);
        }
    }
}
