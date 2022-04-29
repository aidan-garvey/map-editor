package aidan_garvey.mapeditor;

public class Sector {
    private static final String TEX_DEFAULT = "testTexture1.png";

    private final Wall northWall, eastWall, southWall, westWall;
    private final Surface floor, ceiling; // altitude data for floor and ceiling
    private String floorTexture, ceilingTexture;
    private boolean floorAdjoin, ceilingAdjoin; // is the sector connected vertically through the floor/ceiling?
    private boolean ceilingIsSky;
    private Door door; // the door in this sector (null if one does not exist)

    private final int zPos, xPos, yPos;

    // default sector
    public Sector (int z, int x, int y) {
        northWall = new Wall(TEX_DEFAULT);
        southWall = new Wall(TEX_DEFAULT);
        eastWall = new Wall(TEX_DEFAULT);
        westWall = new Wall(TEX_DEFAULT);

        floor = new Surface();
        ceiling = new Surface();

        floorTexture = TEX_DEFAULT;
        ceilingTexture = TEX_DEFAULT;

        floorAdjoin = ceilingAdjoin = false;
        ceilingIsSky = false;

        // by default, no door in the sector
        door = null;

        zPos = z;
        xPos = x;
        yPos = y;
    }

    public int getZPos() {
        return zPos;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setWallAdjoin(Direction whichWall, boolean val) {
        decodeWall(whichWall).adjoin = val;
    }

    public boolean getWallAdjoin(Direction whichWall) {
        // all four walls should exist
        assert decodeWall(whichWall) != null;
        return decodeWall(whichWall).adjoin;
    }

    public void setWallBlocksMovement(Direction whichWall, boolean val) {
        decodeWall(whichWall).blocksMovement = val;
    }

    public boolean getWallBlocksMovement(Direction which) {
        return decodeWall(which).blocksMovement;
    }

    public void setWallBlocksProjectiles(Direction which, boolean val) {
        decodeWall(which).blocksProjectiles = val;
    }

    public boolean getWallBlocksProjectiles(Direction which) {
        return decodeWall(which).blocksProjectiles;
    }

    public void setAlignToFloor(Direction which, boolean val) { decodeWall(which).alignFloor = val; }

    public boolean getAlignToFloor(Direction which) { return decodeWall(which).alignFloor; }

    public void setAdjAlignToFloor(Direction which, boolean val) {
        decodeWall(which).adjAlignFloor = val;
    }

    public boolean getAdjAlignToFloor(Direction which) { return decodeWall(which).adjAlignFloor; }

    public void setMainHFlip(Direction which, boolean val) {
        decodeWall(which).hFlip = val;
    }

    public boolean getMainHFlip(Direction which) {
        return decodeWall(which).hFlip;
    }

    public void setMainVFlip(Direction which, boolean val) {
        decodeWall(which).vFlip = val;
    }

    public boolean getMainVFlip(Direction which) {
        return decodeWall(which).vFlip;
    }

    public void setAdjHFlip(Direction which, boolean val) {
        decodeWall(which).adjHFlip = val;
    }

    public boolean getAdjHFlip(Direction which) {
        return decodeWall(which).adjHFlip;
    }

    public void setAdjVFlip(Direction which, boolean val) {
        decodeWall(which).adjVFlip = val;
    }

    public boolean getAdjVFlip(Direction which) {
        return decodeWall(which).adjVFlip;
    }

    public boolean isFloorFlush() {
        return floor.isFlush();
    }

    public boolean isCeilingFlush() {
        return ceiling.isFlush();
    }

    public void setFloorAdjoin(boolean val) {
        floorAdjoin = val;
    }

    public boolean getFloorAdjoin() {
        return floorAdjoin;
    }

    public void setCeilingAdjoin(boolean val) {
        ceilingAdjoin = val;
    }

    public boolean getCeilingAdjoin() {
        return ceilingAdjoin;
    }

    public void setSky(boolean val) {
        ceilingIsSky = val;
    }

    public boolean getSky() {
        return ceilingIsSky;
    }

    public void setMainTexture(Direction which, String texName) {
        decodeWall(which).mainTextureName = texName;
    }

    public String getMainTexture(Direction which) {
        return decodeWall(which).mainTextureName;
    }

    public void setAdjTexture(Direction which, String texName) {
        decodeWall(which).adjTextureName = texName;
    }

    public String getAdjTexture(Direction which) { return decodeWall(which).adjTextureName; }

    public void setFloorTexture(String texName) { floorTexture = texName; }

    public String getFloorTexture() {return floorTexture;}

    public void setCeilingTexture(String texName) { ceilingTexture = texName; }

    public String getCeilingTexture() {return ceilingTexture;}

    public void addDoor(boolean northSouth, boolean vertical) {
        door = new Door(northSouth, 1, vertical, DoorType.UP_LEFT, 0.5, false, TEX_DEFAULT, TEX_DEFAULT, null);
    }

    public void removeDoor() {
        door = null;
    }

    public boolean hasDoor() {
        return door != null;
    }

    public void setDoorNorthSouth(boolean northSouth) {
        if (door != null)
            door.facingNorthSouth = northSouth;
    }

    public boolean doorIsNorthSouth() {
        return door == null || door.facingNorthSouth;
    }

    public void setDoorPos(int p) {
        if (door != null)
            door.doorPosition = p;
    }

    public int getDoorPos() {
        if (door == null)
            return 0;
        else
            return door.doorPosition;
    }

    public void setDoorVertical(boolean v) {
        if (door != null)
            door.opensVertical = v;
    }

    public boolean doorIsVertical() {
        return door == null || door.opensVertical;
    }

    public void setDoorType(DoorType t) {
        if (door != null)
            door.type = t;
    }

    public DoorType getDoorType() {
        return door == null ? DoorType.UP_LEFT : door.type;
    }

    public void setDoorSpeed(double s) {
        if (this.door != null) {
            door.openingTime = Math.max(0, s);
        }
    }

    public double getDoorSpeed() {
        return this.door == null ? 0.0 : door.openingTime;
    }

    public void setDoorShootToOpen(boolean s) {
        if (this.door != null) door.shootToOpen = true;
    }

    public boolean getDoorShootToOpen() { return this.door != null && door.shootToOpen; }

    public void setDoorTexture1(String s) {
        if (this.door != null)
            door.texture1 = s;
    }

    public String getDoorTexture1() {
        return this.door == null ? null : door.texture1;
    }

    public void setDoorTexture2(String s) {
        if (this.door != null)
            door.texture2 = s;
    }

    public String getDoorTexture2() {
        return this.door == null ? null : door.texture2;
    }

    public void setDoorKey(String s) {
        if (this.door != null)
            door.keyName = s;
    }

    public String getDoorKey() {
        return this.door == null ? null : door.keyName;
    }

    public int[] getCeilingOffsets() {return ceiling.corners;}
    public int[] getFloorOffsets() {return floor.corners;}

    private Wall decodeWall(Direction which) {
        Wall result = null;

        switch(which) {
            case NORTH -> result = northWall;
            case SOUTH -> result = southWall;
            case EAST -> result = eastWall;
            case WEST -> result = westWall;
        }

        return result;
    }
}

class Wall {
    boolean adjoin; // are the two sectors on either side of this wall connected?
    String mainTextureName; // name of the wall texture
    String adjTextureName; // name of wall texture shown when adjoined sector has different floor/ceiling height
    boolean alignFloor; // is the texture drawn bottom-up from the floor, or top-down from the ceiling?
    boolean adjAlignFloor; // alignFloor option for adjoin texture
    boolean blocksMovement; // can this wall be walked through
    boolean blocksProjectiles; // can this wall be shot through
    boolean hFlip, vFlip, adjHFlip, adjVFlip; // texture and adjoin texture is flipped horizontally or vertically

