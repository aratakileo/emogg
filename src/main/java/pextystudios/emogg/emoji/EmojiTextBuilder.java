package pextystudios.emogg.emoji;

import org.jetbrains.annotations.Nullable;
import pextystudios.emogg.emoji.resource.Emoji;
import pextystudios.emogg.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class EmojiTextBuilder {
    public final static EmojiTextBuilder EMPTY = new EmojiTextBuilder(null);

    private final static Pattern pattern = Pattern.compile("(\\\\?)(:([_A-Za-z0-9]+):)");
    private final static int BACKSLASH_PATTERN_GROUP = 1, EMOJI_CODE_PATTERN_GROUP = 2, EMOJI_NAME_PATTERN_GROUP = 3;

    private HashMap<Integer, EmojiContainer> emojiIndexes;
    private String builtText;
    private int lengthDifference;

    public EmojiTextBuilder(String sourceText) {
        setSourceText(sourceText);
    }

    public @Nullable EmojiContainer getEmojiContainerFor(int charRenderIndex) {
        return emojiIndexes.get(charRenderIndex);
    }

    public boolean hasEmojiFor(int charRenderIndex) {
        return emojiIndexes.containsKey(charRenderIndex);
    }

    public String getBuiltText() {
        return builtText;
    }

    public int getLengthDifference() {
        return lengthDifference;
    }

    public boolean isEmpty() {
        return builtText.isEmpty();
    }

    private void setSourceText(String sourceText) {
        if (sourceText == null)
            sourceText = "";

        emojiIndexes = new LinkedHashMap<>();
        builtText = sourceText;
        lengthDifference = 0;

        if (sourceText.isEmpty())
            return;

        var matcher = pattern.matcher(builtText);

        while (matcher.find()) {
            var backslashBeforeEmojiCode = matcher.group(BACKSLASH_PATTERN_GROUP);
            var matchedEmojiName = matcher.group(EMOJI_NAME_PATTERN_GROUP);

            if (!EmojiHandler.getInstance().hasEmoji(matchedEmojiName))
                continue;

            var emoji = EmojiHandler.getInstance().getEmoji(matchedEmojiName);

            if (!backslashBeforeEmojiCode.isEmpty()) {
                emojiIndexes.put(
                        matcher.start(BACKSLASH_PATTERN_GROUP) - lengthDifference,
                        new EmojiContainer(emoji, true)
                );

                builtText = StringUtil.replaceStartEndIndex(
                        builtText,
                        matcher.start() - lengthDifference,
                        matcher.end() - lengthDifference,
                        emoji.getCode()
                );
                lengthDifference += 1;

                continue;
            }

            var lengthBeforeChanges = builtText.length();

            builtText = StringUtil.replaceStartEndIndex(
                    builtText,
                    matcher.start(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    matcher.end(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    '\u2603'
            );

            emojiIndexes.put(
                    matcher.start(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    new EmojiContainer(emoji, false)
            );

            lengthDifference += lengthBeforeChanges - builtText.length();
        }
    }

    public record EmojiContainer(Emoji emoji, boolean isEscaped) {}
}
