package io.github.aratakileo.emogg.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Platform {
    static @Nullable String getModVersion(@NotNull String modId) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse(null);
    }

    static @NotNull String getMinecraftVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    static @NotNull String getPlatformName() {
        return "fabric";
    }
}
