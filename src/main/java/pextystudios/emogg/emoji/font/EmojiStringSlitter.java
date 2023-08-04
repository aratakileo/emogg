package pextystudios.emogg.emoji.font;

import com.google.common.collect.Lists;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import pextystudios.emogg.Emogg;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EmojiStringSlitter extends StringSplitter {
    public EmojiStringSlitter(WidthProvider widthProvider) {
        super(widthProvider);
    }

    @Override
    public void splitLines(FormattedText formattedText, int maxWidth, Style style, BiConsumer<FormattedText, Boolean> biConsumer) {
        final List<LineComponent> list = Lists.newArrayList();

        formattedText.visit((_style, string) -> {
            if (!string.isEmpty()) list.add(new StringSplitter.LineComponent(string, _style));

            return Optional.empty();
        }, style);

        final var flatComponents = new StringSplitter.FlatComponents(list);
        var bl = true;
        var bl2 = false;
        var bl3 = false;

        while (bl) {
            bl = false;
            final var lineBreakFinder = new StringSplitter.LineBreakFinder((float) maxWidth);

            for (StringSplitter.LineComponent lineComponent : flatComponents.parts) {
                final var contentsEmojiTextProcessor = EmojiTextProcessor.from(lineComponent.contents);
                final var bl4 = StringDecomposer.iterateFormatted(contentsEmojiTextProcessor.getProcessedText(), 0, lineComponent.style, style, lineBreakFinder);

                if (!bl4) {
                    final var originalSplitPosition = lineBreakFinder.getSplitPosition();
                    final var splitPosition = contentsEmojiTextProcessor.hasEmojis() ? contentsEmojiTextProcessor.originalizeCharPosition(originalSplitPosition) : originalSplitPosition;
                    final var style2 = lineBreakFinder.getSplitStyle();
                    final var ch = flatComponents.charAt(splitPosition);
                    final var bl5 = ch == '\n';

                    bl2 = bl5;

                    FormattedText formattedText2 = flatComponents.splitAt(splitPosition, bl5 || ch == ' ' ? 1 : 0, style2);
                    biConsumer.accept(formattedText2, bl3);

                    bl3 = !bl5;
                    bl = true;

                    break;
                }

                lineBreakFinder.addToOffset(lineComponent.contents.length());
            }
        }

        FormattedText formattedText3 = flatComponents.getRemainder();
        if (formattedText3 != null) {
            biConsumer.accept(formattedText3, bl3);
        } else if (bl2) {
            biConsumer.accept(FormattedText.EMPTY, false);
        }
    }
}