    Wall (String mainTex) {
        mainTextureName = mainTex;
        adjoin = false;
        adjTextureName = null;
        alignFloor = adjAlignFloor = false;
        blocksMovement = blocksProjectiles = false;
        hFlip = vFlip = adjHFlip = adjVFlip = false;
    }
}

// for a floor or ceiling, store height offsets for each corner of the surface and the centre
// corner order (see Corner.java): North-East, South-East, South-West, North-West, Centre
class Surface {
    static final int NUM_CORNERS = 5;
    final int[] corners = new int[NUM_CORNERS];

    Surface (int[] c) {
        System.arraycopy(c, 0, corners, 0, NUM_CORNERS);
    }

    Surface () {
        for (int i = 0; i < NUM_CORNERS; i++) {
            corners[i] = 0;
        }
    }

    boolean isFlush() {
        return corners[0] == 0 && corners[1] == 0 && corners[2] == 0 && corners[3] == 0 && corners[4] == 0;
    }
}

class Door {
    boolean facingNorthSouth; // is the door facing north/south or east/west
    int doorPosition; // 0 = left/top wall, 1 = middle of sector, 2 = right/bottom wall
    boolean opensVertical; // which way does the door open?
    DoorType type; // door can open (depending on v/h): up/left, down/right, in the middle
    double openingTime; // time in seconds for door to open
    boolean shootToOpen; // can the door be opened by shooting it
    String texture1, texture2;
    String keyName;


    Door (boolean fns, int doorPos, boolean vert, DoorType t, double openTime, boolean shootOpen, String tex1, String tex2, String key) {
        facingNorthSouth = fns;
        doorPosition = doorPos;
        opensVertical = vert;
        type = t;
        openingTime = openTime;
        shootToOpen = shootOpen;
        texture1 = tex1;
        texture2 = tex2;
        keyName = key;
    }
}