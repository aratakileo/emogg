package io.github.aratakileo.emogg.util;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Platform {
    @NotNull String getPlatformName();

    @Nullable String getModVersion(@NotNull String modId);

    default @NotNull String getMinecraftVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    void registerResourcesReloadListener(
            @NotNull ResourceLocation resourceLocation,
            @NotNull Consumer<ResourceManager> resourcesReloadListener
    );

    void setConfigScreenRegistrator(@NotNull Function<@Nullable Screen, @NotNull Screen> configScreenRegistrator);

    void registerBuiltinResourcePack(@NotNull String modId, @NotNull String resourcepackName);
}
