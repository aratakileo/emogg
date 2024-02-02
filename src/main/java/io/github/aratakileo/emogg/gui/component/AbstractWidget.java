package io.github.aratakileo.emogg.gui.component;

import io.github.aratakileo.emogg.util.GuiUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public abstract class AbstractWidget extends AbstractButton {
    public final static MouseTooltipPositioner MOUSE_TOOLTIP_POSITIONER = new MouseTooltipPositioner();

    private @Nullable Tooltip tooltip;

    protected @NotNull ClientTooltipPositioner tooltipPositioner = new MenuTooltipPositioner(this);
    protected @Nullable Consumer<AbstractWidget> onClicked = null;

    public AbstractWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public AbstractWidget(int x, int y, int width, int height, @NotNull Component message) {
        super(x, y, width, height, message);
    }

    @Override
    public void onPress() {
        if (onClicked != null) onClicked.accept(this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (active && visible && (keyCode == 257 || keyCode == 32 || keyCode == 335)) {
            onPress();
            return true;
        }

        return false;
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return mouseScrolled(mouseX, mouseY, 0, verticalAmount);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        super.render(guiGraphics, mouseX, mouseY, dt);

        if (visible && isHovered)
            renderTooltip(guiGraphics, mouseX, mouseY, dt);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        GuiUtil.drawRect(guiGraphics, x, y, width, height, 0xaa222222, 1, 0xaa000000);
        renderString(guiGraphics);
    }

    public void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        if (Objects.isNull(tooltip)) return;

        Minecraft.getInstance().screen.setTooltipForNextRenderPass(tooltip, tooltipPositioner, isFocused());
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
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setLeftTop(int left, int top) {
        setPosition(left, top);
    }

    public void setRightBottom(int right, int bottom) {
        setX(right - width);
        setY(bottom - height);
    }

    public int getRight() {
        return getX() + width;
    }

    public int getBottom() {
        return getY() + height;
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

    public void setOnClicked(@Nullable Consumer<AbstractWidget> onClicked) {
        this.onClicked = onClicked;
    }

    public void setTooltip(@NotNull String message) {
        setTooltip(Component.literal(message));
    }

    public void setTooltip(@NotNull Component message) {
        setTooltip(Tooltip.create(message));
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    public @Nullable Tooltip getTooltip() {
        return tooltip;
    }

    public void disableTooltip() {
        this.tooltip = null;
    }

    public void setTooltipPositioner(@NotNull ClientTooltipPositioner tooltipPositioner) {
        this.tooltipPositioner = tooltipPositioner;
    }

    protected static Font getFont() {
        return Minecraft.getInstance().font;
    }

    private static class MouseTooltipPositioner implements ClientTooltipPositioner {
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
