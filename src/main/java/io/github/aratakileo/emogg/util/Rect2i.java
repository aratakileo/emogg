package io.github.aratakileo.emogg.util;

import org.jetbrains.annotations.NotNull;

public class Rect2i extends net.minecraft.client.renderer.Rect2i {
    public Rect2i(int x, int y, int size) {
        this(x, y, size, size);
    }

    public Rect2i(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
    }

    @Override
    public boolean contains(int x, int y) {
        return super.contains(x, y);
    }

    public @NotNull Rect2i moveX(int x) {
        final var returnable = copy();
        returnable.setX(getX() + x);

        return returnable;
    }

    public @NotNull Rect2i moveY(int y) {
        final var returnable = copy();
        returnable.setY(getY() + y);

        return returnable;
    }

    public @NotNull Rect2i move(int x, int y) {
        final var returnable = copy();
        returnable.setPosition(getX() + x, getY() + y);

        return returnable;
    }

    public void moveIpX(int x) {
        setX(getX() + x);
    }

    public void moveIpY(int y) {
        setY(getY() + y);
    }

    public void moveIp(int x, int y) {
        setX(getX() + x);
        setY(getY() + y);
    }

    public @NotNull Rect2i moveBounds(int left, int top, int right, int bottom) {
        final var returnable = new Rect2i(
                getX() + left,
                getY() + top,
                getWidth() - left + right,
                getHeight() - top + bottom
        );

        if (returnable.getWidth() < 0) {
            returnable.moveIpX(returnable.getWidth());
            returnable.setWidth(-returnable.getWidth());
        }

        if (returnable.getHeight() < 0) {
            returnable.moveIpY(returnable.getHeight());
            returnable.setHeight(-returnable.getHeight());
        }

        return returnable;
    }

    public boolean contains(float x, float y) {
        return contains((int) Math.ceil(x), (int) Math.ceil(y));
    }

    public boolean contains(double x, double y) {
        return contains((int) Math.ceil(x), (int) Math.ceil(y));
    }

    public @NotNull Rect2i expand(int horizontal, int vertical) {
        final var returnable = copy();
        returnable.setSize(getWidth() + horizontal, getHeight() + vertical);

        return returnable;
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public int getRight() {
        return getX() + getWidth();
    }

    public void setRight(int right) {
        setX(right - getWidth());
    }

    public int getBottom() {
        return getY() + getHeight();
    }

    public void setBottom(int bottom) {
        setY(bottom - getHeight());
    }

    public boolean hasArea() {
        return getWidth() > 0 && getHeight() > 0;
    }

    @Override
    public String toString() {
        return "Rect2i{%d, %d, %d, %d}".formatted(getX(), getY(), getWidth(), getHeight());
    }

    public @NotNull Rect2i copy() {
        return new Rect2i(getX(), getY(), getWidth(), getHeight());
    }
}
