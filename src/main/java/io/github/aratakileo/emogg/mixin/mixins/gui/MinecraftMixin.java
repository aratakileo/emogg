package io.github.aratakileo.emogg.mixin.mixins.gui;

import io.github.aratakileo.emogg.Emogg;
import io.github.aratakileo.emogg.api.ModrinthApi;
import io.github.aratakileo.emogg.mixin.MixinHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.Charset;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique
    private final static ResourceLocation messageResourceLocation = new ResourceLocation(
            Emogg.NAMESPACE_OR_ID,
            "rawtext/new_version_is_available.json"
    );

    @Shadow
    public ClientLevel level;

    @Shadow @Final
    public Gui gui;


    @Inject(method = "setScreen", at = @At("HEAD"))
    private void setScreen(Screen screen, CallbackInfo ci) {
        if (
                screen == null
                        && level != null
                        && !MixinHelpers.hasMessageAboutUpdateBeenShown
                        && ModrinthApi.getResponseCode() == ModrinthApi.ResponseCode.NEEDS_TO_BE_UPDATED
        ) {
            try {
                final var lang = Language.getInstance();
                final var rightMessageParts = lang.getOrDefault("emogg.message.new_version_is_available")
                        .split("%s");
                final var messageRawtext = IOUtils.toString(
                        Minecraft.getInstance()
                                .getResourceManager()
                                .getResource(messageResourceLocation)
                                .get()
                                .open(),
                                Charset.defaultCharset()
                ).replaceAll("\\{link}", ModrinthApi.getLinkForUpdate())
                        .replaceAll("\\{tooltip}", lang.getOrDefault("chat.link.open"))
                        .replace("{left}", rightMessageParts[0])
                        .replace("{right}", rightMessageParts.length > 1 ? rightMessageParts[1] : "")
                        .replace("{button}", "Modrinth");

                gui.getChat().addMessage(Component.Serializer.fromJson(messageRawtext));

                MixinHelpers.hasMessageAboutUpdateBeenShown = true;
            } catch (Exception e) {
                gui.getChat().addMessage(
                        Component.literal("Emogg: something went wrong...")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))
                );
                Emogg.LOGGER.error("Trouble: ", e);
            }
        }
    }
}
