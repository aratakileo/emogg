package pextystudios.emogg.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pextystudios.emogg.Emogg;
import pextystudios.emogg.gui.component.EmojiButton;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private EmojiButton emojiButton;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;
        final var positionOffset = input.getHeight() + 1;

        emojiButton = new EmojiButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 2,
                input.getHeight() - 2,
                emojiButton -> {
                    Emogg.LOGGER.info("Pressed emoji button!");
                }
        );

        self.addRenderableWidget(emojiButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        if (!emojiButton.collidePoint(mouseX, mouseY)) {
            emojiButton.setFocused(false);
            return;
        }

        final var self = (ChatScreen)(Object)this;

        self.setFocused(emojiButton);
        self.input.setFocus(false);
        emojiButton.setFocused(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiButton.isHovered()) return;

        cir.setReturnValue(emojiButton.mouseClicked(mouseX, mouseY, 0));
    }
}
