package io.github.aratakileo.emogg.emoji;

import com.mojang.datafixers.util.Pair;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.emoji.EmojiFontSet;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class EmojiParser {
    public final static Pattern PATTERN = Pattern.compile("(\\\\?):([_A-Za-z0-9]+):");

    private static final WeakHashMap<MutableComponent, MutableComponent> originalComponents = new WeakHashMap<>();

    private static void _parse(MutableComponent component) {
        if (component.getContents() instanceof LiteralContents literalContents) {
            final var originalText = literalContents.text();
            final var matcher = PATTERN.matcher(originalText);

            List<Pair<Pair<Integer, Integer>, Emoji>> sections = new ArrayList<>();

            while (matcher.find()) {
                final var slash = matcher.group(1);
                final var name = matcher.group(2);
                final var section = Pair.of(matcher.start(), matcher.end());
                if (!slash.isEmpty()) {
                    sections.add(Pair.of(section, null));
                } else {
                    final var emoji = EmojiManager.getInstance().getEmoji(name);
                    if (emoji != null) {
                        sections.add(Pair.of(section, emoji));
                    }
                }
            }

            if (sections.isEmpty()) return;

            if (EmoggConfig.instance.isDebugModeEnabled)
                Emogg.LOGGER.info("Parsing <"+component+">");

            originalComponents.put(component, component.copy());

            List<Component> components = new ArrayList<>();

            var stringBuilder = new StringBuilder();

            int lastEnd = 0;
            for (int i = 0; i < sections.size() + 1; i++) {
                if (i != sections.size()) {
                    var pair = sections.get(i);
                    var section = pair.getFirst();
                    var emoji = pair.getSecond();

                    stringBuilder.append(
                            originalText,
                            lastEnd,
                            section.getFirst()
                    );
                    lastEnd = section.getSecond();

                    if (emoji != null) {
                        if (!stringBuilder.isEmpty()) {
                            components.add(MutableComponent.create(new LiteralContents(stringBuilder.toString())));
                            stringBuilder.setLength(0);
                        }

                        final var emojiComponent = MutableComponent.create(new LiteralContents(
                                Character.toString(EmojiFontSet.idToCodePoint(emoji.getId()))
                        ));
                        emojiComponent.setStyle(Style.EMPTY.withFont(EmojiFontSet.NAME));
                        components.add(emojiComponent);
                    } else { // escaped
                        stringBuilder.append(
                                originalText,
                                section.getFirst() + 1,
                                section.getSecond()
                        );
                    }
                } else { // Ending
                    stringBuilder.append(
                            originalText,
                            lastEnd,
                            originalText.length()
                    );
                }
            }
            components.add(MutableComponent.create(new LiteralContents(stringBuilder.toString())));

            // Add old extra components back
            components.addAll(component.getSiblings());

            component.siblings = components;
            component.contents = LiteralContents.EMPTY;

            if (EmoggConfig.instance.isDebugModeEnabled)
                Emogg.LOGGER.info("Parse result: <"+component+">");
        }
    }

    // Avoid infinite recursions
    private static boolean parsing = false;

    public static void parse(MutableComponent component) {
        try {
            if (!parsing) {
                parsing = true;
                _parse(component);
                parsing = false;
            }
        } catch (Exception e) {
            parsing = false;
            Emogg.LOGGER.warn("Failed to parse component <"+component+">", e);
        }
    }

    public static @Nullable MutableComponent getOriginal(Component component) {
        //noinspection SuspiciousMethodCalls
        return originalComponents.get(component);
    }
}
