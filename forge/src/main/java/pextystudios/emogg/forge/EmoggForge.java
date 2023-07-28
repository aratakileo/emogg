package pextystudios.emogg.forge;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.emoji.EmojiHandler;
import pextystudios.emogg.gui.screen.SettingsScreen;

@Mod(Emogg.NAMESPACE)
@OnlyIn(Dist.CLIENT)
public class EmoggForge {
    public EmoggForge() {
        Emogg.init();

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EmojiHandler.getInstance().registerReloadListeners((id, consumer) ->
                modEventBus.<RegisterClientReloadListenersEvent>addListener(e ->
                        e.registerReloadListener((ResourceManagerReloadListener) consumer::accept)));

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) ->
                        new SettingsScreen(parent))
        );
    }
}
