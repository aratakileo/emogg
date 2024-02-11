package io.github.aratakileo.emogg.mixin.mixins.gui;

import io.github.aratakileo.elegantia.math.Rect2i;
import io.github.aratakileo.elegantia.util.Mouse;
import io.github.aratakileo.emogg.emoji.EmojiManager;
import io.github.aratakileo.emogg.emoji.FueController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import oshi.util.tuples.Pair;
import io.github.aratakileo.emogg.emoji.Emoji;
import io.github.aratakileo.emogg.gui.esm.EmojiSelectionMenu;
import io.github.aratakileo.emogg.gui.EmojiButton;
import io.github.aratakileo.emogg.util.KeyboardUtil;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    public EmojiButton emojiButton;
    @Unique
    public EmojiSelectionMenu emojiSelectionMenu;
    @Unique
    protected Pair<Integer, Boolean> emojiSelectionMenuState;
    @Unique
    protected Emoji emojiButtonDisplayableEmojiState;

    @Shadow
    protected EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;
        input.setCanLoseFocus(true);

        final var positionOffset = input.getHeight();
        emojiButton = new EmojiButton(new Rect2i(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        ));

        emojiSelectionMenu = new EmojiSelectionMenu((float) (emojiButton.getHeight() * 1.5));
        emojiSelectionMenu.setRightBottom(self.width - 2, self.height - input.getHeight() - 3);
        emojiSelectionMenu.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));

        emojiButton.setOnClickListener((btn, byUser) -> {
            if (!emojiSelectionMenu.isVisible) emojiSelectionMenu.refreshFrequentlyUsedEmojis();

            emojiSelectionMenu.isVisible = !emojiSelectionMenu.isVisible;

            return true;
        });

        if (EmojiManager.getInstance().isEmpty()) {
            emojiButton.isActive = false;
            emojiButton.isVisible = false;
        }

        self.addRenderableWidget(emojiButton);
        self.addRenderableWidget(emojiSelectionMenu);
    }

    @Inject(method = "resize", at = @At("HEAD"))
    public void resizeHead(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        emojiSelectionMenuState = new Pair<>(
                emojiSelectionMenu.verticalScrollbar.getProgress(),
                emojiSelectionMenu.isVisible
        );
        emojiButtonDisplayableEmojiState = emojiButton.getDisplayableEmoji();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    public void resizeTail(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        emojiSelectionMenu.refreshFrequentlyUsedEmojis();
        emojiSelectionMenu.verticalScrollbar.setProgress(emojiSelectionMenuState.getA());
        emojiSelectionMenu.isVisible = emojiSelectionMenuState.getB();
        emojiButton.setDisplayableEmoji(emojiButtonDisplayableEmojiState);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt, CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        if (emojiSelectionMenu.isMouseOver(mouseX, mouseY)) {
            self.setFocused(emojiSelectionMenu);
            return;
        }

        if (emojiButton.isMouseOver(mouseX, mouseY)) {
            self.setFocused(emojiButton);
            return;
        }

        self.setFocused(input);
    }

    @Inject(method = "handleChatInput", at = @At("HEAD"))
    public void handleChatInput(String text, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        FueController.collectStatisticFrom(text);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != KeyboardUtil.K_ESC) return;

        if (emojiSelectionMenu.isVisible || modifiers == KeyboardUtil.KMOD_SHIFT)
            cir.setReturnValue(emojiButton.onClick(false));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Mouse.Button.of(button).isLeft()) {
            if (emojiButton.isHovered()) {
                cir.setReturnValue(emojiButton.onClick(true));
                return;
            }

            if (!emojiSelectionMenu.isHovered() && emojiSelectionMenu.isVisible) {
                cir.setReturnValue(emojiButton.onClick(false));
                return;
            }
        }

        if (Mouse.Button.of(button).isMiddle()) cir.setReturnValue(emojiButton.onClick(false));
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
// 1.20.1
    public void mouseScrolled(double mouseX, double mouseY, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
// 1.20.4
//    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.isHovered()) return;

        cir.setReturnValue(emojiSelectionMenu.mouseScrolled(mouseX, mouseY, 0, verticalAmount));
    }
}
