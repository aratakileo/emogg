package pextystudios.emogg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.api.ModrinthApi;
import pextystudios.emogg.font.EmojiFont;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    public ClientLevel level;

    @Shadow @Final
    public Gui gui;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontManager;createFont()Lnet/minecraft/client/gui/Font;"))
    private Font createFontWhenInit(FontManager fontManager) {
        return new EmojiFont(fontManager.createFont());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontManager;createFontFilterFishy()Lnet/minecraft/client/gui/Font;"))
    private Font createFontFilterFishyWhenInit(FontManager fontManager) {
        return new EmojiFont(fontManager.createFontFilterFishy());
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void setScreen(Screen screen, CallbackInfo ci) {
        if (
                screen == null
                        && level != null
                        && !Emogg.hasMessageAboutUpdateBeenShown
                        && ModrinthApi.needsToBeUpdated()
        ) {
            gui.getChat().addMessage(Component.literal(
                    "§7[§cemogg§7]§r "
                            + Language.getInstance().getOrDefault("emogg.message.new_version_is_available")
            ));

            Emogg.hasMessageAboutUpdateBeenShown = true;
        }
    }
}
