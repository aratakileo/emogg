package pextystudios.emogg.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import pextystudios.emogg.util.RenderUtil;

public class Button extends AbstractWidget {
    public Button(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Button(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public Button(int x, int y, String message) {
        this(x, y, Component.literal(message));
    }

    public Button(int x, int y, Component message) {
        super(
                x,
                y,
                Minecraft.getInstance().font.width(message.getString()) + 10,
                Minecraft.getInstance().font.lineHeight + 10,
                message
        );
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
}
