package aidan_garvey.mapeditor;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileEdDraw {
    private static final double
        MIN_SECTOR_SIZE = 16,
        MAX_SECTOR_SIZE = 256,
        MIN_LINE_WEIGHT = 0.5,
        MAX_LINE_WEIGHT = 2,
        DEFAULT_ZOOM = 0.5,
        START_H_OFFSET = -7.5,
        START_V_OFFSET = -3.5;

    private static final Color
        STROKE_SELECTEDSECTOR = Color.YELLOW,
        STROKE_SECTOR = Color.web("0xFF0000"),
        STROKE_GRIDLINE = Color.web("0x300000"),
        FILL_BACKGROUND = Color.BLACK,
        STROKE_SELECTEDWALL = Color.WHITE,
        FILL_DOOR = Color.web("0x4080FF");

    private boolean gridLinesShown;

    private double camZ, camX, zoom;

    private int floorIndex;

    private GameMap gameMap;

    private final Canvas canvas;

    private Sector currSector;
    private HashMap<Integer, HashMap<Integer, Sector>> currFloor;
    private final HashMap<Sector, List<Direction>> currWalls;

    private DrawMode drawMode;

    public TileEdDraw(Canvas c) {
        canvas = c;
        currFloor = null;
        floorIndex = 0;
        currSector = null;
        gameMap = null;
        currWalls = new HashMap<>();

        gridLinesShown = true;
        drawMode = DrawMode.SECTOR;

        camZ = START_H_OFFSET;
        camX = START_V_OFFSET;
        zoom = DEFAULT_ZOOM;
    }

    public void setDrawMode(DrawMode d) {
        drawMode = d;
    }

    public DrawMode getDrawMode() {
        return drawMode;
    }

    public void deselectWalls() {
        currWalls.clear();
    }

    public HashMap<Sector, List<Direction>> getCurrWalls() {
        return currWalls;
    }

    public void selectWall(double mouseX, double mouseY) {
        Sector s = getClickSector(mouseX, mouseY);

        if (s != null) {
            Direction w = getClickWall(mouseX, mouseY);

            if (!currWalls.containsKey(s))
                currWalls.put(s, new ArrayList<>());

            if (currWalls.get(s).contains(w))
                currWalls.get(s).remove(w);
            else
                currWalls.get(s).add(w);
        }
    }

    public void moveCamH(double h) {
        camZ += h / calcSectorSize();
    }

    public void moveCamV(double v) {
        camX += v / calcSectorSize();
    }

    public void changeZoom(double amount) {
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();

        // find current size of screen in sectors
        double oldWidth = canvasW / calcSectorSize();
        double oldHeight = canvasH / calcSectorSize();

        zoom += amount;
        // ensure range is [0, 1]
        zoom = Math.max(0, zoom);
        zoom = Math.min(1, zoom);

        // get new size of screen in sectors
        double newWidth = canvasW / calcSectorSize();
        double newHeight = canvasH / calcSectorSize();

        // move camera by half of difference in canvas size, so we zoom in on the centre and not the top left
        camZ += (oldWidth - newWidth) / 2d;
        camX += (oldHeight - newHeight) / 2d;

        drawFloor();
    }

    public void switchMap(GameMap m) {
        gameMap = m;
        currSector = null;
    }

    public void switchFloor(int flIndex) {
        currFloor = gameMap.getFloor(flIndex);
        floorIndex = flIndex;
        currSector = null;
    }

    public Sector selectSector(double mouseX, double mouseY)
    {
        Sector newSelection = getClickSector(mouseX, mouseY);
        if (newSelection != null)
        {
            currSector = newSelection;
        }
        else
        {
            currSector = new Sector((int)Math.floor(getMouseZIndex(mouseX)), (int)Math.floor(getMouseXIndex(mouseY)), floorIndex);
        }

        drawFloor();

        return newSelection;
    }

    public Sector getClickSector(double mouseX, double mouseY) {
        // determine coordinates of map that were clicked
        double zIndex = getMouseZIndex(mouseX);
        double xIndex = getMouseXIndex(mouseY);

        // System.out.println("Camera: " + camZ + ", " + camX + "\nClick Index: " + zIndex + ", " + xIndex);

        boolean exists = sectorExists(zIndex, xIndex);

        // if sector exists at location, return it
        if (exists) {
            return getSector(zIndex, xIndex);
        }
        // if sector does not exist at location, return null
        else {
            return null;
        }
    }

    public Direction getClickWall(double mouseX, double mouseY) {
        Sector s = getClickSector(mouseX, mouseY);

        if (s == null) {
            return null;
        }

        // distance from click to north, east, south, west walls
        double[] cardinalDist = {getMouseXIndex(mouseY) - s.getXPos(), s.getZPos() + 1 - getMouseZIndex(mouseX), 0, 0};
        cardinalDist[2] = 1 - cardinalDist[0];
        cardinalDist[3] = 1 - cardinalDist[1];

        int leastIndex = 0;

        for (int i = 1; i < 4; i++) {
            if (cardinalDist[i] < cardinalDist[leastIndex])
                leastIndex = i;
        }

        Direction closest;

        switch (leastIndex) {
            case 1 -> closest = Direction.EAST;
            case 2 -> closest = Direction.SOUTH;
            case 3 -> closest = Direction.WEST;
            default -> closest = Direction.NORTH;
        }

        return closest;
    }

    public double getMouseZIndex(double mouseX) {
        return (mouseX / calcSectorSize()) + camZ;
    }

    public double getMouseXIndex(double mouseY) {
        return (mouseY / calcSectorSize()) + camX;
    }

    public void clearSelectedSector() {
        currSector = null;
    }

    public void applyMainTexture(String tex) {
        for (Sector s : currWalls.keySet()) {
            for (Direction d : currWalls.get(s)) {
                s.setMainTexture(d, tex);
            }
        }
    }

    public void applyAdjTexture(String tex) {
        for (Sector s : currWalls.keySet()) {
            for (Direction d : currWalls.get(s)) {
                s.setAdjTexture(d, tex);
            }
        }
    }

    public void clear() {
        canvas.getGraphicsContext2D().setFill(FILL_BACKGROUND);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setGridLinesShown(boolean isShown) {
        gridLinesShown = isShown;
    }

    public boolean getGridLinesShown() {
        return gridLinesShown;
    }

    // public void drawFloor(double z, double x, double zoom) {
    public void drawFloor() {
        assert zoom >= 0 && zoom <= 1;

        double sectorSize = calcSectorSize();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // find how many rows and columns we can draw
        int gridW = (int)(canvas.getWidth() / sectorSize) + 1;
        int gridH = (int)(canvas.getHeight() / sectorSize) + 2;

        // place we start drawing relative to upper-left corner of canvas
        double xStart = (camX % 1) * -sectorSize;
        double zStart = (camZ % 1) * -sectorSize;

        // clear canvas before drawing over it
        clear();

        // lines will be thinner when more zoomed out
        gc.setLineWidth((MAX_LINE_WEIGHT - MIN_LINE_WEIGHT) * zoom + MIN_LINE_WEIGHT);

        // draw grid lines, if needed
        if (gridLinesShown) {

            gc.setStroke(STROKE_GRIDLINE);

            // draw column lines
            for (int i = 0; i < gridW; i++) {
                gc.strokeLine(zStart + i * sectorSize, 0, zStart + i * sectorSize, canvas.getHeight());
            }

            // draw row lines
            for (int i = 0; i < gridH; i++) {
                gc.strokeLine(0, xStart + i * sectorSize, canvas.getWidth(), xStart + i * sectorSize);
            }
        }

        // calculate leftmost and rightmost tiles we can show
        int leftBound = (int)camZ;
        int rightBound = leftBound + gridW;
        // calculate highest and lowest tiles we can show
        int upperBound = (int)camX;
        int lowerBound = upperBound + gridH;

        // draw on-screen sectors
        List<Sector> onScreenSectors = getOnScreen(leftBound, upperBound, rightBound, lowerBound);
        gc.setStroke(STROKE_SECTOR);

        for (Sector s : onScreenSectors) {
            double sectorZ = zStart + (s.getZPos() - leftBound) * sectorSize;
            double sectorX = xStart + (s.getXPos() - upperBound) * sectorSize;

            drawSector(sectorZ, sectorX, sectorSize, s);
        }

        // draw selected sector
        if ((drawMode == DrawMode.SECTOR || drawMode == DrawMode.SURFACE) && currSector != null) {
            double sectorZ = zStart + (currSector.getZPos() - leftBound) * sectorSize;
            double sectorX = xStart + (currSector.getXPos() - upperBound) * sectorSize;

            gc.setStroke(STROKE_SELECTEDSECTOR);
            drawSector(sectorZ, sectorX, sectorSize);
        }
        // draw selected walls
        else if (drawMode == DrawMode.TEXTURE) {
            gc.setStroke(STROKE_SELECTEDWALL);

            for (Sector s : currWalls.keySet()) {
                double sectorZ = zStart + (s.getZPos() - leftBound) * sectorSize;
                double sectorX = xStart + (s.getXPos() - upperBound) * sectorSize;

                drawSelectedWalls(sectorZ, sectorX, sectorSize, currWalls.get(s));
            }
        }
    }

    // draw sector at given spot, in canvas' current colour
    private void drawSector(double sectorZ, double sectorX, double sectorSize) {
        canvas.getGraphicsContext2D().strokeRect(sectorZ, sectorX, sectorSize, sectorSize);
    }

    // draw sector, except for adjoined walls
    private void drawSector(double sectorZ, double sectorX, double sectorSize, Sector s) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // draw a door, if present
        if (s.hasDoor()) {
            double height = s.doorIsNorthSouth() ? sectorSize : sectorSize/16.0;
            double width = s.doorIsNorthSouth() ? sectorSize/16.0 : sectorSize;

            double xStart = 0, yStart = 0;
            // door is in the north or east
            if (s.getDoorPos() == 0) {
                if (s.doorIsNorthSouth())
                    xStart = sectorSize * 15.0 / 16.0;
            }
            // door is in middle of sector
            else if (s.getDoorPos() == 1) {
                if (s.doorIsNorthSouth())
                    xStart = sectorSize * 15.0 / 32.0;
                else
                    yStart = sectorSize * 15.0 / 32.0;
            }
            // door is in the south or west
            else if (s.getDoorPos() == 2) {
                if (!s.doorIsNorthSouth())
                    yStart = sectorSize * 15.0 / 16.0;
            }

            gc.setFill(FILL_DOOR);
            gc.fillRect(sectorZ + xStart, sectorX + yStart, width, height);
        }

        // draw top line
        if (!s.getWallAdjoin(Direction.NORTH))
            gc.strokeLine(sectorZ, sectorX, sectorZ + sectorSize, sectorX);
        // draw bottom line
        if (!s.getWallAdjoin(Direction.SOUTH))
            gc.strokeLine(sectorZ, sectorX + sectorSize, sectorZ + sectorSize, sectorX + sectorSize);
        // draw right line
        if (!s.getWallAdjoin(Direction.EAST))
            gc.strokeLine(sectorZ + sectorSize, sectorX, sectorZ + sectorSize, sectorX + sectorSize);
        // draw left line
        if (!s.getWallAdjoin(Direction.WEST))
            gc.strokeLine(sectorZ, sectorX, sectorZ, sectorX + sectorSize);
    }

    private void drawSelectedWalls(double sectorZ, double sectorX, double sectorSize, List<Direction> selected) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // draw top line
        if (selected.contains(Direction.NORTH)) {
            gc.strokeLine(sectorZ, sectorX, sectorZ + sectorSize, sectorX);
            gc.strokeLine(sectorZ + sectorSize/2d, sectorX, sectorZ + sectorSize/2d, sectorX + sectorSize/4d);
        }
        // draw bottom line
        if (selected.contains(Direction.SOUTH)) {
            gc.strokeLine(sectorZ, sectorX + sectorSize, sectorZ + sectorSize, sectorX + sectorSize);
            gc.strokeLine(sectorZ + sectorSize/2d, sectorX + sectorSize, sectorZ + sectorSize/2d, sectorX + sectorSize*3d/4d);
        }
        // draw right line
        if (selected.contains(Direction.EAST)) {
            gc.strokeLine(sectorZ + sectorSize, sectorX, sectorZ + sectorSize, sectorX + sectorSize);
            gc.strokeLine(sectorZ + sectorSize, sectorX + sectorSize/2d, sectorZ + sectorSize*3d/4d, sectorX + sectorSize/2d);
        }
        // draw left line
        if (selected.contains(Direction.WEST)) {
            gc.strokeLine(sectorZ, sectorX, sectorZ, sectorX + sectorSize);
            gc.strokeLine(sectorZ, sectorX + sectorSize/2d, sectorZ + sectorSize/4d, sectorX + sectorSize/2d);
        }
    }

    public Sector makeSector()
    {
        if (currSector != null) putInFloor(currSector);
        return currSector;
    }

    private boolean sectorExists(double z, double x){
        return gameMap.sectorExists((int)Math.floor(z), (int)Math.floor(x), floorIndex);
    }

    private Sector getSector(double z, double x) {
        return gameMap.getSector((int)Math.floor(z), (int)Math.floor(x), floorIndex);
    }

    private double calcSectorSize() {
        return (MAX_SECTOR_SIZE - MIN_SECTOR_SIZE) * zoom * zoom + MIN_SECTOR_SIZE;
    }

    private List<Sector> getOnScreen(int z1, int x1, int z2, int x2) {
        ArrayList<Sector> onScreen = new ArrayList<>();

        // add all sectors in given range
        for (HashMap<Integer, Sector> hm : currFloor.values()) {
            for (Sector s : hm.values()) {
                if (s.getZPos() >= z1 && s.getZPos() <= z2 && s.getXPos() >= x1 && s.getXPos() <= x2) {
                    onScreen.add(s);
                }
            }
        }

        return onScreen;
    }

    private void putInFloor(Sector s) {
        if (sectorExists(s.getZPos(), s.getXPos())) {
            System.err.println("ERROR: Sector already in map");
        }
        else {
            currFloor.computeIfAbsent(s.getZPos(), k -> new HashMap<>());
            currFloor.get(s.getZPos()).put(s.getXPos(), s);
        }
    }
}
