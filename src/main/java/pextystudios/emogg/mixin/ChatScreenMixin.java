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
import pextystudios.emogg.gui.component.EmojiPickerButton;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private EmojiPickerButton emojiPickerButton;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;
        final var positionOffset = input.getHeight() + 2;

        emojiPickerButton = new EmojiPickerButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        );
        emojiPickerButton.setOnClickListener(emojiPickerButton -> Emogg.LOGGER.info("Pressed emoji button!"));

        self.addRenderableWidget(emojiPickerButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        if (!emojiPickerButton.collidePoint(mouseX, mouseY)) {
            emojiPickerButton.setFocused(false);
            return;
        }

        final var self = (ChatScreen)(Object)this;

        self.setFocused(emojiPickerButton);
        self.input.setFocus(false);
        emojiPickerButton.setFocused(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiPickerButton.isHovered()) return;

        cir.setReturnValue(emojiPickerButton.mouseClicked(mouseX, mouseY, 0));
    }
}
