package org.cooney.world.items.agents;

public enum Direction {

    UP(0, -1, 0, new int[][][]{
            {{-2, -1}, {-2, -2}, {-2, -3}, {-2, -4}, {-2, -5}, {-2, -6}},
            {{-1, -1}, {-1, -2}, {-1, -3}, {-1, -4}, {-1, -5}, {-1, -6}},
            {{0, -1}, {0, -2}, {0,-3}, {0,-4}, {0,-5}, {0,-6}},
            {{1, -1}, {1, -2}, {1, -3}, {1, -4}, {1, -5}, {1, -6}},
            {{2, -1}, {2, -2}, {2, -3}, {2, -4}, {2, -5}, {2, -6}}
    }),
    DOWN(1, 1, 0, new int[][][]{
            {{-2, 1}, {-2, 2}, {-2, 3}, {-2, 4}, {-2, 5}, {-2, 6}},
            {{-1, 1}, {-1, 2}, {-1, 3}, {-1, 4}, {-1, 5}, {-1, 6}},
            {{0,  1}, {0,  2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}},
            {{1,  1}, {1,  2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}},
            {{2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}},
    }),
    LEFT(2, 0, -1, new int [][][]{
            {{-1, -2}, {-2, -2}, {-3, -2}, {-4, -2}, {-5, -2}, {-6, -2}},
            {{-1, -1}, {-2, -1}, {-3, -1}, {-4, -1}, {-5, -1}, {-6, -1}},
            {{-1,  0}, {-2,  0}, {-3, 0},  {-4, 0},  {-5, 0},  {-6, 0}},
            {{-1,  1}, {-2,  1}, {-3, 1},  {-4, 1},  {-5, 1},  {-6, 1}},
            {{-1,  2}, {-2,  2}, {-3, 2},  {-4, 2},  {-5, 2},  {-6, 2}}
    }),
    RIGHT(3, 0, 1, new int [][][]{
            {{1, -2}, {2, -2}, {3, -2}, {4, -2}, {5, -2}, {6, -2}},
            {{1, -1}, {2, -1}, {3, -1}, {4, -1}, {5, -1}, {6, -1}},
            {{1,  0}, {2,  0}, {3,  0}, {4,  0}, {5,  0}, {6,  0}},
            {{1,  1}, {2,  1}, {3,  1}, {4,  1}, {5,  1}, {6,  1}},
            {{1,  2}, {2,  2}, {3,  2}, {4,  2}, {5,  2}, {6,  2}}
    }),
    STAY_STILL(4, 0, 0, new int [][][]{
            // Stay Still has no related field of vision. Instead, use the previously selected direction.
    });

    private final int index;
    private final int xDirection;
    private final int yDirection;

    private final int[][][] fieldOfVisionCoordinateDeltas;

    Direction(int index, int xDirection, int yDirection, int[][][] fieldOfVisionCoordinateDeltas) {
        this.index = index;
        this.xDirection = xDirection;
        this.yDirection = yDirection;
        this.fieldOfVisionCoordinateDeltas = fieldOfVisionCoordinateDeltas;
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

    public int[][][] getFieldOfVisionCoordinateDeltas() {
        return fieldOfVisionCoordinateDeltas;
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
