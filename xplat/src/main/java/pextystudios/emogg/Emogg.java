package pextystudios.emogg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pextystudios.emogg.emoji.EmojiHandler;


public class Emogg {
    public static Logger LOGGER = LoggerFactory.getLogger(Emogg.class);

    public static final String NAMESPACE = "emogg";

    public static void init() {
        new EmojiHandler();

        EmoggConfig.load();
    }
}
