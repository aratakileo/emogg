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
import pextystudios.emogg.util.KeyboardUtil;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    protected EmojiSelectionButton emojiSelectionButton;
    protected EmojiSelectionMenu emojiSelectionMenu;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        final var positionOffset = input.getHeight();
        emojiSelectionButton = new EmojiSelectionButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        );
        self.addRenderableWidget(emojiSelectionButton);

        emojiSelectionMenu = new EmojiSelectionMenu(emojiSelectionButton.getHeight() + 4);
        emojiSelectionMenu.setRightBottom(self.width - 2, self.height - input.getHeight() - 3);
        emojiSelectionMenu.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));
        self.addRenderableWidget(emojiSelectionMenu);

        emojiSelectionButton.setOnClicked(emojiPickerButton -> emojiSelectionMenu.visible = !emojiSelectionMenu.visible);
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

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.visible || keyCode != KeyboardUtil.K_ESC) return;

        emojiSelectionMenu.visible = false;

        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionButton.isHovered) {
            if (!emojiSelectionMenu.isHovered) emojiSelectionMenu.visible = false;

            return;
        }

        cir.setReturnValue(emojiSelectionButton.mouseClicked(mouseX, mouseY, 0));
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.isHovered) return;

        cir.setReturnValue(emojiSelectionMenu.mouseScrolled(mouseX, mouseY, scrollDelta));
    }
}
