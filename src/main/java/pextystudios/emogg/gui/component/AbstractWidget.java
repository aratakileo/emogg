package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

public abstract class AbstractWidget extends AbstractButton {
    public interface OnClickListener {
        void onClick(AbstractWidget widget);
    }

    protected OnClickListener onClickListener = null;
    protected Component hint = null;

    public boolean playSoundWhenClick = true;

    public AbstractWidget(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY);
    }

    public AbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onPress() {
        if (onClickListener != null) onClickListener.onClick(this);
    }

    @Override
    public boolean keyPressed(int key, int j, int k) {
        if (this.active && this.visible) {
            if (key != 257 && key != 32 && key != 335)
                return false;

            if (playSoundWhenClick) playDownSound(Minecraft.getInstance().getSoundManager());
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
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        super.renderButton(poseStack, mouseX, mouseY, dt);

        if (isHovered())
            renderToolTip(poseStack, mouseX, mouseY);
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
        return x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
    }

    public boolean isFocusedButNotHovered() {
        return !isHovered && isFocused();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
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
