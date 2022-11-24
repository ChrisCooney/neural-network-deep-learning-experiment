package org.cooney.world.items.agents;

public enum Direction {

    UP(0, 0, -1),
    DOWN(1, 0, 1),
    LEFT(2, -1, 0),
    RIGHT(3, 1, 0),
    UP_LEFT(4, -1, -1),
    UP_RIGHT(5, -1, 1),
    DOWN_LEFT(6, 1, -1),
    DOWN_RIGHT(7, 1, 1),
    STAY_STILL(8, 0, 0);

    private final int index;
    private final int xDirection;
    private final int yDirection;

    Direction(int index, int xDirection, int yDirection) {
        this.index = index;
        this.xDirection = xDirection;
        this.yDirection = yDirection;
    }

    public static Direction randomDirection() {
        return values()[(int) (Math.random() * (values().length - 1))];
    }

    public int getIndex() {
        return index;
    }

    public int getXDirection() {
        return xDirection;
    }

    public int getYDirection() {
        return yDirection;
    }

    public static Direction getFromIndex(int index) {
        for(Direction d : values()) {
            if (d.getIndex() == index) {
                return d;
            }
        }

        return null;
    }
}
