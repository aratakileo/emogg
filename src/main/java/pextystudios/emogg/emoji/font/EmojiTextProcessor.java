package pextystudios.emogg.emoji.font;

import org.jetbrains.annotations.Nullable;
import pextystudios.emogg.emoji.handler.EmojiHandler;
import pextystudios.emogg.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class EmojiTextProcessor {
    public final static EmojiTextProcessor EMPTY = new EmojiTextProcessor(null);

    private final static Pattern pattern = Pattern.compile("(\\\\?)(:([_A-Za-z0-9]+):)");
    private final static int BACKSLASH_PATTERN_GROUP = 1, EMOJI_CODE_PATTERN_GROUP = 2, EMOJI_NAME_PATTERN_GROUP = 3;

    private HashMap<Integer, EmojiRenderer> emojiRendererIndexes;
    private String processedText;
    private int lengthDifference;

    public EmojiTextProcessor(String sourceText) {
        setSourceText(sourceText);
    }

    public @Nullable EmojiRenderer getEmojiRendererFor(int charRenderIndex) {
        return emojiRendererIndexes.get(charRenderIndex);
    }

    public Collection<EmojiRenderer> getMojiRenderers() {
        return emojiRendererIndexes.values();
    }

    public boolean hasEmojiFor(int charRenderIndex) {
        return emojiRendererIndexes.containsKey(charRenderIndex);
    }

    public String getProcessedText() {
        return processedText;
    }

    public int getLengthDifference() {
        return lengthDifference;
    }

    public boolean isEmpty() {
        return processedText.isEmpty();
    }

    private void setSourceText(String sourceText) {
        if (sourceText == null)
            sourceText = "";

        emojiRendererIndexes = new LinkedHashMap<>();
        processedText = sourceText;
        lengthDifference = 0;

        if (sourceText.isEmpty())
            return;

        var matcher = pattern.matcher(processedText);

        while (matcher.find()) {
            var backslashBeforeEmojiCode = matcher.group(BACKSLASH_PATTERN_GROUP);
            var matchedEmojiName = matcher.group(EMOJI_NAME_PATTERN_GROUP);

            if (!EmojiHandler.getInstance().hasEmoji(matchedEmojiName))
                continue;

            var emoji = EmojiHandler.getInstance().getEmoji(matchedEmojiName);

            if (!backslashBeforeEmojiCode.isEmpty()) {
                emojiRendererIndexes.put(
                        matcher.start(BACKSLASH_PATTERN_GROUP) - lengthDifference,
                        emoji.getRenderer(true)
                );

                processedText = StringUtil.replaceStartEndIndex(
                        processedText,
                        matcher.start() - lengthDifference,
                        matcher.end() - lengthDifference,
                        emoji.getCode()
                );
                lengthDifference += 1;

                continue;
            }

            var lengthBeforeChanges = processedText.length();

            processedText = StringUtil.replaceStartEndIndex(
                    processedText,
                    matcher.start(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    matcher.end(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    '\u2603'
            );

            emojiRendererIndexes.put(
                    matcher.start(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    emoji.getRenderer()
            );

            lengthDifference += lengthBeforeChanges - processedText.length();
        }
    }
}
