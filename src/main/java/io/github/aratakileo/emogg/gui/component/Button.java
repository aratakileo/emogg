package io.github.aratakileo.emogg.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import io.github.aratakileo.emogg.util.GuiUtil;
import org.jetbrains.annotations.NotNull;

public class Button extends AbstractWidget {
    private int padding = 0;

    protected Button(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Button(int x, int y, @NotNull Component text) {
        this(x, y, text, 5);
    }

    public Button(int x, int y, @NotNull Component text, int padding) {
        super(
                x,
                y,
                getFont().width(text.getString()) + padding * 2,
                getFont().lineHeight + padding * 2,
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
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        GuiUtil.drawRect(guiGraphics, x, y, width, height, 0xaa222222, 1, 0xaa000000);

        if (isHovered)
            GuiUtil.drawRectStroke(guiGraphics, x, y, width, height, 1, 0xffffffff);

        renderString(guiGraphics);
    }

    public void setMessage(@NotNull Component text, boolean isHorizontalCentered) {
        super.setMessage(text);

        final var prevWidth = width;
        width = getFont().width(text.getString()) + padding * 2;

        if (isHorizontalCentered && width != prevWidth)
            x -= (width - prevWidth) / 2;
    }
}
