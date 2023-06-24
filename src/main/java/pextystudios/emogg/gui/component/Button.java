package pextystudios.emogg.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import pextystudios.emogg.util.RenderUtil;

public class Button extends AbstractWidget {
    public Button(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public Button(int x, int y, String message) {
        this(x, y, new TextComponent(message));
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
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float dt) {
        if (!visible) return;

        RenderUtil.drawRect(x, y, width, height, 0xaa222222, 1, 0xaa000000);

        if (isHovered())
            RenderUtil.drawRectStroke(x, y, width, height, 1, 0xffffffff);

        renderMessage(poseStack, 0xffffffff);
    }
}
