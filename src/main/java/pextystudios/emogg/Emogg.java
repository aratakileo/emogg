package pextystudios.emogg;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.EmojiHandler;
import pextystudios.emogg.util.RenderUtil;


public class Emogg implements ClientModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    @Override
    public void onInitializeClient() {
        new EmojiHandler();

        EmoggConfig.load();
    }
}
