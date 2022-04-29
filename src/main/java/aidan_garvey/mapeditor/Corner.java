package aidan_garvey.mapeditor;

public enum Corner {
    NORTH_EAST(0), SOUTH_EAST(1), SOUTH_WEST(2), NORTH_WEST(3), CENTER(4);

    public final int index;

    Corner(int i) {
        index = i;
    }
}
