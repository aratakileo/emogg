package io.github.aratakileo.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.aratakileo.emogg.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractWidget extends AbstractButton {
    protected Consumer<AbstractWidget> onClicked = null;
    protected Component hint = null;

    public AbstractWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        isHovered = collidePoint(mouseX, mouseY);

        renderButton(poseStack, mouseX, mouseY, dt);
        renderToolTip(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);
        renderString(poseStack);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        var currentScreen = Minecraft.getInstance().screen;

        if (hint == null || currentScreen == null) return;

        if (isHovered)
            currentScreen.renderTooltip(poseStack, hint, mouseX, mouseY);
        else if (isFocused())
            currentScreen.renderTooltip(poseStack, hint, x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible)
            return false;

        if (isValidClickButton(button) && clicked(mouseX, mouseY)) {
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);

        if (hint != null) narrationElementOutput.add(NarratedElementType.HINT, hint);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setLeftTop(int left, int top) {
        setX(left);
        setY(top);
    }

    public void setRightBottom(int right, int bottom) {
        setX(right - width);
        setY(bottom - height);
    }

    public int getRight() {
        return x + width;
    }

    public int getBottom() {
        return y + height;
    }

    public void renderString(PoseStack poseStack) {
        renderString(poseStack, 0xffffff);
    }

    public void renderString(PoseStack poseStack, int color) {
        drawCenteredString(
                poseStack,
                Minecraft.getInstance().font,
                getMessage(),
                x + width / 2,
                y + (height - 8) / 2,
                color | Mth.ceil(this.alpha * 255.0F) << 24
        );
    }

    public void renderString(PoseStack poseStack, String text, int localX, int localY, int color) {
        drawString(poseStack, Minecraft.getInstance().font, text, x + localX, y + localY, color);
    }

    public void playClickSound() {
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public boolean collidePoint(int x, int y) {
        if (!visible) return false;

        return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
    }

    public void setOnClicked(Consumer<AbstractWidget> onClicked) {
        this.onClicked = onClicked;
    }

    public void setHint(@NotNull String hint) {
        setHint(Component.literal(hint));
    }

    public void setHint(@NotNull Component hint) {
        this.hint = hint;
    }

    public void disableHint() {
        this.hint = null;
    }
}
