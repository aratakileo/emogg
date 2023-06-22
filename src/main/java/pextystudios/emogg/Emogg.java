package pextystudios.emogg;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.handler.EmojiHandler;
import pextystudios.emogg.handler.ConfigHandler;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    @Override
    public void onInitializeClient() {
        new EmojiHandler();

        ConfigHandler.load();
    }
}
