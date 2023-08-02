package pextystudios.emogg.gui.component;

import net.minecraft.client.gui.GuiGraphics;
import pextystudios.emogg.util.RenderUtil;

public class VerticalScrollbar extends AbstractWidget {
    private final RenderUtil.Rect2i thumbRect;

    private int numberOfScrollingPositions,
            scrollSegmentSize,
            scrollProgress = 0,
            scrollingThumbTopToTouchOffset = -1,
            strokeSize;

    public VerticalScrollbar(int x, int y, int width, int height, int numberOfScrollingPositions, int scrollSegmentSize, int strokeSize) {
        super(x, y, width, height);

        this.strokeSize = strokeSize;
        this.thumbRect = new RenderUtil.Rect2i(0, 0, width - strokeSize * 2, 0);

        setNumberOfScrollingPositions(numberOfScrollingPositions, scrollSegmentSize);
    }

    public boolean isScrolling() {
        return scrollingThumbTopToTouchOffset >= 0;
    }

    public int getNumberOfScrollingPositions() {
        return numberOfScrollingPositions;
    }

    public int getScrollProgress() {
        return scrollProgress;
    }

    public RenderUtil.Rect2i getRenderableThumbRect() {
        return thumbRect.move(x + strokeSize, y + strokeSize);
    }

    public void setNumberOfScrollingPositions(int numberOfScrollingPositions) {
        setNumberOfScrollingPositions(numberOfScrollingPositions, scrollSegmentSize);
    }

    public void setNumberOfScrollingPositions(int numberOfScrollingPositions, int scrollSegmentSize) {
        this.numberOfScrollingPositions = numberOfScrollingPositions;
        this.scrollSegmentSize = scrollSegmentSize;
        this.thumbRect.setHeight(Math.min(height - 10 - strokeSize * 2, Math.max(8, height - strokeSize * 2 - numberOfScrollingPositions / scrollSegmentSize)));
        this.thumbRect.setBottom(Math.min(width - strokeSize * 2, thumbRect.getBottom()));
    }

    public void setScrollProgress(int scrollProgress) {
        this.scrollProgress = Math.min(numberOfScrollingPositions, Math.max(scrollProgress, 0));
        this.thumbRect.setY((int) ((height - strokeSize * 2) * ((double) this.scrollProgress / (double) this.numberOfScrollingPositions)));
    }

    public void setScrollProgressByThumbY(int localY) {
        final var maxThumbTop = height - strokeSize * 2 - thumbRect.height;
        thumbRect.setY(Math.min(Math.max(localY - strokeSize, 0), maxThumbTop));

        scrollProgress = (int) (numberOfScrollingPositions * (double)thumbRect.getY() / (double)(maxThumbTop));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float dt) {
        RenderUtil.drawRect(
                x,
                y,
                width,
                height,
                0xaa222222,
                strokeSize,
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
        setScrollProgress((int) (scrollProgress - scrollDelta));

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
