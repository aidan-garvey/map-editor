package aidan_garvey.mapeditor;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapEdController {
    @FXML
    private Button
            // texture file browser buttons
            adjoinTextureFileButton,
            mainTextureFileButton,
            floorTextureFileButton,
            ceilingTextureFileButton,

            newSectorButton,
            deleteSectorButton,

            floorUpButton,
            floorDownButton,
            gridLinesButton,

            // buttons to remove the texture on a surface
            clearMainTextureButton,
            clearAdjTextureButton,
            clearFloorTextureButton,
            clearCeilingTextureButton
    ;

    @FXML
    private GridPane
            mainWallOptions,
            adjoinedWallOptions
    ;

    @FXML
    private ToggleGroup modeSelection; // switch between sector, surface and texture mode

    @FXML
    private VBox sectorSettings, doorOptionsContainer;

    @FXML
    private ScrollPane tileScrollPane; // primary menu, contents depend on mode

    @FXML
    private Canvas tileEdCanvas; // where the map is drawn

    @FXML
    private StackPane
            // Wraps the entire UI
            tileEdWrapper,
            // Options in the left menu when an empty sector is selected (just a button to make new sector)
            newSectorMenu,
            // the area of the UI where the map is displayed
            tileEdDisplayRegion
    ;

    @FXML
    private ToggleButton
            northWallButton,
            southWallButton,
            eastWallButton,
            westWallButton
    ;

    @FXML
    private RadioButton
            sectorModeButton,
            surfaceModeButton,
            textureModeButton
    ;

    @FXML
    private CheckBox
            wallAdjoinButton,
            movementBlockButton,
            projectileBlockButton,
            texAlignButton,
            adjoinTexAlignButton,
            adjoinFloorButton,
            adjoinCeilingButton,
            toggleSkyButton,
            mainHFlipButton,
            mainVFlipButton,
            adjHFlipButton,
            adjVFlipButton
    ;

    @FXML
    private ImageView axisGuideBox; // displays the X and Z axes in the map editor, and the direction of North

    @FXML
    private Text
            currentFloorNumber,
            mousePosZ, // mouse Z position in map coordinates
            mousePosX, // mouse X position in map coordinates
            fileNameText, // name of map being edited
            modifiedText // says "modified" if the map has been modified since saving
            // numWallsSelected
    ;

    @FXML
    // These TextFields display a surface's texture and allow the user to type one in to use
    private TextField
            mainTextureName,
            adjoinTextureName,
            ceilingTextureName,
            floorTextureName
    ;

    private static final double ZOOM_SENSITIVITY = 0.001;

    public static final String
            // File names for in-app graphics
            FOLDER_ICON_PATH = "FolderIcon.png",
            AXIS_GUIDE_PATH = "AxisGuideCompass.png",
            ARROW_UP_PATH = "UpArrow.png",
            ARROW_DOWN_PATH = "DownArrow.png",
            CANCEL_ICON_PATH = "Cancel.png",
            KEY_ICON_PATH = "Key.png",
            TILE_GUIDE_PATH = "TileGuide.png",
            SPINNERS_HELP_PATH = "SpinnersHelp.png",
            EAST_GUIDE_PATH = "East.png",
            NORTH_GUIDE_PATH = "North.png",
            SOUTH_GUIDE_PATH = "South.png",
            WEST_GUIDE_PATH = "West.png",
            // Other
            TEXTURE_NA = "----",
            TEXTURE_NULL = "(none)",
            TEXTURE_DIR = "Textures/",
            TEXTURE_EXT = ".png"
    ;

    private final Image
            FILE_ICON = new Image(new FileInputStream(FOLDER_ICON_PATH)),
            AXIS_GUIDE = new Image(new FileInputStream(AXIS_GUIDE_PATH)),
            ARROW_UP = new Image(new FileInputStream(ARROW_UP_PATH)),
            ARROW_DOWN = new Image(new FileInputStream(ARROW_DOWN_PATH)),
            CANCEL_ICON = new Image(new FileInputStream(CANCEL_ICON_PATH))
    ;

    private Stage stage;

    private TileEdDraw tileEdDraw;

    private GameMap currMap;
    private Sector currSector;

    private final SurfaceModeOptions surfaceModeOptions;
    private final TextureModeOptions textureModeOptions;
    private final DoorOptions doorOptions;

    private double lastMouseX, lastMouseY;
    private boolean isDragging, shiftHeld;
    private boolean needsRefresh; // does the canvas need to be re-drawn?
    private boolean modified; // has file been modified since saving?

    private int currFloor;

    public void setStage(Stage s) {
        stage = s;
    }

    public Scene getScene() {return stage.getScene();}

    public Sector getCurrSector() {return currSector;}

    public void setNeedsRefresh() {needsRefresh = true;}

    public MapEdController() throws FileNotFoundException {
        stage = null;
        currMap = new GameMap();

        doorOptions = new DoorOptions(this);
        doorOptions.setGameMap(currMap);
        textureModeOptions = new TextureModeOptions(this);
        surfaceModeOptions = new SurfaceModeOptions(this);

        lastMouseX = 0; lastMouseY = 0;
        isDragging = false;
        needsRefresh = false;
        modified = true;

        currSector = null;
        currFloor = 0;

        newSectorButton = new Button("New Sector");
        newSectorButton.setOnAction(actionEvent -> {
            // displayTileOptions(tileEdDraw.makeSector()); // update tile options for new sector
            currSector = tileEdDraw.makeSector();
            displayTileOptions(currSector != null);
            setIsModified();
        });
        newSectorMenu = new StackPane();
        newSectorMenu.getChildren().add(newSectorButton);
        newSectorMenu.setPadding(new Insets(2.5, 2.5, 2.5, 2.5));
    }

    @FXML
    public void initialize() {
        tileEdCanvas.widthProperty().bind(tileEdWrapper.widthProperty());
        tileEdCanvas.heightProperty().bind(tileEdWrapper.heightProperty());

        // now that canvas is ready, pass it to ted
        tileEdDraw = new TileEdDraw(tileEdCanvas);
        textureModeOptions.setTileEdDraw(tileEdDraw);
        // doorOptions.setTileEdDraw(tileEdDraw);

        initUIControls();

        tileScrollPane.setContent(null);

        mainWallOptions.setDisable(true);

        // make floor and ceiling texture TextFields use width of the wall TextField
        floorTextureName.maxWidthProperty().bind(mainTextureName.widthProperty());
        ceilingTextureName.maxWidthProperty().bind(mainTextureName.widthProperty());

        mainTextureFileButton.setGraphic(new ImageView(FILE_ICON));
        adjoinTextureFileButton.setGraphic(new ImageView(FILE_ICON));
        floorTextureFileButton.setGraphic(new ImageView(FILE_ICON));
        ceilingTextureFileButton.setGraphic(new ImageView(FILE_ICON));

        axisGuideBox.setImage(AXIS_GUIDE);
        currentFloorNumber.setText(String.format("%d", currFloor));
        floorUpButton.setGraphic(new ImageView(ARROW_UP));
        floorDownButton.setGraphic(new ImageView(ARROW_DOWN));

        gridLinesButton.setText("Hide Grid Lines");

        clearMainTextureButton.setGraphic(new ImageView(CANCEL_ICON));
        clearAdjTextureButton.setGraphic(new ImageView(CANCEL_ICON));
        clearFloorTextureButton.setGraphic(new ImageView(CANCEL_ICON));
        clearCeilingTextureButton.setGraphic(new ImageView(CANCEL_ICON));

        modeSelection.selectToggle(sectorModeButton);

        doorOptionsContainer.getChildren().add(doorOptions);
        doorOptions.setDoorOptions();
    }

    private void initUIControls() {
        initUISelection();
        initScrollZoom();
    }

    private void initUISelection() {
        // when drag starts, get initial mouse position
        tileEdWrapper.setOnDragDetected(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY) {
                isDragging = true;
                lastMouseX = dragEvent.getX();
                lastMouseY = dragEvent.getY();
            }
        });

        // while mouse is being dragged, move the map
        tileEdWrapper.setOnMouseDragged(dragEvent -> {
            if (isDragging) {
                tileEdDraw.moveCamH((lastMouseX - dragEvent.getX()));
                tileEdDraw.moveCamV((lastMouseY - dragEvent.getY()));

                tileEdDraw.drawFloor();

                lastMouseX = dragEvent.getX();
                lastMouseY = dragEvent.getY();
            }
        });

        // when mouse is released, treat it as a click or releasing the drag
        tileEdWrapper.setOnMouseReleased(mouseEvent -> {
            needsRefresh = true; // make sure map will be redrawn next frame

            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                // release mouse drag
                if (isDragging) {
                    isDragging = false;
                }
                // release left click
                else {
                    // for sector and surface mode, select the sector where user clicked
                    if (!textureModeButton.isSelected()) {
                        currSector = tileEdDraw.selectSector(mouseEvent.getX(), mouseEvent.getY());
                        displayTileOptions(currSector != null);
                        // displayTileOptions(tileEdDraw.updateSelectedSector(mouseEvent.getX(), mouseEvent.getY()));
                    }

                    // for texture mode, select closest wall to where user clicked
                    else {
                        // if user isn't holding shift, user will only select the new sector
                        if (!shiftHeld)
                            tileEdDraw.deselectWalls();
                        // select closest wall to mouse (or deselect if already selected)
                        tileEdDraw.selectWall(mouseEvent.getX(), mouseEvent.getY());
                        tileEdDraw.drawFloor();

                        textureModeOptions.setTextureModeOptions();
                    }

                }
            }
            else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                // release right click
                if (tileEdDraw.getDrawMode() != DrawMode.TEXTURE) {
                    currSector = null;
                    tileEdDraw.clearSelectedSector();
                    tileScrollPane.setContent(null);
                }
                else {
                    tileEdDraw.deselectWalls();
                    textureModeOptions.setTextureModeOptions();
                }
            }
        });

        // display map coordinates at mouse position in the bottom right
        tileEdWrapper.setOnMouseMoved(mouseEvent -> {
            mousePosZ.setText(String.format("z: %.2f", tileEdDraw.getMouseZIndex(mouseEvent.getX())));
            mousePosX.setText(String.format("x: %.2f", tileEdDraw.getMouseXIndex(mouseEvent.getY())));
        });

        tileEdWrapper.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case SHIFT -> shiftHeld = true;
            }
        });
        tileEdWrapper.setOnKeyReleased(keyEvent -> {
            switch (keyEvent.getCode()) {
                case SHIFT -> shiftHeld = false;
            }
        });

        // if the user clicks in tileOptions, consume the click so it doesn't go to tileEdWrapper
        // prevents a click in tileOptions that does not hit a button from selecting a new sector
        tileScrollPane.setOnMouseReleased(Event::consume);
    }

    private void initScrollZoom() {
        tileEdDisplayRegion.setOnScroll(
                scrollEvent -> tileEdDraw.changeZoom(scrollEvent.getDeltaY() * ZOOM_SENSITIVITY));
    }

    // Make the map be redrawn when the window is resized.
    private void initRedrawOnResize() {
        Window w = stage.getScene().getWindow();

        // add ChangeListeners to detect window resize
        w.widthProperty().addListener((observableValue, number, t1) -> needsRefresh = true);
        w.heightProperty().addListener((observableValue, number, t1) -> needsRefresh = true);

        // if window is maximized or unmaximized, the map must be redrawn
        stage.maximizedProperty().addListener((observableValue, aBoolean, t1) -> {
            needsRefresh = true;
        });

        // if needsRefresh was set to true, this will redraw the floor after the layouts are all calculated by JFX
        stage.getScene().addPostLayoutPulseListener(() -> {
            if (needsRefresh) {
                tileEdDraw.drawFloor();
                needsRefresh = false;
            }
        });
    }

    // depending on the presence of a sector, display options to create one or options to modify it
    private void displayTileOptions(boolean sectorExists) {
        if (sectorExists) {
            if (sectorModeButton.isSelected())
            {
                tileScrollPane.setContent(sectorSettings);
                setSectorOptions();
            }
            else
            {
                tileScrollPane.setContent(surfaceModeOptions);
                surfaceModeOptions.setSurfaceModeOptions();
            }
            tileScrollPane.setVvalue(tileScrollPane.getVmin()); // jump to top of scroll pane
        }
        else {
            tileScrollPane.setContent(newSectorMenu);
        }
    }

    // allows the main class to invoke onNewFile
    public void newFile() {
        onNewFile();
    }

    @FXML private void onNewFile() {
        // set name of file to "untitled"
        fileNameText.setText("untitled");

        // display "Modified" at bottom of the screen to tell user changes aren't saved
        setIsModified();

        // create map with one default sector
        currMap = new GameMap();
        tileEdDraw.switchMap(currMap);
        doorOptions.setGameMap(currMap);

        currFloor = 0;
        currSector = null;

        // by default, grid lines are shown
        tileEdDraw.setGridLinesShown(true);
        // set floor to first floor
        tileEdDraw.switchFloor(0);

        tileEdDraw.drawFloor();

        initRedrawOnResize(); // has to be done after reference to stage is initialised
    }

    @FXML private void onOpenFile() {

    }

    @FXML private void onSaveFile() {


        modified = false;
        modifiedText.setText("Changes Saved");
    }

    @FXML private void onSaveAs() {


        modified = false;
        modifiedText.setText("Changes Saved");
    }

    @FXML private void onExit() {
        if (stage != null) {
            stage.close();
        }
        else {
            System.err.println("ERROR: No stage is set");
        }
    }

    // delete currently selected sector
    // clear reference to sector in tileEdDraw
    @FXML private void deleteSector() {
        // currMap.removeSector(tileEdDraw.getCurrSector());
        currMap.removeSector(currSector);
        currSector = null;
        tileEdDraw.clearSelectedSector();
        tileScrollPane.setContent(null);
        tileEdDraw.drawFloor();

        setIsModified();
    }

    @FXML private void changeMode() {
        currSector = null;
        tileEdDraw.clearSelectedSector();
        tileEdDraw.deselectWalls();

        if (sectorModeButton.isSelected()) {
            tileEdDraw.setDrawMode(DrawMode.SECTOR);
            tileScrollPane.setContent(null);
        }
        else if (textureModeButton.isSelected()) {
            tileEdDraw.setDrawMode(DrawMode.TEXTURE);
            tileScrollPane.setContent(textureModeOptions);
            textureModeOptions.setTextureModeOptions();
        }
        else {
            tileScrollPane.setContent(null);
            tileEdDraw.setDrawMode(DrawMode.SURFACE);
        }

        needsRefresh = true;
    }

    @FXML private void toggleWall() {
        setWallOptions();
    }

    @FXML private void toggleAllWalls() {
        // deselect all walls if they're all selected. Otherwise, select all walls
        if (allWallsSelected()) {
            northWallButton.setSelected(false);
            eastWallButton.setSelected(false);
            westWallButton.setSelected(false);
            southWallButton.setSelected(false);
            mainWallOptions.setDisable(true);
        }
        else {
            northWallButton.setSelected(true);
            eastWallButton.setSelected(true);
            westWallButton.setSelected(true);
            southWallButton.setSelected(true);
            mainWallOptions.setDisable(false);
        }

        setWallOptions();
    }

    @FXML private void selectAdjoinedWalls() {
        // select all walls in the current sector which are adjoined
        // Sector curr = tileEdDraw.getCurrSector();

        /*
        northWallButton.setSelected(curr.getWallAdjoin(Direction.NORTH));
        eastWallButton.setSelected(curr.getWallAdjoin(Direction.EAST));
        southWallButton.setSelected(curr.getWallAdjoin(Direction.SOUTH));
        westWallButton.setSelected(curr.getWallAdjoin(Direction.WEST));
         */
        northWallButton.setSelected(currSector.getWallAdjoin(Direction.NORTH));
        eastWallButton.setSelected(currSector.getWallAdjoin(Direction.EAST));
        southWallButton.setSelected(currSector.getWallAdjoin(Direction.SOUTH));
        westWallButton.setSelected(currSector.getWallAdjoin(Direction.WEST));
        setWallOptions();
    }

    @FXML private void toggleWallAdjoin() {
        // attempt to adjoin all selected walls
        boolean setAdjoin = wallAdjoinButton.isIndeterminate() || wallAdjoinButton.isSelected();

        /*
        if (northWallButton.isSelected()) {
            currMap.setWallAdjoin(tileEdDraw.getCurrSector(), Direction.NORTH, setAdjoin);
        }
        if (southWallButton.isSelected()) {
            currMap.setWallAdjoin(tileEdDraw.getCurrSector(), Direction.SOUTH, setAdjoin);
        }
        if (eastWallButton.isSelected()) {
            currMap.setWallAdjoin(tileEdDraw.getCurrSector(), Direction.EAST, setAdjoin);
        }
        if (westWallButton.isSelected()) {
            currMap.setWallAdjoin(tileEdDraw.getCurrSector(), Direction.WEST, setAdjoin);
        }
        */

        if (northWallButton.isSelected()) {
            currMap.setWallAdjoin(currSector, Direction.NORTH, setAdjoin);
        }
        if (southWallButton.isSelected()) {
            currMap.setWallAdjoin(currSector, Direction.SOUTH, setAdjoin);
        }
        if (eastWallButton.isSelected()) {
            currMap.setWallAdjoin(currSector, Direction.EAST, setAdjoin);
        }
        if (westWallButton.isSelected()) {
            currMap.setWallAdjoin(currSector, Direction.WEST, setAdjoin);
        }

        // this will determine if the wallAdjoinButton is selected, unselected, indeterminate
        setWallOptions();

        setIsModified();
    }

    @FXML private void openMainTextureFile() {
        String name = openTextureFile();

        if (name != null) {
            for (Direction d : getSelectedWalls()) {
                // tileEdDraw.getCurrSector().setMainTexture(d, name);
                currSector.setMainTexture(d, name);
            }

            setIsModified();
            setWallOptions();
        }
    }

    @FXML private void openAdjoinTextureFile() {
        String name = openTextureFile();

        if (name != null) {
            for (Direction d : getSelectedWalls()) {
                // tileEdDraw.getCurrSector().setAdjTexture(d, name);
                currSector.setAdjTexture(d, name);
            }

            setIsModified();
            setWallOptions();
        }
    }

    @FXML private void openFloorTextureFile() {
        String name = openTextureFile();

        if (name != null) {
            // tileEdDraw.getCurrSector().setFloorTexture(name);
            currSector.setFloorTexture(name);

            setIsModified();
            setSectorOptions();
        }
    }

    @FXML private void openCeilingTextureFile() {
        String name = openTextureFile();

        if (name != null) {
            // tileEdDraw.getCurrSector().setCeilingTexture(name);
            currSector.setCeilingTexture(name);

            setIsModified();
            setSectorOptions();
        }
    }

    public String openTextureFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Texture File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported File Types", "*.png"));
        chooser.setInitialDirectory(new File("Textures/"));

        File result = chooser.showOpenDialog(stage);
        return result == null ? null : result.getName();
    }

    @FXML private void clearMainTexture() {
        for (Direction d : getSelectedWalls()) {
            // tileEdDraw.getCurrSector().setMainTexture(d, null);
            currSector.setMainTexture(d, null);
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void clearAdjoinTexture() {
        for (Direction d : getSelectedWalls()) {
            // tileEdDraw.getCurrSector().setAdjTexture(d, null);
            currSector.setAdjTexture(d, null);
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void clearFloorTexture() {
        // tileEdDraw.getCurrSector().setFloorTexture(null);
        currSector.setFloorTexture(null);

        setSectorOptions();
        setIsModified();
    }

    @FXML private void clearCeilingTexture() {
        // tileEdDraw.getCurrSector().setCeilingTexture(null);
        currSector.setCeilingTexture(null);

        setSectorOptions();
        setIsModified();
    }

    @FXML private void setMainTextureName() {
        // try to open file to verify it exists
        if (adjustName(mainTextureName.getText()) != null) {
            // if it does, set the texture file
            // Sector curr = tileEdDraw.getCurrSector();
            for (Direction d : getSelectedWalls()) {
                // curr.setMainTexture(d, adjustName(mainTextureName.getText()));
                currSector.setMainTexture(d, adjustName(mainTextureName.getText()));
            }
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setWallOptions();
        setIsModified();
    }

    @FXML private void setAdjTextureName() {
        // try to open file to verify it exists
        if (adjustName(adjoinTextureName.getText()) != null) {
            // if it does, set the texture file
            // Sector curr = tileEdDraw.getCurrSector();
            for (Direction d : getSelectedWalls()) {
                // curr.setAdjTexture(d, adjustName(adjoinTextureName.getText()));
                currSector.setAdjTexture(d, adjustName(adjoinTextureName.getText()));
            }
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setWallOptions();
        setIsModified();
    }

    @FXML private void setFloorTextureName() {
        // try to open file to verify it exists
        if (adjustName(floorTextureName.getText()) != null) {
            // if it does, set the texture file
            // tileEdDraw.getCurrSector().setFloorTexture(adjustName(floorTextureName.getText()));
            currSector.setFloorTexture(adjustName(floorTextureName.getText()));
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setSectorOptions();
        setIsModified();
    }

    @FXML private void setCeilingTextureName() {
        // try to open file to verify it exists
        if (adjustName(ceilingTextureName.getText()) != null) {
            // if it does, set the texture file
            // tileEdDraw.getCurrSector().setCeilingTexture(adjustName(ceilingTextureName.getText()));
            currSector.setCeilingTexture(adjustName(ceilingTextureName.getText()));
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setSectorOptions();
        setIsModified();
    }

    @FXML private void toggleBlockMovement() {
        for (Direction d : getSelectedWalls()) {
            // currMap.setBlocksMovement(tileEdDraw.getCurrSector(), d, movementBlockButton.isSelected());
            currMap.setBlocksMovement(currSector, d, movementBlockButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void toggleBlockProjectiles() {
        for (Direction d : getSelectedWalls()) {
            // currMap.setBlocksProjectiles(tileEdDraw.getCurrSector(), d, projectileBlockButton.isSelected());
            currMap.setBlocksProjectiles(currSector, d, projectileBlockButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void toggleAdjTexAlign() {
        for (Direction d : getSelectedWalls()) {
            // currMap.setAdjAlignToFloor(tileEdDraw.getCurrSector(), d, adjoinTexAlignButton.isSelected());
            currMap.setAdjAlignToFloor(currSector, d, adjoinTexAlignButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void toggleTexAlign() {
        for (Direction d : getSelectedWalls()) {
            // currMap.setAlignToFloor(tileEdDraw.getCurrSector(), d, texAlignButton.isSelected());
            currMap.setAlignToFloor(currSector, d, texAlignButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void flipMainTexture() {
        for (Direction d : getSelectedWalls()) {
            // tileEdDraw.getCurrSector().setMainHFlip(d, mainHFlipButton.isSelected());
            // tileEdDraw.getCurrSector().setMainVFlip(d, mainVFlipButton.isSelected());
            currSector.setMainHFlip(d, mainHFlipButton.isSelected());
            currSector.setMainVFlip(d, mainVFlipButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void flipAdjTexture() {
        for (Direction d : getSelectedWalls()) {
            // tileEdDraw.getCurrSector().setAdjHFlip(d, adjHFlipButton.isSelected());
            // tileEdDraw.getCurrSector().setAdjVFlip(d, adjVFlipButton.isSelected());
            currSector.setAdjHFlip(d, adjHFlipButton.isSelected());
            currSector.setAdjVFlip(d, adjVFlipButton.isSelected());
        }

        setWallOptions();
        setIsModified();
    }

    @FXML private void toggleAdjoinFloor() {
        // currMap.setFloorAdjoin(tileEdDraw.getCurrSector(), adjoinFloorButton.isSelected());
        currMap.setFloorAdjoin(currSector, adjoinFloorButton.isSelected());

        setSectorOptions();
        setIsModified();
    }

    @FXML private void toggleAdjoinCeiling() {
        // currMap.setCeilingAdjoin(tileEdDraw.getCurrSector(), adjoinCeilingButton.isSelected());
        currMap.setCeilingAdjoin(currSector, adjoinCeilingButton.isSelected());

        setSectorOptions();
        setIsModified();
    }

    @FXML private void toggleCeilingSky() {
        // currMap.setCeilingIsSky(tileEdDraw.getCurrSector(), toggleSkyButton.isSelected());
        currMap.setCeilingIsSky(currSector, toggleSkyButton.isSelected());

        setSectorOptions();
        setIsModified();
    }

    @FXML private void toggleGridLines() {
        if (tileEdDraw.getGridLinesShown()) {
            gridLinesButton.setText("Show Grid Lines");
        }
        else {
            gridLinesButton.setText("Hide Grid Lines");
        }
        tileEdDraw.setGridLinesShown(!tileEdDraw.getGridLinesShown());
        tileEdDraw.drawFloor();
    }

    @FXML private void changeFloorUp() {
        ++currFloor;
        changeFloor();
    }

    @FXML private void changeFloorDown() {
        --currFloor;
        changeFloor();
    }

    private void changeFloor() {
        tileEdDraw.switchFloor(currFloor);
        currSector = null;
        currentFloorNumber.setText(((Integer)currFloor).toString());
        tileScrollPane.setContent(null);
        tileEdDraw.drawFloor();
    }

    public void setIsModified() {
        modified = true;
        modifiedText.setText("Modified");
    }

    // select and unselect certain wall options depending on what is true for the current wall
    private void setWallOptions() {
        List<Direction> selectedWalls = getSelectedWalls();
        // Sector currSector = tileEdDraw.getCurrSector();
        Direction firstWall = selectedWalls.isEmpty() ? null : selectedWalls.get(0);

        mainWallOptions.setDisable(noWallsSelected());

        // first, set whether each CheckBox is indeterminate
        setIndeterminateButtons();

        // wall adjoin button:
        // disable if not all selected walls can be adjoined
        wallAdjoinButton.setDisable(!adjoinsPossible(selectedWalls));

        // use first selected wall (if any are selected) to set adjoin
        // button being selected (only shows up if not indeterminate)
        wallAdjoinButton.setSelected(
                !wallAdjoinButton.isIndeterminate()
                && selectedWalls.size() > 0
                && currSector.getWallAdjoin(firstWall));

        // if all selected walls are adjoined, enable the wall adjoin options
        adjoinedWallOptions.setDisable(!wallAdjoinButton.isSelected() || wallAdjoinButton.isIndeterminate());

        // wall adjoin-specific options:

        // movement is blocked b/w sectors
        movementBlockButton.setSelected(selectedWalls.size() > 0
                && currSector.getWallBlocksMovement(firstWall));

        // projectiles are blocked b/w sectors
        projectileBlockButton.setSelected(selectedWalls.size() > 0
                && currSector.getWallBlocksProjectiles(firstWall));

        // main texture is drawn up from floor (doesn't make any difference if not adjoined)
        texAlignButton.setSelected(selectedWalls.size() > 0
                && currSector.getAlignToFloor(firstWall));

        // adjoin texture is drawn up from floor
        adjoinTexAlignButton.setSelected(selectedWalls.size() > 0
                && currSector.getAdjAlignToFloor(firstWall));

        // main texture flipping
        mainHFlipButton.setSelected(selectedWalls.size() > 0
                && currSector.getMainHFlip(firstWall));
        mainVFlipButton.setSelected(selectedWalls.size() > 0
                && currSector.getMainVFlip(firstWall));

        // adjoin texture flipping
        adjHFlipButton.setSelected(selectedWalls.size() > 0
                && currSector.getAdjHFlip(firstWall));

        // texture selection WAS disabled if more than one wall selected
        /*
        mainTextureFileButton.setDisable(selectedWalls.size() != 1);
        adjoinTextureFileButton.setDisable(selectedWalls.size() != 1);
        clearMainTextureButton.setDisable(selectedWalls.size() != 1);
        clearAdjTextureButton.setDisable(selectedWalls.size() != 1);
        */

        // fill main wall texture TextField
        if (selectedWalls.size() == 0 || !mainTexturesMatch(selectedWalls)) {
            mainTextureName.setText(TEXTURE_NA);
        }
        else if (currSector.getMainTexture(selectedWalls.get(0)) == null) {
            mainTextureName.setText(TEXTURE_NULL);
        }
        else {
            mainTextureName.setText(currSector.getMainTexture(selectedWalls.get(0)));
        }

        // fill adjoin wall texture TextField
        if (selectedWalls.size() == 0 || !adjTexturesMatch(selectedWalls)) {
            adjoinTextureName.setText(TEXTURE_NA);
        }
        else if (currSector.getAdjTexture(selectedWalls.get(0)) == null) {
            adjoinTextureName.setText(TEXTURE_NULL);
        }
        else {
            adjoinTextureName.setText(currSector.getAdjTexture(selectedWalls.get(0)));
        }
    }

    private void setIndeterminateButtons() {
        List<Direction> selectedWalls = getSelectedWalls();

        boolean
                firstAdjoin,
                firstMoveBlock,
                firstProjectileBlock,
                firstMainTexAlign,
                firstAdjTexAlign,
                firstMainHFlip,
                firstMainVFlip,
                firstAdjHFlip,
                firstAdjVFlip,

                adjoinMatch = true,
                moveBlockMatch = true,
                projectileBlockMatch = true,
                mainTexAlignMatch = true,
                adjTexAlignMatch = true,
                mainHMatch = true,
                mainVMatch = true,
                adjHMatch = true,
                adjVMatch = true
        ;

        if (!selectedWalls.isEmpty()) {
            // Sector currSector = tileEdDraw.getCurrSector();
            Direction firstDir = selectedWalls.get(0);

            firstAdjoin = currSector.getWallAdjoin(firstDir);
            firstMoveBlock = currSector.getWallBlocksMovement(firstDir);
            firstProjectileBlock = currSector.getWallBlocksProjectiles(firstDir);
            firstMainTexAlign = currSector.getAlignToFloor(firstDir);
            firstAdjTexAlign = currSector.getAdjAlignToFloor(firstDir);
            firstMainHFlip = currSector.getMainHFlip(firstDir);
            firstMainVFlip = currSector.getMainVFlip(firstDir);
            firstAdjHFlip = currSector.getAdjHFlip(firstDir);
            firstAdjVFlip = currSector.getAdjVFlip(firstDir);

            for (Direction d : selectedWalls) {
                if (currSector.getWallAdjoin(d) != firstAdjoin)
                    adjoinMatch = false;
                if (currSector.getWallBlocksMovement(d) != firstMoveBlock)
                    moveBlockMatch = false;
                if (currSector.getWallBlocksProjectiles(d) != firstProjectileBlock)
                    projectileBlockMatch = false;
                if (currSector.getAlignToFloor(d) != firstMainTexAlign)
                    mainTexAlignMatch = false;
                if (currSector.getAdjAlignToFloor(d) != firstAdjTexAlign)
                    adjTexAlignMatch = false;
                if (currSector.getMainHFlip(d) != firstMainHFlip)
                    mainHMatch = false;
                if (currSector.getMainVFlip(d) != firstMainVFlip)
                    mainVMatch = false;
                if (currSector.getAdjHFlip(d) != firstAdjHFlip)
                    adjHMatch = false;
                if (currSector.getAdjVFlip(d) != firstAdjVFlip)
                    adjVMatch = false;
            }
        }

        wallAdjoinButton.setIndeterminate(!adjoinMatch);
        movementBlockButton.setIndeterminate(!moveBlockMatch);
        projectileBlockButton.setIndeterminate(!projectileBlockMatch);
        texAlignButton.setIndeterminate(!mainTexAlignMatch);
        adjoinTexAlignButton.setIndeterminate(!adjTexAlignMatch);
        mainHFlipButton.setIndeterminate(!mainHMatch);
        mainVFlipButton.setIndeterminate(!mainVMatch);
        adjHFlipButton.setIndeterminate(!adjHMatch);
        adjVFlipButton.setIndeterminate(!adjVMatch);
    }

    private boolean adjoinsPossible(List<Direction> selectedWalls) {
        if (selectedWalls.size() == 0)
            return false;

        boolean possible = false;

        for (Direction d : selectedWalls) {
            // if (currMap.canAdjoinWall(tileEdDraw.getCurrSector(), d))
            if (currMap.canAdjoinWall(currSector, d))
                possible = true;
        }

        return possible;
    }

    private boolean mainTexturesMatch(List<Direction> walls) {
        if (walls.size() == 0)
            return true;

        // Sector currSector = tileEdDraw.getCurrSector();

        boolean match = true;
        String firstTex = currSector.getMainTexture(walls.get(0));

        for (Direction d : walls) {
            if (!Objects.equals(firstTex, currSector.getMainTexture(d)))
                match = false;
        }

        return match;
    }

    private boolean adjTexturesMatch(List<Direction> walls) {
        if (walls.size() == 0)
            return true;

        // Sector currSector = tileEdDraw.getCurrSector();

        boolean match = true;
        String firstTex = currSector.getAdjTexture(walls.get(0));

        for (Direction d : walls) {
            if (!Objects.equals(firstTex, currSector.getAdjTexture(d)))
                match = false;
        }

        return match;
    }

    public String adjustName(String name) {
        if (new File(TEXTURE_DIR + name).exists())
            return name;
        else if (new File(TEXTURE_DIR + name + TEXTURE_EXT).exists())
            return name + TEXTURE_EXT;
        else
            return null;
    }

    private List<Direction> getSelectedWalls() {
        ArrayList<Direction> list = new ArrayList<>();

        if (northWallButton.isSelected())
            list.add(Direction.NORTH);
        if (eastWallButton.isSelected())
            list.add(Direction.EAST);
        if (southWallButton.isSelected())
            list.add(Direction.SOUTH);
        if (westWallButton.isSelected())
            list.add(Direction.WEST);

        return list;
    }

    private void setSectorOptions() {
        // clear wall selection
        northWallButton.setSelected(false);
        southWallButton.setSelected(false);
        eastWallButton.setSelected(false);
        westWallButton.setSelected(false);

        // Sector currSector = tileEdDraw.getCurrSector();

        // set adjoin options
        adjoinCeilingButton.setSelected(currSector.getCeilingAdjoin());
        adjoinFloorButton.setSelected(currSector.getFloorAdjoin());

        // set sky options
        toggleSkyButton.setSelected(currSector.getSky());

        // cannot have the sky for a ceiling if adjoined
        toggleSkyButton.setDisable(adjoinCeilingButton.isSelected());

        // fill floor texture field
        floorTextureName.setText( currSector.getFloorTexture() == null ? TEXTURE_NULL : currSector.getFloorTexture() );

        // ceiling texture field
        ceilingTextureName.setText( currSector.getCeilingTexture() == null ? TEXTURE_NULL : currSector.getCeilingTexture() );

        // update wall options now that all are unselected
        setWallOptions();

        // set options for door in sector
        doorOptions.setDoorOptions();
    }

    private boolean allWallsSelected() {
        // return northSelected && southSelected && eastSelected && westSelected;

        return northWallButton.isSelected()
                && southWallButton.isSelected()
                && eastWallButton.isSelected()
                && westWallButton.isSelected()
        ;
    }

    private boolean noWallsSelected() {
        // return !northSelected && !southSelected && !eastSelected && !westSelected;

        return !northWallButton.isSelected()
                && !southWallButton.isSelected()
                && !eastWallButton.isSelected()
                && !westWallButton.isSelected()
        ;
    }

    private void unselectAllWalls() {
        northWallButton.setSelected(false);
        eastWallButton.setSelected(false);
        southWallButton.setSelected(false);
        westWallButton.setSelected(false);
    }
}