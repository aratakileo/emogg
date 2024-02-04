package io.github.aratakileo.emogg.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

public interface ResourcePackHelper {
    static void registerResourcePack(
            @NotNull ResourceLocation resourceLocation,
            @NotNull String modId,
            @NotNull Component displayName,
            boolean isActivatedByDefault
    ) {
        final var resourcePath = ModList.get()
                .getModFileById(modId)
                .getFile()
                .findResource("resourcepacks/" + resourceLocation.getPath());

        final var resourcePack = Pack.readMetaAndCreate(
                resourceLocation.toString(),
                displayName,
                isActivatedByDefault,
                path -> new PathPackResources(path, resourcePath, false),
                PackType.CLIENT_RESOURCES,
                Pack.Position.BOTTOM,
                PackSource.BUILT_IN
        );

        FMLJavaModLoadingContext.get()
                .getModEventBus()
                .<AddPackFindersEvent>addListener(
                        event -> event.addRepositorySource(packConsumer -> packConsumer.accept(resourcePack))
                );
    }
}
