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
import pextystudios.emogg.gui.component.EmojiSelector;
import pextystudios.emogg.gui.component.EmojiSelectorButton;
import pextystudios.emogg.handler.ConfigHandler;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private EmojiSelectorButton emojiSelectorButton;
    private EmojiSelector emojiSelector;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        emojiSelector = new EmojiSelector();
        emojiSelector.setOnClicked(emojiPicker -> Emogg.LOGGER.info("Emoji picker clicked!"));
        emojiSelector.x = self.width - emojiSelector.getWidth() - 4;
        emojiSelector.y = self.height - emojiSelector.getHeight() - input.getHeight() - 4;
        emojiSelector.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));
        self.addRenderableWidget(emojiSelector);

        final var positionOffset = input.getHeight();
        emojiSelectorButton = new EmojiSelectorButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        );
        emojiSelectorButton.setOnClicked(emojiPickerButton -> emojiSelector.visible = !emojiSelector.visible);
        emojiSelectorButton.visible = ConfigHandler.data.isExperimentalExperienceEnabled;
        self.addRenderableWidget(emojiSelectorButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        emojiSelector.setFocused(false);
        emojiSelectorButton.setFocused(false);

        if (emojiSelector.collidePoint(mouseX, mouseY)) {
            self.setFocused(emojiSelector);
            self.input.setFocus(false);
            emojiSelector.setFocused(true);

            return;
        }

        if (emojiSelectorButton.collidePoint(mouseX, mouseY)) {
            self.setFocused(emojiSelectorButton);
            self.input.setFocus(false);
            emojiSelectorButton.setFocused(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectorButton.isHovered()) return;

        cir.setReturnValue(emojiSelectorButton.mouseClicked(mouseX, mouseY, 0));
    }
}
