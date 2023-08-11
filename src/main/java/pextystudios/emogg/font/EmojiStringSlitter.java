package pextystudios.emogg.font;

import com.google.common.collect.Lists;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EmojiStringSlitter extends StringSplitter {
    public EmojiStringSlitter(WidthProvider widthProvider) {
        super(widthProvider);
    }

    @Override
    public void splitLines(
            FormattedText formattedText,
            int maxWidth,
            Style style,
            BiConsumer<FormattedText, Boolean> biConsumer
    ) {
        final var styledContentConsumer = new StyledContentConsumer(formattedText);
        final List<LineComponent> list = Lists.newArrayList();

        formattedText.visit((_style, string) -> {
            if (!string.isEmpty())
                list.add(new StringSplitter.LineComponent(EmojiTextProcessor.from(string).getProcessedText(), _style));

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

                    final var notProcessedFormattedText = flatComponents.splitAt(
                            splitPosition,
                            hasNewlineOrSpaceChar ? 1 : 0,
                            splitStyle
                    );

                    notProcessedFormattedText.visit(styledContentConsumer::accept, style);

                    if (hasNewlineOrSpaceChar)
                        styledContentConsumer.increaseOffset();

                    biConsumer.accept(
                            styledContentConsumer.getProcessedFormattedTextAndReset(),
                            prevContentsWereWithoutNewline
                    );

                    prevContentsWereWithoutNewline = !hasNewlineChar;
                    needToCheck = true;

                    break;
                }

                lineBreakFinder.addToOffset(lineComponent.contents.length());
            }
        }

        var notProcessedFormattedText = flatComponents.getRemainder();

        if (notProcessedFormattedText != null) {
            notProcessedFormattedText.visit(styledContentConsumer::accept, style);
            biConsumer.accept(
                    styledContentConsumer.getProcessedFormattedTextAndReset(),
                    prevContentsWereWithoutNewline
            );
            return;
        }

        if (penultimateContentsHaveNewlineChar)
            biConsumer.accept(FormattedText.EMPTY, false);
    }

    private static class StyledContentConsumer {
        private final EmojiTextProcessor emojiTextProcessor;
        private FormattedText processedFormattedText = FormattedText.EMPTY;
        private int offset = 0;

        public StyledContentConsumer(@NotNull FormattedText sourceFormattedText) {
            emojiTextProcessor = EmojiTextProcessor.from(sourceFormattedText.getString());
        }

        public Optional<Integer> accept(Style style, String string) {
            final var stringBuilder = new StringBuilder();
            var i = -1;

            for (var _ch: string.toCharArray()) {
                i++;

                final var offsetedI = offset + i;

                if (emojiTextProcessor.hasEmojiFor(offsetedI)) {
                    final var emojiLiteral = emojiTextProcessor.getEmojiLiteralFor(offsetedI);

                    stringBuilder.append(emojiLiteral.isEscaped() ? "\\:" : emojiLiteral.getEmoji().getCode());
                    continue;
                }

                stringBuilder.append(_ch);
            }

            offset += string.length();
            processedFormattedText = FormattedText.composite(
                    processedFormattedText,
                    FormattedText.of(stringBuilder.toString(), style)
            );

            return Optional.empty();
        }

        public FormattedText getProcessedFormattedTextAndReset() {
            final var processedFormattedTextOld = processedFormattedText;

            processedFormattedText = FormattedText.EMPTY;

            return processedFormattedTextOld;
        }

        public void increaseOffset() {
            offset++;
        }
    }
}
