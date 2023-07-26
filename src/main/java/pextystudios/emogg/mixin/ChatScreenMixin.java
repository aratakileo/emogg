package pextystudios.emogg.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pextystudios.emogg.gui.component.EmojiSelectionMenu;
import pextystudios.emogg.gui.component.EmojiSelectionButton;
import pextystudios.emogg.EmoggConfig;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private EmojiSelectionButton emojiSelectionButton;
    private EmojiSelectionMenu emojiSelectionMenu;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        emojiSelectionMenu = new EmojiSelectionMenu();
        emojiSelectionMenu.x = self.width - emojiSelectionMenu.getWidth() - 4;
        emojiSelectionMenu.y = self.height - emojiSelectionMenu.getHeight() - input.getHeight() - 4;
        emojiSelectionMenu.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));
        self.addRenderableWidget(emojiSelectionMenu);

        final var positionOffset = input.getHeight();
        emojiSelectionButton = new EmojiSelectionButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        );
        emojiSelectionButton.setOnClicked(emojiPickerButton -> emojiSelectionMenu.visible = !emojiSelectionMenu.visible);
        emojiSelectionButton.visible = EmoggConfig.instance.isExperimentalExperienceEnabled;
        self.addRenderableWidget(emojiSelectionButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        if (emojiSelectionMenu.isMouseOver(mouseX, mouseY)) {
            self.setFocused(emojiSelectionMenu);
            return;
        }

        if (emojiSelectionButton.isMouseOver(mouseX, mouseY)) {
            self.setFocused(emojiSelectionButton);
            return;
        }

        self.setFocused(input);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int i, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionButton.isHovered) return;

        cir.setReturnValue(emojiSelectionButton.mouseClicked(mouseX, mouseY, 0));
    }
}
