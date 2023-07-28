package pextystudios.emogg.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pextystudios.emogg.gui.component.EmojiSelectionMenu;
import pextystudios.emogg.gui.component.EmojiSelectionButton;
import pextystudios.emogg.EmoggConfig;
import pextystudios.emogg.util.KeyboardUtil;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique
    protected EmojiSelectionButton emogg$emojiSelectionButton;
    @Unique
    protected EmojiSelectionMenu emogg$emojiSelectionMenu;

    @Shadow protected EditBox input;

    protected ChatScreenMixin(Component $$0) {
        super($$0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var positionOffset = input.getHeight();
        emogg$emojiSelectionButton = new EmojiSelectionButton(
                width - positionOffset,
                height - positionOffset,
                input.getHeight() - 4
        );
        addRenderableWidget(emogg$emojiSelectionButton);

        emogg$emojiSelectionMenu = new EmojiSelectionMenu(emogg$emojiSelectionButton.getHeight() + 4);
        emogg$emojiSelectionMenu.setRightBottom(width - 2, height - input.getHeight() - 3);
        emogg$emojiSelectionMenu.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));
        addRenderableWidget(emogg$emojiSelectionMenu);

        emogg$emojiSelectionButton.setOnClicked(emojiPickerButton -> emogg$emojiSelectionMenu.visible = !emogg$emojiSelectionMenu.visible);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        if (emogg$emojiSelectionMenu.isMouseOver(mouseX, mouseY)) {
            setFocused(emogg$emojiSelectionMenu);
            return;
        }

        if (emogg$emojiSelectionButton.isMouseOver(mouseX, mouseY)) {
            setFocused(emogg$emojiSelectionButton);
            return;
        }

        setFocused(input);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emogg$emojiSelectionMenu.visible || keyCode != KeyboardUtil.K_ESC) return;

        emogg$emojiSelectionMenu.visible = false;

        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!emogg$emojiSelectionButton.isHovered()) {
            if (!emogg$emojiSelectionMenu.isHovered()) emogg$emojiSelectionMenu.visible = false;

            return;
        }

        cir.setReturnValue(emogg$emojiSelectionButton.mouseClicked(mouseX, mouseY, 0));
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
        if (!emogg$emojiSelectionMenu.isHovered()) return;

        cir.setReturnValue(emogg$emojiSelectionMenu.mouseScrolled(mouseX, mouseY, scrollDelta));
    }
}
