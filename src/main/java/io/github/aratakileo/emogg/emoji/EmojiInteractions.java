package io.github.aratakileo.emogg.emoji;

import com.google.gson.JsonObject;
import io.github.aratakileo.emogg.Emogg;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.WeakHashMap;

public class EmojiInteractions {
    public static class EmojiHoverEvent extends HoverEvent {
        private final Emoji emoji;

        private EmojiHoverEvent(@NotNull Emoji emoji) {
            super(Action.SHOW_TEXT, null);
            this.emoji = emoji;
        }

        private static final WeakHashMap<Emoji, EmojiHoverEvent> cache = new WeakHashMap<>();
        public static @NotNull EmojiHoverEvent of(@NotNull Emoji emoji) {
            return cache.computeIfAbsent(emoji, EmojiHoverEvent::new);
        }

        @SuppressWarnings("unchecked")
        public <T> @Nullable T getValue(@NotNull Action<T> action) {
            if (action != Action.SHOW_TEXT) return null;

            var component = Component.empty();

            switch (emoji.getState()) {
                case LOADING -> {
                    component.append(Component.translatable("emogg.emoji.interaction.loading"));
                    component.append(".".repeat((int) (Util.getMillis() / 200 % 4)));
                    component.append("\n");
                }
                case ERROR -> {
                    component.append(Component
                            .translatable("emogg.emoji.interaction.error", emoji.getLoadError())
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                    component.append("\n");
                }
            }

            component.append(Component.translatable("emogg.emoji.interaction.copy", emoji.getCode()));

            return (T) component;
        }

        @Override
        public boolean equals(@NotNull Object object) {
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
    }

    public static class EmojiClickEvent extends ClickEvent {
        private final Emoji emoji;

        private EmojiClickEvent(@NotNull Emoji emoji) {
            super(null, null);
            this.emoji = emoji;
        }

        private static final WeakHashMap<Emoji, EmojiClickEvent> cache = new WeakHashMap<>();
        public static @NotNull EmojiClickEvent of(@NotNull Emoji emoji) {
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
        public boolean equals(@NotNull Object object) {
            return object instanceof EmojiClickEvent emojiClickEvent && emojiClickEvent.emoji.equals(emoji);
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
