package pextystudios.emogg.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import pextystudios.emogg.util.RenderUtil;

import java.util.function.Consumer;

public abstract class AbstractWidget extends AbstractButton {
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
    public boolean mouseClicked(double mouseX, double mouseY, int i) {
        if (!active || !visible)
            return false;

        if (isValidClickButton(i) && clicked(mouseX, mouseY)) {
            onClick(mouseX, mouseY);
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public void renderString(GuiGraphics guiGraphics) {
        renderString(guiGraphics, 0xffffff);
    }

    public void renderString(GuiGraphics guiGraphics, int color) {
        renderString(guiGraphics, Minecraft.getInstance().font, color | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(GuiGraphics guiGraphics, String text, int localX, int localY, int color) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x + localX, y + localY, color, false);
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
}
