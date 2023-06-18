package pextystudios.emogg.emoji;

import pextystudios.emogg.Emogg;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiTextBuilder {
    private final static Pattern pattern = Pattern.compile("(:([_A-Za-z0-9]+):)");
    private HashMap<Integer, Emoji> emojiIndexes;
    private String builtText;
    private int lengthDifference;

    public EmojiTextBuilder(String sourceText) {
        setSourceText(sourceText);
    }

    public void setSourceText(String sourceText) {
        this.emojiIndexes = new LinkedHashMap<>();
        this.builtText = sourceText;
        this.lengthDifference = 0;

        Matcher matcher = pattern.matcher(builtText);

        while (matcher.find()) {
            if (!Emogg.getInstance().allEmojis.containsKey(matcher.group(2)))
                continue;

            int lengthBeforeChanges = builtText.length();

            builtText = builtText.replaceFirst(matcher.group(1), "\u2603");

            emojiIndexes.put(
                    matcher.start(1) - lengthDifference,
                    Emogg.getInstance().allEmojis.get(matcher.group(2))
            );

            lengthDifference += lengthBeforeChanges - builtText.length();
        }
    }

    public HashMap<Integer, Emoji> getEmojiIndexes() {
        return emojiIndexes;
    }

    public String getBuiltText() {return builtText;}

    public int getLengthDifference() {
        return lengthDifference;
    }
}
