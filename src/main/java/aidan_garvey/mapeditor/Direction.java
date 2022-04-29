package aidan_garvey.mapeditor;

public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public static Direction oppositeDirection (Direction d) {
        return switch (d) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }
}
