package pextystudios.emogg.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AbstractScreen extends Screen {
    protected final Screen parent;

    protected AbstractScreen(Component component, Screen parent) {
        super(component);
        this.parent = parent;
    }

    protected AbstractScreen(Component component) {
        super(component);
        this.parent = Minecraft.getInstance().screen;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(poseStack, i, j, f);
    }
}
