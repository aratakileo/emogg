package io.github.aratakileo.emogg.gui.component;

import io.github.aratakileo.emogg.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;

public class VerticalScrollbar extends AbstractWidget {
    private final RenderUtil.Rect2i thumbRect;

    private int maxProgress,
            segmentSize,
            progress = 0,
            scrollingThumbTopToTouchOffset = -1,
            padding;

    public VerticalScrollbar(
            int x,
            int y,
            int width,
            int height,
            int maxProgress,
            int segmentSize,
            int padding
    ) {
        super(x, y, width, height);

        this.padding = padding;
        this.thumbRect = new RenderUtil.Rect2i(0, 0, width - padding * 2, 0);

        setMaxProgress(maxProgress, segmentSize);
    }

    public boolean isScrolling() {
        return scrollingThumbTopToTouchOffset >= 0;
    }

    public int getProgress() {
        return progress;
    }

    public RenderUtil.Rect2i getRenderableThumbRect() {
        return thumbRect.move(x + padding, y + padding);
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        setMaxProgress(maxProgress, segmentSize);
    }

    public void increaseMaxProgress(int increaseValue) {
        if (increaseValue == 0) return;

        setMaxProgress(maxProgress + increaseValue);
    }

    public void setMaxProgress(int maxProgress, int segmentSize) {
        this.maxProgress = maxProgress;
        this.segmentSize = segmentSize;

        thumbRect.setHeight(Math.min(
                height - 10 - padding * 2,
                Math.max(8, height - padding * 2 - maxProgress / segmentSize)
        ));

        setThumbY(padding + thumbRect.getY());
    }

    public void setProgress(int progress) {
        this.progress = Math.min(maxProgress, Math.max(progress, 0));
        this.thumbRect.setY((int) (
                (height - thumbRect.height - padding * 2)
                        * ((double) this.progress / (double) this.maxProgress)
        ));
    }

    public void setScrollProgressByThumbY(int localY) {
        final var maxThumbTop = height - padding * 2 - thumbRect.height;
        setThumbY(localY, maxThumbTop);
        progress = (int) (maxProgress * (double)thumbRect.getY() / (double)(maxThumbTop));
    }

    private void setThumbY(int localY) {
        final var maxThumbTop = height - padding * 2 - thumbRect.height;
        setThumbY(localY, maxThumbTop);
    }

    private void setThumbY(int localY, int maxThumbTop) {
        thumbRect.setY(Math.min(Math.max(localY - padding, 0), maxThumbTop));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(
                x,
                y,
                width,
                height,
                0xaa222222,
                padding,
                0xaa000000
        );

        RenderUtil.drawRect(
                getRenderableThumbRect(),
                (getRenderableThumbRect().contains(mouseX, mouseY)) ? 0xaacbcbcb : 0xaa6c757d
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (getRenderableThumbRect().contains((int) mouseX, (int) mouseY)) {
            scrollingThumbTopToTouchOffset = (int) (mouseY - getRenderableThumbRect().getY());
            return;
        }

        if (!isHovered) return;

        setScrollProgressByThumbY((int) (mouseY - y));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        setProgress((int) (progress - scrollDelta));

        scrollingThumbTopToTouchOffset = -1;

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isScrolling()) return false;

        setScrollProgressByThumbY((int) (mouseY - y - scrollingThumbTopToTouchOffset));

        return true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        scrollingThumbTopToTouchOffset = -1;
    }
}
