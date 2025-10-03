package net.lyivx.ls_tweaks.api.recipes;

/** Simple position helpers for widget placement within a container. */
public final class Position {
    public enum Horizontal { LEFT, CENTER, RIGHT }
    public enum Vertical { TOP, MIDDLE, BOTTOM }

    public static int leftX(int containerWidth, int elementWidth, int margin) {
        return Math.max(0, margin);
    }

    public static int centerX(int containerWidth, int elementWidth) {
        return Math.max(0, (containerWidth - elementWidth) / 2);
    }

    public static int rightX(int containerWidth, int elementWidth, int margin) {
        return Math.max(0, containerWidth - elementWidth - Math.max(0, margin));
    }

    public static int topY(int containerHeight, int elementHeight, int margin) {
        return Math.max(0, margin);
    }

    public static int middleY(int containerHeight, int elementHeight) {
        return Math.max(0, (containerHeight - elementHeight) / 2);
    }

    public static int bottomY(int containerHeight, int elementHeight, int margin) {
        return Math.max(0, containerHeight - elementHeight - Math.max(0, margin));
    }

    public static XY topLeft(int marginX, int marginY) {
        return new XY(Math.max(0, marginX), Math.max(0, marginY));
    }

    public static XY topCenter(int containerWidth, int elementWidth, int marginTop) {
        return new XY(centerX(containerWidth, elementWidth), Math.max(0, marginTop));
    }

    public static XY center(int containerWidth, int containerHeight, int elementWidth, int elementHeight) {
        return new XY(centerX(containerWidth, elementWidth), middleY(containerHeight, elementHeight));
    }

    public static final class XY {
        public final int x; public final int y;
        public XY(int x, int y) { this.x = x; this.y = y; }
    }

    private Position() {}
}


