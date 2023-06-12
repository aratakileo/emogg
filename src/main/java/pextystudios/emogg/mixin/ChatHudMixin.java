package pextystudios.emogg.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.text.TextReaderVisitor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin({net.minecraft.client.gui.hud.ChatHud.class})
public abstract class ChatHudMixin {
  private final Logger MOD_LOGGER = LogManager.getLogger("customemotes.mixin.ChatHudMixin");
  
  @Shadow
  @Final
  private MinecraftClient client;
  
  @Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I"))
  private int drawWithShadow(TextRenderer textRenderer, MatrixStack matrixStack, OrderedText text, float x, float y, int color) {
    TextReaderVisitor textReaderVisitor = new TextReaderVisitor();
    text.accept(textReaderVisitor);
    float emoteSize = textRenderer.getWidth("   ");
    float emoteAlpha = (color >> 24 & 0xFF) / 255.0F;
    final double VERTICAL_OFFSET = 2;

    matrixStack.translate(0.0D, -VERTICAL_OFFSET, 0.0D);

    Pattern emojiPattern = Pattern.compile("(:([_A-Za-z0-9]+):)");

    boolean emotesLeft = true;
    while (emotesLeft) {

      String textStr = textReaderVisitor.getString();
      Matcher emoteMatch = emojiPattern.matcher(textStr);

      while (emotesLeft = emoteMatch.find()) {
        try {
          String emojiName = emoteMatch.group(2);
          int startPos = emoteMatch.start(1);
          int endPos = emoteMatch.end(1);
          if (Emogg.getInstance().emojis.containsKey(emojiName)) {
            float beforeTextWidth = textRenderer.getWidth(textStr.substring(0, startPos));

            Emogg.getInstance().emojis.get(emojiName).draw(
                    matrixStack,
                    x + beforeTextWidth,
                    y,
                    emoteSize,
                    emoteAlpha
            );

            textReaderVisitor.replaceBetween(startPos, endPos, "   ", Style.EMPTY);
            break;
          }
        } catch (NumberFormatException numberFormatException) {}
      }
    }

    matrixStack.translate(0.0D, VERTICAL_OFFSET, 0.0D);
    return textRenderer.draw(matrixStack, textReaderVisitor.getOrderedText(), x, y, color);
  }
}

