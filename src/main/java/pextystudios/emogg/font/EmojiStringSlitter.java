package pextystudios.emogg.font;

import com.google.common.collect.Lists;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EmojiStringSlitter extends StringSplitter {
    public EmojiStringSlitter(WidthProvider widthProvider) {
        super(widthProvider);
    }

    @Override
    public void splitLines(FormattedText formattedText, int maxWidth, Style style, BiConsumer<FormattedText, Boolean> biConsumer) {
        final var textOriginalizer = new TextOriginalizer(formattedText, biConsumer);
        final List<LineComponent> list = Lists.newArrayList();

        formattedText.visit((_style, string) -> {
            if (!string.isEmpty()) list.add(new StringSplitter.LineComponent(EmojiTextProcessor.from(string).getProcessedText(), _style));

            return Optional.empty();
        }, style);

        final var flatComponents = new StringSplitter.FlatComponents(list);
        var needToCheck = true;
        var penultimateContentsHaveNewlineChar = false;
        var prevContentsWereWithoutNewline = false;

        while (needToCheck) {
            needToCheck = false;
            final var lineBreakFinder = new StringSplitter.LineBreakFinder((float) maxWidth);

            for (StringSplitter.LineComponent lineComponent : flatComponents.parts) {
                if (!StringDecomposer.iterateFormatted(
                        lineComponent.contents,
                        0,
                        lineComponent.style,
                        style,
                        lineBreakFinder
                )) {
                    final var splitPosition = lineBreakFinder.getSplitPosition();
                    final var splitStyle = lineBreakFinder.getSplitStyle();
                    final var ch = flatComponents.charAt(splitPosition);
                    final var hasNewlineChar = ch == '\n';
                    final var hasNewlineOrSpaceChar = hasNewlineChar || ch == ' ';

                    penultimateContentsHaveNewlineChar = hasNewlineChar;

                    final var preprocessedFormattedText = flatComponents.splitAt(splitPosition, hasNewlineOrSpaceChar ? 1 : 0, splitStyle);
                    textOriginalizer.originalizeAndAccept(preprocessedFormattedText, splitStyle, prevContentsWereWithoutNewline);

                    if (hasNewlineOrSpaceChar) textOriginalizer.increaseRenderPositionOffset();

                    prevContentsWereWithoutNewline = !hasNewlineChar;
                    needToCheck = true;

                    break;
                }

                lineBreakFinder.addToOffset(lineComponent.contents.length());
            }
        }

        final var finalFormattedText = flatComponents.getRemainder();

        if (finalFormattedText == null && penultimateContentsHaveNewlineChar) {
            biConsumer.accept(FormattedText.EMPTY, false);
            return;
        }

        textOriginalizer.originalizeAndAccept(finalFormattedText, style, prevContentsWereWithoutNewline);
    }

    private static class TextOriginalizer {
        private final EmojiTextProcessor emojiTextProcessor;
        private final BiConsumer<FormattedText, Boolean> biConsumer;
        private final String sourceText;

        private int renderPositionOffset = 0;

        public TextOriginalizer(FormattedText sourceFormattedText, BiConsumer<FormattedText, Boolean> biConsumer) {
            this.sourceText = sourceFormattedText.getString();
            this.emojiTextProcessor = EmojiTextProcessor.from(sourceText);
            this.biConsumer = biConsumer;
        }

        public void increaseRenderPositionOffset() {
            renderPositionOffset++;
        }

        public void originalizeAndAccept(FormattedText formattedtext, Style style, boolean prevContentsWereWithoutNewline) {
            if (formattedtext == null) {
                biConsumer.accept(FormattedText.EMPTY, false);
                return;
            }

            final var preprocessedText = formattedtext.getString();

            if (preprocessedText.equals(sourceText)) {
                biConsumer.accept(FormattedText.of(sourceText, style), prevContentsWereWithoutNewline);
                return;
            }

            var processedText = preprocessedText;
            var localOffset = 0;

            for (var i = 0; i < preprocessedText.length(); i++) {
                if (!emojiTextProcessor.hasEmojiFor(renderPositionOffset + i)) continue;

                final var currentEmojiLiteral = emojiTextProcessor.getEmojiLiteralFor(renderPositionOffset + i);
                final var emojiStart = localOffset + i;

                if (currentEmojiLiteral.isEscaped()) {
                    processedText = processedText.substring(0, emojiStart) + '\\' + processedText.substring(emojiStart);
                    localOffset += 1;
                } else {
                    final var currentEmojiString = currentEmojiLiteral.emoji().getCode();
                    processedText = processedText.substring(0, emojiStart) + currentEmojiString + processedText.substring(emojiStart + 1);
                    localOffset += currentEmojiString.length() - 1;
                }
            }

            biConsumer.accept(FormattedText.of(processedText, style), prevContentsWereWithoutNewline);
            renderPositionOffset += preprocessedText.length();
        }
    }
}
