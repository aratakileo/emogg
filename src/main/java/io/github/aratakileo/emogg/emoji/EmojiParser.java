package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
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

    public record Section(int start, int end, String emoji, boolean escaped) { }

    public static List<Section> getEmojiSections(String text) {
        final var matcher = PATTERN.matcher(text);
        List<Section> sections = new ArrayList<>();
        while (matcher.find()) {
            sections.add(new Section(
                    matcher.start(),
                    matcher.end(),
                    matcher.group(2), // Emoji name
                    !matcher.group(1).isEmpty() // Is escaped
            ));
        }
        return sections;
    }

    private static void _parse(MutableComponent component) {
        if (component.getContents() instanceof LiteralContents literalContents) {
            final var originalText = literalContents.text();

            final var sections = getEmojiSections(originalText);

            if (sections.isEmpty()) return;

            if (EmoggConfig.instance.enableDebugMode)
                Emogg.LOGGER.debug("Parsing <"+component+">");

            originalComponents.put(component, component.copy());

            List<Component> components = new ArrayList<>();

            var stringBuilder = new StringBuilder();
            int lastEnd = 0;
            for (int i = 0; i < sections.size() + 1; i++) {
                if (i != sections.size()) {
                    var section = sections.get(i);

                    stringBuilder.append(
                            originalText,
                            lastEnd,
                            section.start()
                    );

                    if (!section.escaped()) {
                        var emoji = EmojiManager.getInstance().getEmoji(section.emoji());

                        if (emoji != null) {
                            if (!stringBuilder.isEmpty()) {
                                components.add(MutableComponent.create(new LiteralContents(stringBuilder.toString())));
                                stringBuilder.setLength(0);
                            }

                            // We only set lastEnd if the escaped or emoji is valid.
                            // Since, otherwise, the string builder should append the emoji syntax as normal text later.
                            lastEnd = section.end();

                            final var emojiComponent = MutableComponent.create(new LiteralContents(
                                    Character.toString(EmojiFontSet.idToCodePoint(emoji.getId()))
                            ));
                            emojiComponent.setStyle(Style.EMPTY.withFont(EmojiFontSet.NAME));
                            components.add(emojiComponent);
                        }
                    } else { // escaped
                        stringBuilder.append(
                                originalText,
                                section.start() + 1,
                                section.end()
                        );

                        lastEnd = section.end();
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

            if (EmoggConfig.instance.enableDebugMode)
                Emogg.LOGGER.debug("Parse result: <"+component+">");
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

    public static boolean isOnLogicalClient() {
        return RenderSystem.isOnRenderThreadOrInit();
    }

    public static @Nullable MutableComponent getOriginal(Component component) {
        //noinspection SuspiciousMethodCalls
        return originalComponents.get(component);
    }
}
