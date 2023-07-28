package pextystudios.emogg.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.EmojiHandler;

public class EmoggFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Emogg.init();

        EmojiHandler.getInstance().registerReloadListeners((id, listener) -> ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                listener.accept(resourceManager);
            }
        }));
    }
}
