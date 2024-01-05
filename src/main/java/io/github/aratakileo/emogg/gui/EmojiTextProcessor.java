package io.github.aratakileo.emogg.gui;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EmojiTextProcessor {
    public final static EmojiTextProcessor EMPTY = new EmojiTextProcessor(null);

    private static final LoadingCache<@NotNull String, @NotNull EmojiTextProcessor> EMOJI_TEXT_PROCESSORS_BUFFER
            = CacheBuilder
            .newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull EmojiTextProcessor load(@NotNull String key) {
                    return new EmojiTextProcessor(key);
                }
            });

    private final static int BACKSLASH_PATTERN_GROUP = 1,
            EMOJI_CODE_PATTERN_GROUP = 2,
            EMOJI_NAME_PATTERN_GROUP = 3;

    private final HashMap<@NotNull Integer, @NotNull EmojiLiteral> emojiRenderIndexes = new LinkedHashMap<>();
    private final String processedText;

    private EmojiTextProcessor(@Nullable String sourceText) {
        this.processedText = getSourceText(sourceText);
    }

    public @Nullable EmojiLiteral getEmojiLiteralFor(int renderCharPosition) {
        return emojiRenderIndexes.get(renderCharPosition);
    }

    public @NotNull Collection<@NotNull EmojiLiteral> getEmojiLiterals() {
        return emojiRenderIndexes.values();
    }

    public boolean hasEmojiFor(int renderCharPosition) {
        return emojiRenderIndexes.containsKey(renderCharPosition);
    }

    public int emojisCount() {
        return emojiRenderIndexes.size();
    }

    public @NotNull String getProcessedText() {
        return processedText;
    }

    public boolean isEmpty() {
        return processedText.isEmpty();
    }

    private @NotNull String getSourceText(@Nullable String sourceText) {
        if (Strings.isNullOrEmpty(sourceText))
            return "";

        var processedText = sourceText;

        final var matcher = EmojiLiteral.EMOJI_LITERAL_PATTERN.matcher(processedText);
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
                        new EmojiLiteral(emoji, true)
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
                    EmojiLiteral.DUMMY_CHAR
            );

            emojiRenderIndexes.put(
                    emojiCodeStart - lengthDifference,
                    new EmojiLiteral(emoji, false)
            );

            lengthDifference += lengthBeforeChanges - processedText.length();
        }

        return processedText;
    }

    public static @NotNull EmojiTextProcessor from(@Nullable String text) {
        if (Strings.isNullOrEmpty(text)) return EmojiTextProcessor.EMPTY;

        try {
            return EMOJI_TEXT_PROCESSORS_BUFFER.get(text);
        } catch (ExecutionException e) {
            return new EmojiTextProcessor(text);
        }
    }

    public static @NotNull String processText(String text) {
        return from(text).processedText;
    }
}
