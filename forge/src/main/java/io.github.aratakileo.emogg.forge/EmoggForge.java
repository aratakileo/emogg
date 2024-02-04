package io.github.aratakileo.emogg.forge;

import io.github.aratakileo.emogg.util.ClientEnvironment;
import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.util.Platform;
import io.github.aratakileo.emogg.util.ResourcePackHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@Mod("emogg")
@ClientEnvironment
public class EmoggForge {
    public EmoggForge() {
        Emogg.init(new Platform() {
            @Override
            public @NotNull String getPlatformName() {
                        return "forge";
                    }

            @Override
            public @Nullable String getModVersion(@NotNull String modId) {
                return ModList.get().getModFileById(modId).versionString();
            }

            @Override
            public void registerResourcesReloadListener(
                    @NotNull ResourceLocation resourceLocation,
                    @NotNull Consumer<ResourceManager> resourcesReloadListener
            ) {
                FMLJavaModLoadingContext.get()
                        .getModEventBus()
                        .<RegisterClientReloadListenersEvent>addListener(
                                event -> event.registerReloadListener(
                                        (ResourceManagerReloadListener) resourcesReloadListener
                                )
                        );
            }

            @Override
            public void setConfigScreenRegistrator(
                    @NotNull Function<@Nullable Screen,
                            @NotNull Screen> configScreenRegistrator
            ) {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, parent) -> configScreenRegistrator.apply(parent)
                        )
                );
            }

            @Override
            public void registerBuiltinResourcePack(@NotNull String modId, @NotNull String resourcepackName) {
                ResourcePackHelper.registerResourcePack(
                        new ResourceLocation(modId, resourcepackName),
                        modId,
                        Component.translatable(String.format("emogg.resourcepack.%s.name", resourcepackName)),
                        true
                );
            }
        });
    }
}
