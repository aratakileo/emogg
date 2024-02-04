package io.github.aratakileo.emogg.fabric;

import io.github.aratakileo.emogg.util.ClientEnvironment;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.util.ModMenuConfigScreenGetter;
import io.github.aratakileo.emogg.util.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@ClientEnvironment
public class EmoggFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Emogg.init(new Platform() {
            @Override
            public @NotNull String getPlatformName() {
                return "fabric";
            }

            @Override
            public @Nullable String getModVersion(@NotNull String modId) {
                return FabricLoader.getInstance()
                        .getModContainer(modId)
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse(null);
            }

            @Override
            public void registerResourcesReloadListener(
                    @NotNull ResourceLocation resourceLocation,
                    @NotNull Consumer<ResourceManager> resourcesReloadListener
            ) {
                ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                        new SimpleSynchronousResourceReloadListener() {
                            @Override
                            public ResourceLocation getFabricId() {
                                return resourceLocation;
                            }

                            @Override
                            public void onResourceManagerReload(ResourceManager resourceManager) {
                                resourcesReloadListener.accept(resourceManager);
                            }
                        }
                );
            }

            @Override
            public void setConfigScreenRegistrator(
                    @NotNull Function<@Nullable Screen, @NotNull Screen> configScreenRegistrator
            ) {
                ModMenuConfigScreenGetter.configScreenGetter = configScreenRegistrator;
            }

            @Override
            public void registerBuiltinResourcePack(@NotNull String modId, @NotNull String resourcepackName) {
                ResourceManagerHelper.registerBuiltinResourcePack(
                        new ResourceLocation(modId, resourcepackName),
                        FabricLoader.getInstance().getModContainer(modId).orElseThrow(),
                        Component.translatable(String.format("emogg.resourcepack.%s.name", resourcepackName)),
                        ResourcePackActivationType.DEFAULT_ENABLED
                );
            }
        });
    }
}
