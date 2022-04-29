package aidan_garvey.mapeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameMap {
    private final ArrayList<String> keys;
    private final ArrayList<HashMap<Integer, HashMap<Integer, Sector>>> floors;
    private int lowestFloor;  // determines floor number of floors.get(0)

    public GameMap() {
        lowestFloor = 0;

        // add default floor for empty maps

        // create default sector
        Sector defaultSec = new Sector(0, 0, 0);

        // create column for sectors, add default sector
        HashMap<Integer, Sector> col = new HashMap<>();
        col.put(0, defaultSec);

        // create row of columns, add default column
        HashMap<Integer, HashMap<Integer, Sector>> defaultFloor = new HashMap<>();
        defaultFloor.put(0, col);

        // create list of floors, add default floor
        floors = new ArrayList<>();
        floors.add(defaultFloor);

        // create list of keys used in the map (when loading map files is implemented,
        // include the names of every key and every locked door's key)
        keys = new ArrayList<>();
    }

    // todo: when saving the map, check for empty floors and delete them
    public HashMap<Integer, HashMap<Integer, Sector>> getFloor(int index) {
        while(index - lowestFloor < 0) {
            floors.add(0, new HashMap<>());
            --lowestFloor;
        }

        while (index - lowestFloor >= floors.size()) {
            floors.add(new HashMap<>());
        }

        return floors.get(index - lowestFloor);
    }

    // add a new key to the list of keys on the map
    public void addKey(String s) {
        if (!keys.contains(s))
            keys.add(s);
    }

    // todo: might need to replace with returning a copy
    public List<String> getKeys() {
        return keys;
    }

    public boolean canAdjoinWall(Sector s, Direction whichWall) {
        int zOff = 0, xOff = 0;
        switch (whichWall) {
            case NORTH -> xOff = -1;
            case SOUTH -> xOff = 1;
            case EAST -> zOff = 1;
            case WEST -> zOff = -1;
        }

        return sectorExists(s.getZPos() + zOff, s.getXPos() + xOff, s.getYPos());
    }

    public void setWallAdjoin(Sector s, Direction whichWall, boolean val) {
        int zOff = 0, xOff = 0;
        switch (whichWall) {
            case NORTH -> xOff = -1;
            case SOUTH -> xOff = 1;
            case EAST -> zOff = 1;
            case WEST -> zOff = -1;
        }

        // if adjacent sector exists
        if (sectorExists(s.getZPos() + zOff, s.getXPos() + xOff, s.getYPos())) {
            // get sector on other side of current wall
            Sector t = getSector(s.getZPos() + zOff, s.getXPos() + xOff, s.getYPos());

            // if wall is being un-adjoined, disable all adjoin-specific options for both walls
            if (!val) {
                // each function here performs the operation for this wall and the opposite wall
                setBlocksMovement(s, whichWall, false);
                setBlocksProjectiles(s, whichWall, false);
                setAdjAlignToFloor(s, whichWall, false);
            }
            // if the wall is being adjoined, set the texture on both sides to null (most likely to be what the user wants)
            else {
                s.setMainTexture(whichWall, null);
                t.setMainTexture(Direction.oppositeDirection(whichWall), null);
            }

            // adjoin this wall and the adjacent sector's wall
            s.setWallAdjoin(whichWall, val);
            // for sector on other side of wall, set corresponding wall's adjoin value
            t.setWallAdjoin(Direction.oppositeDirection(whichWall), val);

        }
        // if no adjacent sector exists, do nothing and indicate failure
    }

    public void setBlocksMovement(Sector s, Direction whichWall, boolean val) {
        int zOff = 0, xOff = 0;
        switch (whichWall) {
            case NORTH -> xOff = -1;
            case SOUTH -> xOff = 1;
            case EAST -> zOff = 1;
            case WEST -> zOff = -1;
        }

        // if wall is adjoined, this operation can be done
        if (s.getWallAdjoin(whichWall)) {
            s.setWallBlocksMovement(whichWall, val);
            getSector(s.getZPos() + zOff, s.getXPos() + xOff, s.getYPos()).setWallBlocksMovement(Direction.oppositeDirection(whichWall), val);

        }
        // if wall wasn't adjoined, this operation fails
    }

    public void setBlocksProjectiles(Sector s, Direction whichWall, boolean val) {
        int zOff = 0, xOff = 0;
        switch (whichWall) {
            case NORTH -> xOff = -1;
            case SOUTH -> xOff = 1;
            case EAST -> zOff = 1;
            case WEST -> zOff = -1;
        }

        // if wall is adjoined, this operation can be done
        if (s.getWallAdjoin(whichWall)) {
            s.setWallBlocksProjectiles(whichWall, val);
            getSector(s.getZPos() + zOff, s.getXPos() + xOff, s.getYPos()).setWallBlocksProjectiles(Direction.oppositeDirection(whichWall), val);

        }
        // if wall wasn't adjoined, this operation fails
    }

    public void setAlignToFloor(Sector s, Direction whichWall, boolean toFloor) {
        s.setAlignToFloor(whichWall, toFloor);
    }

    public void setAdjAlignToFloor(Sector s, Direction whichWall, boolean toFloor) {
        // can only be done if wall is adjoined
        if (s.getWallAdjoin(whichWall)) {
            s.setAdjAlignToFloor(whichWall, toFloor);
        }
    }

    public void setFloorAdjoin(Sector s, boolean val) {
        // can only be done if:
            // no part of the floor is raised
            // there is a sector right below this one
            // that sector's ceiling is not lowered at any point, nor a sky
        if (s.isFloorFlush()
                && sectorExists(s.getZPos(), s.getXPos(), s.getYPos() - 1)
                && getSector(s.getZPos(), s.getXPos(), s.getYPos() - 1).isCeilingFlush()
                && !getSector(s.getZPos(), s.getXPos(), s.getYPos() - 1).getSky())
        {
            s.setFloorAdjoin(val);
            getSector(s.getZPos(), s.getXPos(), s.getYPos() - 1).setCeilingAdjoin(val);
        }
        // if conditions weren't met, operation failed

        // note: consider a "forceFloorAdjoin" which only checks for a sector below and clears any modified floor/ceiling height
            // if setFloorAdjoin fails due to heights, a dialog could be shown that asks the user if they want to force the adjoin
    }

    public void setCeilingAdjoin(Sector s, boolean val) {
        // can only be done if:
            // no part of the ceiling is lowered, and the ceiling is not a sky
            // there is a sector right above this one
            // that sector's floor is not raised at any point
        if (s.isCeilingFlush() && !s.getSky()
                && sectorExists(s.getZPos(), s.getXPos(), s.getYPos() + 1)
                && getSector(s.getZPos(), s.getXPos(), s.getYPos() + 1).isFloorFlush())
        {
            s.setCeilingAdjoin(val);
            getSector(s.getZPos(), s.getXPos(), s.getYPos() + 1).setFloorAdjoin(val);
        }
        // if conditions weren't met, operation failed
    }

    public void setCeilingIsSky(Sector s, boolean val) {
        // can't be done if ceiling is adjoined (adjoined ceilings have no texture)
        if (!s.getCeilingAdjoin()) {
            s.setSky(val);
        }
    }

    public boolean sectorExists(int z, int x, int y) {
        return getFloor(y) != null && getFloor(y).get(z) != null && getFloor(y).get(z).get(x) != null;
    }

    public Sector getSector(int z, int x, int y) {
        return sectorExists(z, x, y) ? getFloor(y).get(z).get(x) : null;
    }

    // toggle any options for adjacent sectors such as wall adjoins, then remove the sector
    public void removeSector(int z, int x, int y) {
        Sector toRemove = getSector(z, x, y);

        if (toRemove != null) {
            setFloorAdjoin(toRemove, false);
            setCeilingAdjoin(toRemove, false);

            for (Direction d : Direction.values()) {
                // also removes any blocksMovement or blocksProjectiles on adjacent walls
                setWallAdjoin(toRemove, d, false);
            }

            getFloor(y).get(z).remove(x);
        }
    }

    public void removeSector(Sector toRemove) {
        removeSector(toRemove.getZPos(), toRemove.getXPos(), toRemove.getYPos());
    }

    // ***** Door-related methods ***** //

    // todo: I'm probably going to have to devise a way to link multiple doors in terms of activation (maybe a separate map mode?)

    // Add a door to the sector if possible.
    // There must be walls on both sides of a door.
    // Tries to place a door in the following places:
        // Vertical (requires floor and ceiling)
        // Horizontal north-south (needs north + south walls)
        // Horizontal east-west (needs east + west walls)
    // If none are possible, door is not placed.
    public void addDoor(Sector s) {
        if (s != null)
            s.addDoor(true, true);
    }

    public void removeDoor(Sector s) {
        if (s != null) {
            s.removeDoor();
        }
    }

    public boolean hasDoor(Sector s){
        return s != null && s.hasDoor();
    }

    // toggle door orientation b/w north-south and east-west
    public void toggleDoorOrientation(Sector s) {
        s.setDoorNorthSouth(!s.doorIsNorthSouth());
    }

    public boolean doorIsNorthSouth(Sector s) {
        return s == null || s.doorIsNorthSouth();
    }

    public void setDoorPos(Sector s, int pos) {
        if (s != null)
            s.setDoorPos(pos);
    }

    public int getDoorPos(Sector s) {
        return s == null ? 0 : s.getDoorPos();
    }

    public void setDoorVertical(Sector s, boolean vertical) {
        if (s != null)
            s.setDoorVertical(vertical);
    }

    public boolean doorIsVertical(Sector s) {
        return s == null || s.doorIsVertical();
    }

    public void setDoorType(Sector s, int type) {
        if (s != null) {
            DoorType dt;

            switch (type) {
                case 1 -> dt = DoorType.MIDDLE;
                case 2 -> dt = DoorType.DOWN_RIGHT;
                default -> dt = DoorType.UP_LEFT;
            }

            s.setDoorType(dt);
        }
    }

    public int getDoorType(Sector s) {
        if (s == null) return 0;

        int type;
        switch(s.getDoorType()) {
            case MIDDLE -> type = 1;
            case DOWN_RIGHT -> type = 2;
            default -> type = 0;
        }

        return type;
    }

    public void setDoorSpeed(Sector s, double speed) {
        if (s != null)
            s.setDoorSpeed(speed);
    }

    public double getDoorSpeed(Sector s) {
        return s == null ? 0.0 : s.getDoorSpeed();
    }

    public void setDoorTex1(Sector s, String t) {
        if (s != null)
            s.setDoorTexture1(t);
    }

    public String getDoorTex1(Sector s) {
        return s == null ? null : s.getDoorTexture1();
    }

    public void setDoorTex2(Sector s, String t) {
        if (s != null)
            s.setDoorTexture2(t);
    }

    public String getDoorTex2(Sector s) {
        return s == null ? null : s.getDoorTexture2();
    }

    public void setDoorKey(Sector s, String k) {
        if (s != null) {
            s.setDoorKey(k);
        }
    }

    public String getDoorKey(Sector s) {
        return s == null ? null : s.getDoorKey();
    }
}
