package pextystudios.emogg.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public final class RenderUtil {
    public final static float DEFAULT_Z_LEVEL = 0;

    public static void drawRect(int x, int y, int width, int height, int color, int strokeThickness, int strokeColor) {
        drawRect(x, y, width, height, color, strokeThickness, strokeColor, DEFAULT_Z_LEVEL);
    }

    public static void drawRect(int x, int y, int width, int height, int color, int strokeThickness, int strokeColor, float zLevel) {
        drawRect(x, y, width, height, color, zLevel);
        drawRectStroke(x, y, width, height, strokeThickness, strokeColor, zLevel);
    }

    public static void drawRect(int x, int y, int width, int height, int color) {
        fillSquad(x, y, x, y + height, x + width, y + height, x + width, y, color, DEFAULT_Z_LEVEL);
    }

    public static void drawRect(int x, int y, int width, int height, int color, float zLevel) {
        fillSquad(x, y, x, y + height, x + width, y + height, x + width, y, color, zLevel);
    }



    public static void drawRectStroke(int x, int y, int width, int height, int strokeThickness, int strokeColor) {
        drawRectStroke(x, y, width, height, strokeThickness, strokeColor, DEFAULT_Z_LEVEL);
    }



    public static void drawRectStroke(int x, int y, int width, int height, int strokeThickness, int strokeColor, float zLevel) {
        drawRect(x, y, strokeThickness, height, strokeColor, zLevel);                                                                                // left
        drawRect(x + width - strokeThickness, y, strokeThickness, height, strokeColor, zLevel);                                                   // right
        drawRect(x + strokeThickness, y, width - 2 * strokeThickness, strokeThickness, strokeColor, zLevel);                                // top
        drawRect(x + strokeThickness, y + height - strokeThickness, width - 2 * strokeThickness, strokeThickness, strokeColor, zLevel);  // bottom
    }

    public static void fillSquad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int alphaColor, float zLevel) {
        int a = (alphaColor >> 24 & 255);
        int r = (alphaColor >> 16 & 255);
        int g = (alphaColor >> 8 & 255);
        int b = (alphaColor & 255);

        setupFillMode();

        final var tesselator = Tesselator.getInstance();
        final var builder = tesselator.getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        builder.vertex(x0, y0, zLevel).color(r, g, b, a).endVertex();
        builder.vertex(x1, y1, zLevel).color(r, g, b, a).endVertex();
        builder.vertex(x2, y2, zLevel).color(r, g, b, a).endVertex();
        builder.vertex(x3, y3, zLevel).color(r, g, b, a).endVertex();

        tesselator.end();

        disassembleFillMode();
    }

    public static void setupFillMode() {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    public static void disassembleFillMode() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
}
