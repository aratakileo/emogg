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
import pextystudios.emogg.gui.component.OpenEmojiMenuButton;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private OpenEmojiMenuButton openEmojiMenuButton;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;
        final var positionOffset = input.getHeight() + 1;

        openEmojiMenuButton = new OpenEmojiMenuButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 2,
                input.getHeight() - 2,
                emojiButton -> {
                    Emogg.LOGGER.info("Pressed emoji button!");
                }
        );

        self.addRenderableWidget(openEmojiMenuButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        if (!openEmojiMenuButton.collidePoint(mouseX, mouseY)) {
            openEmojiMenuButton.setFocused(false);
            return;
        }

        final var self = (ChatScreen)(Object)this;

        self.setFocused(openEmojiMenuButton);
        self.input.setFocus(false);
        openEmojiMenuButton.setFocused(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!openEmojiMenuButton.isHovered()) return;

        cir.setReturnValue(openEmojiMenuButton.mouseClicked(mouseX, mouseY, 0));
    }
}
