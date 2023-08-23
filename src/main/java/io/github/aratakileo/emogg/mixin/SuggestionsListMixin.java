package io.github.aratakileo.emogg.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.suggestion.Suggestion;
import io.github.aratakileo.emogg.font.EmojiFont;
import io.github.aratakileo.emogg.font.EmojiLiteral;
import io.github.aratakileo.emogg.handler.EmojiHandler;
import io.github.aratakileo.emogg.resource.Emoji;
import io.github.aratakileo.emogg.util.EmojiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin {
    @Shadow @Final
    private Rect2i rect;

    @Unique
    private final HashMap<String, Emoji> emojis = new HashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(
            CommandSuggestions commandSuggestions,
            int x,
            int y,
            int width,
            List<Suggestion> suggestions,
            boolean bl,
            CallbackInfo ci
    ){
        for (var suggestion: suggestions) {
            final var suggestionText = suggestion.getText();
            final var matcher = EmojiLiteral.EMOJI_CODE_PATTERN.matcher(suggestionText);
            final String emojiName;

            if (matcher.find() && EmojiHandler.getInstance().hasEmoji(emojiName = matcher.group(2))) {
                emojis.put(matcher.group(1), EmojiHandler.getInstance().getEmoji(emojiName));
                rect.setWidth(Math.max(
                        rect.getWidth(),
                        EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE + 6 + ((EmojiFont)Minecraft.getInstance().font).width(
                                suggestionText,
                                false
                        )
                ));
            }
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I"))
    private int updateCommandInfo(Font instance, PoseStack poseStack, String string, float x, float y, int color){
        if (!emojis.containsKey(string)) return instance.drawShadow(poseStack, string, x, y, color);

        EmojiUtil.render(emojis.get(string), poseStack, (int) x + 1, (int) y, EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE);

        return instance.drawShadow(
                poseStack,
                '\\' + string,
                x + EmojiLiteral.EMOJI_DEFAULT_RENDER_SIZE + 3,
                y,
                color
        );
    }
}
