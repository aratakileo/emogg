package io.github.aratakileo.emogg.emoji;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.util.EmojiUtil;
import io.github.aratakileo.emogg.util.NativeGifImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class Emoji {
    private final int id;
    private final String name;
    private final String category;

    private final EmojiLoader loader;
    private Future<? extends EmojiGlyphProvider> loadingFuture = null;
    private EmojiGlyphProvider glyphProvider = null;
    private String loadError = null;
    private State state = State.INACTIVE;

    private Emoji(int id,
                    @NotNull String name,
                    @NotNull String category,
                    @NotNull EmojiLoader loader) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.loader = loader;
    }

    public int getId() {return id;}

    public @NotNull String getName() {return name;}

    public @NotNull String getCategory() {return category;}

    public @NotNull String getCode() {
        return ":" + name + ':';
    }

    public @NotNull String getEscapedCode() {return '\\' + getCode();}

//    public @NotNull ResourceLocation getResourceLocation() {return resourceLocation;}

    private static Supplier<EmojiGlyph> GLYPH_LOADING = () -> {
        EmojiGlyph glyph = new EmojiGlyph.Loading();
        GLYPH_LOADING = () -> glyph;
        return glyph;
    };

    public @NotNull EmojiGlyph getGlyph() {
        updateLoadingState();

        var glyph = switch (state) {
            case LOADING -> GLYPH_LOADING.get();
            case ERROR -> EmojiGlyph.ERROR;
            case ACTIVE -> glyphProvider.getGlyph();
            case INACTIVE -> {
                Emogg.LOGGER.warn("Emoji "+this+" shouldn't be inactive when rendering!");
                yield EmojiGlyph.EMPTY;
            }
        };
        if (glyph == null) {
            Emogg.LOGGER.warn("Failed to get glyph for emoji "+this+": null");
            return EmojiGlyph.ERROR;
        }
        return glyph;
    }

    private void updateLoadingState() {
        if (state == State.INACTIVE) {
            loadingFuture = loader.load();
            state = State.LOADING;
        }

        // We might transition from INACTIVE to ACTIVE in one go
        if (state == State.LOADING) {
            if (loadingFuture.isDone()) {
                try {
                    glyphProvider = loadingFuture.get();
                    state = State.ACTIVE;
                } catch (ExecutionException e) {
                    Emogg.LOGGER.warn("Emoji "+name+" loading failed!", e.getCause());
                    loadError = e.getCause().toString();
                    state = State.ERROR;
                } catch (InterruptedException | CancellationException e) {
                    Emogg.LOGGER.warn("Emoji "+name+" loading failed!", e);
                    loadError = e.toString();
                    state = State.ERROR;
                }

            }
        }
    }

    public void reload(boolean immediately) {
        state = State.INACTIVE;
        if (loadingFuture != null && !loadingFuture.isDone()) loadingFuture.cancel(true);
        loadingFuture = null;
        glyphProvider = null;
        loadError = null;
        if (immediately) updateLoadingState();
    }

    public static Emoji fromResource(int id, @NotNull String name, @NotNull ResourceLocation resourceLocation) {
        name = EmojiUtil.normalizeNameOrCategory(name);

        var category = resourceLocation.getPath().substring(EmojiUtil.EMOJI_FOLDER_NAME.length() + 1);

        if (category.contains("/")) {
            var splitPath = category.split("/");

            category = splitPath[splitPath.length - 2];
        } else category = EmojiManager.CATEGORY_DEFAULT;

        category = EmojiUtil.normalizeNameOrCategory(category);

        EmojiLoader loader;
        if (!resourceLocation.getPath().endsWith(NativeGifImage.GIF_EXTENSION)) {
            loader = () -> EmojiLoader.staticImageLoader(EmojiLoader.resourceReader(resourceLocation));
        } else {
            loader = () -> EmojiLoader.gifLoader(EmojiLoader.resourceReader(resourceLocation));
        }

        return new Emoji(id, name, category, loader);
    }

    private enum State {
        INACTIVE,
        LOADING,
        ACTIVE,
        ERROR
    }
}