package io.github.aratakileo.emogg.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.EmoggConfig;
import io.github.aratakileo.emogg.util.IdentityWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class EmojiParser {
    public final static Pattern PATTERN = Pattern.compile("(\\\\?):([_A-Za-z0-9]+):");

    private static final Map<IdentityWrapper<MutableComponent>, MutableComponent> originalComponents = new WeakHashMap<>();

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

            originalComponents.put(new IdentityWrapper<>(component), component.copy());

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

            // Remember to update mayBeParseResult() when changing the structure of parsed components
            component.siblings = components;
            component.contents = ComponentContents.EMPTY;

            if (EmoggConfig.instance.enableDebugMode)
                Emogg.LOGGER.debug("Parse result: <"+component+">");
        }
    }

    // Avoid infinite recursions
    private static boolean parsing = false;

    public static void parse(MutableComponent component) {
        if (!isParsable(component)) return;
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

    /**
     * Helper method used for early-exit optimizations
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isParsable(Component component) {
        return component instanceof MutableComponent && component.getContents() instanceof LiteralContents;
    }

    /**
     * Helper method used for early-exit optimizations
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean mayBeParseResult(Component component) {
        return component instanceof MutableComponent && component.getContents() == ComponentContents.EMPTY;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOnLogicalClient() {
        return RenderSystem.isOnRenderThreadOrInit();
    }

    private static final IdentityWrapper.Mutable<MutableComponent> queryingIdentityWrapper = new IdentityWrapper.Mutable<>();
    public static @Nullable MutableComponent getOriginal(Component component) {
        if (!mayBeParseResult(component)) return null;
        queryingIdentityWrapper.mutValue = (MutableComponent) component;
        return originalComponents.get(queryingIdentityWrapper);
    }

    // Mixin helpers

    /**
     * Helper function for mixins.
     * <p>
     * Try to find the original (pre-parse) version of the component.
     * If one is found, execute the operation with the original component,
     * set the injected function's return value to the result of the operation,
     * and cancel the injected function.
     * <p>
     * This also detects the current thread, so that it does not alter things on the server thread.
     */
    public static <T> void mixinApplyUsingOriginal(Component component,
                                                   CallbackInfoReturnable<T> cir,
                                                   Function<Component, T> operation,
                                                   String debugLogPrefix) {
        if (!EmojiParser.isOnLogicalClient()) return;
//        System.out.println(component);
        final var original = EmojiParser.getOriginal(component);
        if (original != null) {
            if (EmoggConfig.instance.enableDebugMode)
                Emogg.LOGGER.info(debugLogPrefix+"original:<{}> transformed:<{}>", original, component);
            cir.cancel();
            cir.setReturnValue(operation.apply(original));
        }
    }

    /**
     * Used by {@link io.github.aratakileo.emogg.mixin.component.ComponentMixin}
     * <p>
     * To indicate if we are in a {@link Component#getString()} or {@link Component#getString(int)} method,
     * we always use the original components in this method and the methods it calls
     * ({@link Component#visit(FormattedText.ContentConsumer)} and {@link Component#visit(FormattedText.StyledContentConsumer, Style)}).
     */
    public static boolean isInGetString = false;
}
