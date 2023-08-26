package io.github.aratakileo.emogg.gui.component;

import io.github.aratakileo.emogg.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Consumer;

public abstract class AbstractWidget extends AbstractButton {
    public final static MouseHintPositioner MOUSE_HINT_POSITIONER = new MouseHintPositioner();

    protected ClientTooltipPositioner hintPositioner = new MenuTooltipPositioner(this);
    protected Consumer<AbstractWidget> onClicked = null;

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
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);
        renderString(guiGraphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible) return false;

        if (isValidClickButton(button) && clicked(mouseX, mouseY)) {
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    protected @NotNull ClientTooltipPositioner createTooltipPositioner() {
        return this.isHovered
                && this.isFocused()
                && Minecraft.getInstance().getLastInputType().isKeyboard()
                ? super.createTooltipPositioner() : hintPositioner;
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

    public void renderString(GuiGraphics guiGraphics) {
        renderString(guiGraphics, 0xffffff);
    }

    public void renderString(GuiGraphics guiGraphics, int color) {
        renderString(guiGraphics, Minecraft.getInstance().font, color | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(GuiGraphics guiGraphics, char ch, int localX, int localY, int color) {
        renderString(guiGraphics, String.valueOf(ch), localX, localY, color, false);
    }

    public void renderString(GuiGraphics guiGraphics, String text, int localX, int localY, int color) {
        renderString(guiGraphics, text, localX, localY, color, false);
    }

    public void renderString(GuiGraphics guiGraphics, String text, int localX, int localY, int color, boolean shadow) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x + localX, y + localY, color, shadow);
    }

    public void playClickSound() {
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void setOnClicked(Consumer<AbstractWidget> onClicked) {
        this.onClicked = onClicked;
    }

    public void setHint(@NotNull String hint) {
        setHint(Component.literal(hint));
    }

    public void setHint(@NotNull Component hint) {
        setTooltip(Tooltip.create(hint));
    }

    public void disableHint() {
        setTooltip(null);
    }

    public void setHintPositioner(ClientTooltipPositioner hintPositioner) {
        this.hintPositioner = hintPositioner;
    }

    private static class MouseHintPositioner implements ClientTooltipPositioner {
        @Override
        public @NotNull Vector2ic positionTooltip(
                int guiWidth,
                int guiHeight,
                int mouseX,
                int mouseY,
                int tooltipWidth,
                int tooltipHeight
        ) {
            return new Vector2i(mouseX - tooltipWidth - 7, mouseY - tooltipHeight - 7);
        }
    }
}
