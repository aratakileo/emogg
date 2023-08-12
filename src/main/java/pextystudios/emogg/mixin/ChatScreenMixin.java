package pextystudios.emogg.mixin;

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
import pextystudios.emogg.handler.EmojiHandler;
import pextystudios.emogg.handler.FrequentlyUsedEmojiController;
import pextystudios.emogg.resource.Emoji;
import pextystudios.emogg.gui.component.EmojiSelectionMenu;
import pextystudios.emogg.gui.component.EmojiButton;
import pextystudios.emogg.util.KeyboardUtil;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Unique
    protected EmojiButton emojiButton;
    @Unique
    protected EmojiSelectionMenu emojiSelectionMenu = null;
    @Unique
    protected Pair<Integer, Boolean> emojiSelectionMenuState;
    @Unique
    protected Emoji emojiButtonDisplayableEmojiState;

    @Shadow public EditBox input;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        final var self = (ChatScreen)(Object)this;

        final var positionOffset = input.getHeight();
        emojiButton = new EmojiButton(
                self.width - positionOffset,
                self.height - positionOffset,
                input.getHeight() - 4
        );
        self.addRenderableWidget(emojiButton);

        emojiSelectionMenu = new EmojiSelectionMenu((float) (emojiButton.getHeight() * 1.5));
        emojiSelectionMenu.setRightBottom(self.width - 2, self.height - input.getHeight() - 3);
        emojiSelectionMenu.setOnEmojiSelected(emoji -> input.insertText(emoji.getCode()));
        self.addRenderableWidget(emojiSelectionMenu);

        emojiButton.setOnClicked(emojiPickerButton -> {
            if (!emojiSelectionMenu.visible) emojiSelectionMenu.refreshFrequentlyUsedEmojis();

            emojiSelectionMenu.visible = !emojiSelectionMenu.visible;
        });

        if (EmojiHandler.getInstance().isEmpty()) {
            emojiButton.active = false;
            emojiButton.visible = false;
        }
    }

    @Inject(method = "resize", at = @At("HEAD"))
    public void resizeHead(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        emojiSelectionMenuState = new Pair<>(
                emojiSelectionMenu.verticalScrollbar.getScrollProgress(),
                emojiSelectionMenu.visible
        );
        emojiButtonDisplayableEmojiState = emojiButton.getDisplayableEmoji();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    public void resizeTail(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        emojiSelectionMenu.refreshFrequentlyUsedEmojis();
        emojiSelectionMenu.verticalScrollbar.setScrollProgress(emojiSelectionMenuState.getA());
        emojiSelectionMenu.visible = emojiSelectionMenuState.getB();
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
        FrequentlyUsedEmojiController.collectStatisticFrom(text);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.visible || keyCode != KeyboardUtil.K_ESC) return;

        emojiSelectionMenu.visible = false;

        cir.setReturnValue(true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiButton.isHovered) {
            if (!emojiSelectionMenu.isHovered) emojiSelectionMenu.visible = false;

            return;
        }

        cir.setReturnValue(emojiButton.mouseClicked(mouseX, mouseY, 0));
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void mouseScrolled(double mouseX, double mouseY, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
        if (!emojiSelectionMenu.isHovered) return;

        cir.setReturnValue(emojiSelectionMenu.mouseScrolled(mouseX, mouseY, scrollDelta));
    }
}
