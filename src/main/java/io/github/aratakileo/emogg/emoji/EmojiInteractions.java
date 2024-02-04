package io.github.aratakileo.emogg.emoji;

import com.google.gson.JsonObject;
import io.github.aratakileo.emogg.Emogg;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

public class EmojiInteractions {
    public static class EmojiHoverEvent extends HoverEvent {
        private final Emoji emoji;

        private EmojiHoverEvent(Emoji emoji) {
            super(null, null);
            this.emoji = emoji;
        }

        private static final WeakHashMap<Emoji, EmojiHoverEvent> cache = new WeakHashMap<>();
        public static EmojiHoverEvent of(Emoji emoji) {
            return cache.computeIfAbsent(emoji, EmojiHoverEvent::new);
        }

        @Override
        public @NotNull Action<?> getAction() {
            return Action.SHOW_TEXT;
        }

        @Nullable
        public <T> T getValue(Action<T> action) {
            if (action != Action.SHOW_TEXT) return null;
            var component = Component.empty();
            switch (emoji.getState()) {
                case LOADING:
                    component.append(Component.translatable("emogg.emoji.interaction.loading"));
                    component.append(".".repeat((int) (Util.getMillis() / 200 % 4)));
                    component.append("\n");
                    break;
                case ERROR:
                    component.append(Component
                            .translatable("emogg.emoji.interaction.error", emoji.getLoadError())
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                    component.append("\n");
                    break;
            }
            component.append(Component.translatable("emogg.emoji.interaction.copy", emoji.getCode()));
            //noinspection unchecked
            return (T) component;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof EmojiHoverEvent emojiHoverEvent)
                return emojiHoverEvent.emoji.equals(emoji);
            return false;
        }

        @Override
        public @NotNull String toString() {
            return "EmojiHoverEvent{" +
                    "emoji=" + emoji +
                    '}';
        }

        @Override
        public int hashCode() {
            return emoji.hashCode();
        }

        @Override
        public @NotNull JsonObject serialize() {
            Emogg.LOGGER.warn("EmojiHoverEvent shouldn't be serialized!");
            return new JsonObject();
        }

    }

    public static class EmojiClickEvent extends ClickEvent {
        private final Emoji emoji;

        private EmojiClickEvent(Emoji emoji) {
            super(null, null);
            this.emoji = emoji;
        }

        private static final WeakHashMap<Emoji, EmojiClickEvent> cache = new WeakHashMap<>();
        public static EmojiClickEvent of(Emoji emoji) {
            return cache.computeIfAbsent(emoji, EmojiClickEvent::new);
        }

        @Override
        public @NotNull Action getAction() {
            return Action.COPY_TO_CLIPBOARD;
        }

        @Override
        public @NotNull String getValue() {
            return emoji.getCode();
        }

        @Override
        public boolean equals(Object object) {
            return super.equals(object);
        }

        @Override
        public @NotNull String toString() {
            return "EmojiClickEvent{" +
                    "emoji=" + emoji +
                    '}';
        }

        @Override
        public int hashCode() {
            return emoji.hashCode();
        }
    }
}
