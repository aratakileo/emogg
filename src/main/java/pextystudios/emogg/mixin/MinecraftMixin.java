package pextystudios.emogg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pextystudios.emogg.emoji.EmojiFont;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontManager;createFont()Lnet/minecraft/client/gui/Font;"))
    private Font createFontWhenInit(FontManager fontManager) {
        return new EmojiFont(fontManager.createFont());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/FontManager;createFontFilterFishy()Lnet/minecraft/client/gui/Font;"))
    private Font createFontFilterFishyWhenInit(FontManager fontManager) {
        return new EmojiFont(fontManager.createFontFilterFishy());
    }
}
