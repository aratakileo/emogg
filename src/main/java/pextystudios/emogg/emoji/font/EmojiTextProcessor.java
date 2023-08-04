package pextystudios.emogg.emoji.font;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pextystudios.emogg.emoji.handler.EmojiHandler;
import pextystudios.emogg.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class EmojiTextProcessor {
    public final static EmojiTextProcessor EMPTY = new EmojiTextProcessor(null);

    private static final LoadingCache<String, EmojiTextProcessor> EMOJI_TEXT_PROCESSORS_BUFFER = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull EmojiTextProcessor load(@NotNull String key) {
                    return new EmojiTextProcessor(key);
                }
            });
    private final static Pattern EMOJI_CODE_PATTERN = Pattern.compile("(\\\\?)(:([_A-Za-z0-9]+):)");
    private final static int BACKSLASH_PATTERN_GROUP = 1,
            EMOJI_CODE_PATTERN_GROUP = 2,
            EMOJI_NAME_PATTERN_GROUP = 3;

    private HashMap<Integer, EmojiLiteral> emojiRenderIndexes;
    private String processedText;

    public EmojiTextProcessor(String sourceText) {
        setSourceText(sourceText);
    }

    public @Nullable EmojiLiteral getEmojiLiteralFor(int renderCharPosition) {
        return emojiRenderIndexes.get(renderCharPosition);
    }

    public Collection<EmojiLiteral> getEmojiLiterals() {
        return emojiRenderIndexes.values();
    }

    public boolean hasEmojiFor(int renderCharPosition) {
        return emojiRenderIndexes.containsKey(renderCharPosition);
    }

    public boolean hasEmojis() {
        return !emojiRenderIndexes.isEmpty();
    }

    public int originalizeCharPosition(int renderCharPosition) {
        for (var emojiRenderPosition: emojiRenderIndexes.keySet()) {
            if (renderCharPosition <= emojiRenderPosition) continue;

            final var leftNearestEmojiLiteral = emojiRenderIndexes.get(emojiRenderPosition);

            return leftNearestEmojiLiteral.originalPosition() + leftNearestEmojiLiteral.emoji().getCode().length() + (
                    renderCharPosition - emojiRenderPosition - (leftNearestEmojiLiteral.isEscaped() ? 0 : 1)
            );
        }

        return -1;
    }

    public String getProcessedText() {
        return processedText;
    }

    public boolean isEmpty() {
        return processedText.isEmpty();
    }

    private void setSourceText(String sourceText) {
        if (sourceText == null)
            sourceText = "";

        emojiRenderIndexes = new LinkedHashMap<>();
        processedText = sourceText;

        if (sourceText.isEmpty())
            return;

        final var matcher = EMOJI_CODE_PATTERN.matcher(processedText);
        var lengthDifference = 0;

        while (matcher.find()) {
            final var backslashBeforeEmojiCode = matcher.group(BACKSLASH_PATTERN_GROUP);
            final var matchedEmojiName = matcher.group(EMOJI_NAME_PATTERN_GROUP);

            if (!EmojiHandler.getInstance().hasEmoji(matchedEmojiName))
                continue;

            final var emoji = EmojiHandler.getInstance().getEmoji(matchedEmojiName);

            if (!backslashBeforeEmojiCode.isEmpty()) {
                emojiRenderIndexes.put(
                        matcher.start(BACKSLASH_PATTERN_GROUP) - lengthDifference,
                        new EmojiLiteral(emoji, matcher.start(BACKSLASH_PATTERN_GROUP), true)
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

            final var emojiCodeStart = matcher.start(EMOJI_CODE_PATTERN_GROUP);
            final var lengthBeforeChanges = processedText.length();

            processedText = StringUtil.replaceStartEndIndex(
                    processedText,
                    emojiCodeStart - lengthDifference,
                    matcher.end(EMOJI_CODE_PATTERN_GROUP) - lengthDifference,
                    '\u2603'
            );

            emojiRenderIndexes.put(
                    emojiCodeStart - lengthDifference,
                    new EmojiLiteral(emoji, emojiCodeStart, false)
            );

            lengthDifference += lengthBeforeChanges - processedText.length();
        }
    }

    public static EmojiTextProcessor from(String text) {
        if (Strings.isNullOrEmpty(text)) return EmojiTextProcessor.EMPTY;

        try {
            return EMOJI_TEXT_PROCESSORS_BUFFER.get(text);
        } catch (ExecutionException e) {
            return new EmojiTextProcessor(text);
        }
    }
}
