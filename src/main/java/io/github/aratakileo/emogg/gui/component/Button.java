package io.github.aratakileo.emogg.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import io.github.aratakileo.emogg.util.RenderUtil;

public class Button extends AbstractWidget {
    private int padding = 0;

    protected Button(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Button(int x, int y, Component text) {
        this(x, y, text, 5);
    }

    public Button(int x, int y, Component text, int padding) {
        super(
                x,
                y,
                Minecraft.getInstance().font.width(text.getString()) + padding * 2,
                Minecraft.getInstance().font.lineHeight + padding * 2,
                text
        );

        this.padding = padding;
    }

    @Override
    public void onPress() {
        playClickSound();
        super.onPress();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);

        if (isHovered)
            RenderUtil.drawRectStroke(x, y, width, height, 1, 0xffffffff);

        renderString(guiGraphics);
    }

    public void setMessage(Component text, boolean isHorizontalCentered) {
        super.setMessage(text);

        final var prevWidth = width;
        width = Minecraft.getInstance().font.width(text.getString()) + padding * 2;

        if (isHorizontalCentered && width != prevWidth)
            x -= (width - prevWidth) / 2;
    }
}
