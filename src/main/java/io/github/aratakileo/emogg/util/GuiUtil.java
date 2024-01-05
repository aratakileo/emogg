package io.github.aratakileo.emogg.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class GuiUtil {
    public static void renderTexture(
            @NotNull GuiGraphics guiGraphics,
            @NotNull ResourceLocation resourceLocation,
            @NotNull Rect2i rect2i
    ) {
        io.github.aratakileo.suggestionsapi.util.RenderUtil.renderTexture(
                guiGraphics,
                resourceLocation,
                rect2i.getX(),
                rect2i.getY(),
                rect2i.getWidth(),
                rect2i.getHeight()
        );
    }

    public static void drawRect(@NotNull GuiGraphics guiGraphics, @NotNull Rect2i rect2i, int color) {
        drawRect(guiGraphics, rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight(), color);
    }

    public static void drawRect(@NotNull GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    public static void drawRectStroke(
            @NotNull GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int strokeThickness,
            int strokeColor
    ) {
        guiGraphics.fill(x, y, x + width, y + strokeThickness, strokeColor);
        guiGraphics.fill(x, y + height - strokeThickness, x + width, y + height, strokeColor);
        guiGraphics.fill(x, y + strokeThickness, x + strokeThickness, y + height - strokeThickness, strokeColor);
        guiGraphics.fill(x + width - strokeThickness, y + strokeThickness, x + width, y + height - strokeThickness, strokeColor);
    }

    public static void drawRect(
            @NotNull GuiGraphics guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int color,
            int strokeThickness,
            int strokeColor
    ) {
        drawRect(guiGraphics, x, y, width, height, color);
        drawRectStroke(guiGraphics, x, y, width, height, strokeThickness, strokeColor);
    }
}
