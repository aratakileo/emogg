package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import pextystudios.emogg.gui.screen.AbstractScreen;
import pextystudios.emogg.util.RenderUtil;

import java.util.function.Consumer;

public abstract class AbstractWidget extends AbstractButton {
    protected Consumer<AbstractWidget> onClicked = null;
    protected Component hint = null;

    public AbstractWidget(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY);
    }

    public AbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onPress() {
        if (onClicked != null) onClicked.accept(this);
    }

    @Override
    public boolean keyPressed(int key, int j, int k) {
        if (this.active && this.visible) {
            if (key != 257 && key != 32 && key != 335)
                return false;

            onPress();

            return true;
        }
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);

        if (hint != null) narrationElementOutput.add(NarratedElementType.HINT, hint);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        var currentScreen = Minecraft.getInstance().screen;

        if (hint == null || currentScreen == null) return;

        if (isHovered())
            currentScreen.renderTooltip(poseStack, hint, mouseX, mouseY);
        else if (isFocusedButNotHovered())
            currentScreen.renderTooltip(poseStack, hint, x, y);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        isHovered = collidePoint(mouseX, mouseY);

        renderButton(poseStack, mouseX, mouseY, dt);
        renderToolTip(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);
        renderMessage(poseStack, 0xffffff);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int i) {
        if (!active || !visible)
            return false;

        if (isValidClickButton(i) && clicked(mouseX, mouseY)) {
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    public void playClickSound() {
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void renderMessage(PoseStack poseStack, int color) {
        net.minecraft.client.gui.components.AbstractWidget.drawCenteredString(
                poseStack,
                Minecraft.getInstance().font, getMessage(),
                x + width / 2,
                y + (height - 8) / 2,
                color | Mth.ceil(alpha * 255.0f) << 24
        );
    }

    public void renderMinecraftStyleButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        super.renderButton(poseStack, mouseX, mouseY, dt);
    }

    public boolean collidePoint(int x, int y) {
        if (!visible) return false;

        return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
    }

    public boolean isFocusedButNotHovered() {
        return !isHovered && isFocused();
    }

    public void setOnClicked(Consumer<AbstractWidget> onClicked) {
        this.onClicked = onClicked;
    }

    public void setHint(String hint) {
        this.hint = hint == null ? null : new TextComponent(hint);
    }

    public void setHint(Component hint) {
        this.hint = hint;
    }

    public void disableHint() {
        hint = null;
    }
}
