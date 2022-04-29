package aidan_garvey.mapeditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static aidan_garvey.mapeditor.Corner.*;

public class SurfaceModeOptions extends VBox {
    private static final int
            NUM_CORNERS = 4,
            FLOOR_H_BASE = 0,
            CEIL_H_BASE = 2,
            I_X = 0,
            I_Y = 1,
            I_Z = 2,
            POINT3D_LEN = 3,
            SUBSCENE_SIZE = 150;

    private static final double
            CAMERA_DIST = 10.0,
            DEFAULT_X_ANGLE = 25.0,
            DEFAULT_Y_ANGLE = 135.0;

    private static final String
            CEILING_TEXT = "Ceiling",
            FLOOR_TEXT = "Floor",
            ADJOINED_TEXT = " [ADJOINED]";

    @FXML
    @SuppressWarnings("unused")
    private GridPane
            ceilingSpinnerPane,
            floorSpinnerPane;

    @FXML
    @SuppressWarnings("unused")
    private ImageView
            ceilingEastImageView,
            ceilingNorthImageView,
            ceilingSouthImageView,
            ceilingWestImageView,
            floorEastImageView,
            floorNorthImageView,
            floorSouthImageView,
            floorWestImageView;
            // ceilingGuideImageView,
            // floorGuideImageView;

    @FXML
    @SuppressWarnings("unused")
    private Spinner<Integer>
            ceilingNESpinner,
            ceilingSESpinner,
            ceilingNWSpinner,
            ceilingSWSpinner,
            ceilingCenterSpinner,
            floorNESpinner,
            floorSESpinner,
            floorNWSpinner,
            floorSWSpinner,
            floorCenterSpinner;

    @FXML
    @SuppressWarnings("unused")
    private Text
            ceilingText,
            floorText;

    @FXML
    @SuppressWarnings("unused")
    private StackPane subSceneContainer;

    private final SubScene subScene;
    private final Group
            subSceneRoot,
            sectorGroup;
    private final Rotate
            sectorRotX,
            sectorRotY;
    private final MeshView ceilingMeshView, floorMeshView;
    private final Alert helpDialog;

    private final Image
            SPINNERS_HELP = new Image(new FileInputStream(MapEdController.SPINNERS_HELP_PATH)),
            TILE_GUIDE = new Image(new FileInputStream(MapEdController.TILE_GUIDE_PATH)),
            EAST_GUIDE = new Image(new FileInputStream(MapEdController.EAST_GUIDE_PATH)),
            NORTH_GUIDE = new Image(new FileInputStream(MapEdController.NORTH_GUIDE_PATH)),
            SOUTH_GUIDE = new Image(new FileInputStream(MapEdController.SOUTH_GUIDE_PATH)),
            WEST_GUIDE = new Image(new FileInputStream(MapEdController.WEST_GUIDE_PATH));

    private final MapEdController myController;

    private double mouseXOld = 0.d, mouseYOld = 0.d;
    private boolean isDragging = false;

    public SurfaceModeOptions(MapEdController parent) throws FileNotFoundException {
        myController = parent;

        helpDialog = new Alert(Alert.AlertType.INFORMATION, """
                The five inputs (referred to as "spinners") correspond to the highlighted points on a floor or ceiling tile shown in the image above.
                The spinners control the height of each point. The height must be between 0 (ground level) and 2 (ceiling level, which is also ground level for the floor above).
                To have higher ceilings or lower floors, create a sector above or below this one and adjoin (connect) it to this one with Sector Mode."""
        );
        helpDialog.setGraphic(new ImageView(SPINNERS_HELP));
        helpDialog.setHeaderText("Sector Mode Help");
        helpDialog.setTitle("Sector Mode Help");

        subSceneRoot = new Group();
        sectorGroup = new Group();
        sectorRotX = new Rotate();
        sectorRotY = new Rotate();
        ceilingMeshView = new MeshView();
        floorMeshView = new MeshView();
        subScene = new SubScene(subSceneRoot, SUBSCENE_SIZE, SUBSCENE_SIZE);
        initSubScene();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("surface-mode.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void initSubScene()
    {
        subScene.setFill(Color.BLACK);

        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setTranslateZ(-CAMERA_DIST);
        subScene.setCamera(cam);

        // configure rotation transforms for sectorGroup
        sectorRotX.setAxis(Rotate.X_AXIS);
        sectorRotY.setAxis(Rotate.Y_AXIS);
        sectorGroup.getTransforms().addAll(sectorRotX, sectorRotY);

        // configure MeshViews
        PhongMaterial tileMat = new PhongMaterial(Color.RED);

        ceilingMeshView.setDrawMode(DrawMode.LINE);
        ceilingMeshView.setMaterial(tileMat);
        ceilingMeshView.setTranslateY(-1.f);
        ceilingMeshView.setCullFace(CullFace.NONE);

        floorMeshView.setDrawMode(DrawMode.LINE);
        floorMeshView.setMaterial(tileMat);
        floorMeshView.setTranslateY(1.f);
        floorMeshView.setCullFace(CullFace.NONE);

        // add to sectorGroup, add that to root
        sectorGroup.getChildren().addAll(ceilingMeshView, floorMeshView);
        subSceneRoot.getChildren().add(sectorGroup);

        // illuminate the subscene
        subSceneRoot.getChildren().add(new AmbientLight());
    }

    @SuppressWarnings("unused")
    @FXML public void initialize()
    {
        // set graphics for ceiling and floor guides
        // ceilingGuideImageView.setImage(TILE_GUIDE);
        // floorGuideImageView.setImage(TILE_GUIDE);
        ceilingEastImageView.setImage(EAST_GUIDE);
        ceilingNorthImageView.setImage(NORTH_GUIDE);
        ceilingSouthImageView.setImage(SOUTH_GUIDE);
        ceilingWestImageView.setImage(WEST_GUIDE);

        floorEastImageView.setImage(EAST_GUIDE);
        floorNorthImageView.setImage(NORTH_GUIDE);
        floorSouthImageView.setImage(SOUTH_GUIDE);
        floorWestImageView.setImage(WEST_GUIDE);

        subSceneContainer.getChildren().add(subScene);

        // add listeners for spinners
        ceilingNESpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getCeilingOffsets();
            offsets[NORTH_EAST.index] = newVal - CEIL_H_BASE;
            correctOffsets(offsets, NORTH_EAST);
            refreshCeilingMesh(offsets);
            setCeilingSpinners(offsets);
        });
        ceilingNWSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getCeilingOffsets();
            offsets[NORTH_WEST.index] = newVal - CEIL_H_BASE;
            correctOffsets(offsets, NORTH_WEST);
            refreshCeilingMesh(offsets);
            setCeilingSpinners(offsets);
        });
        ceilingSESpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getCeilingOffsets();
            offsets[SOUTH_EAST.index] = newVal - CEIL_H_BASE;
            correctOffsets(offsets, SOUTH_EAST);
            refreshCeilingMesh(offsets);
            setCeilingSpinners(offsets);
        });
        ceilingSWSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getCeilingOffsets();
            offsets[SOUTH_WEST.index] = newVal - CEIL_H_BASE;
            correctOffsets(offsets, SOUTH_WEST);
            refreshCeilingMesh(offsets);
            setCeilingSpinners(offsets);
        });
        ceilingCenterSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getCeilingOffsets();
            offsets[CENTER.index] = newVal - CEIL_H_BASE;
            correctOffsets(offsets, CENTER);
            refreshCeilingMesh(offsets);
            setCeilingSpinners(offsets);
        });

        floorNESpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getFloorOffsets();
            offsets[NORTH_EAST.index] = newVal - FLOOR_H_BASE;
            correctOffsets(offsets, NORTH_EAST);
            refreshFloorMesh(offsets);
            setFloorSpinners(offsets);
        });
        floorNWSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getFloorOffsets();
            offsets[NORTH_WEST.index] = newVal - FLOOR_H_BASE;
            correctOffsets(offsets, NORTH_WEST);
            refreshFloorMesh(offsets);
            setFloorSpinners(offsets);
        });
        floorSESpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getFloorOffsets();
            offsets[SOUTH_EAST.index] = newVal - FLOOR_H_BASE;
            correctOffsets(offsets, SOUTH_EAST);
            refreshFloorMesh(offsets);
            setFloorSpinners(offsets);
        });
        floorSWSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getFloorOffsets();
            offsets[SOUTH_WEST.index] = newVal - FLOOR_H_BASE;
            correctOffsets(offsets, SOUTH_WEST);
            refreshFloorMesh(offsets);
            setFloorSpinners(offsets);
        });
        floorCenterSpinner.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            int[] offsets = myController.getCurrSector().getFloorOffsets();
            offsets[CENTER.index] = newVal - FLOOR_H_BASE;
            correctOffsets(offsets, CENTER);
            refreshFloorMesh(offsets);
            setFloorSpinners(offsets);
        });

        initSubSceneControls();
    }

    private void initSubSceneControls() {
        subSceneContainer.setOnDragDetected(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY) {
                isDragging = true;
                mouseXOld = dragEvent.getX();
                mouseYOld = dragEvent.getY();
            }
        });

        subSceneContainer.setOnMouseDragged(dragEvent -> {
            if (isDragging) {
                double deltaX = mouseXOld - dragEvent.getX();
                double deltaY = mouseYOld - dragEvent.getY();
                mouseXOld = dragEvent.getX();
                mouseYOld = dragEvent.getY();
                // rotate about the X axis with mouse Y, about Y axis w/ mouse X
                sectorRotX.setAngle(sectorRotX.getAngle() - deltaY);
                sectorRotY.setAngle(sectorRotY.getAngle() + deltaX);
            }
        });

        subSceneContainer.setOnMouseReleased(mouseEvent -> {
            // release mouse drag
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                isDragging = false;
            }
            else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                setSurfaceModeOptions();
            }
        });
    }

    public void setSurfaceModeOptions()
    {
        int[] ceilingOffsets = myController.getCurrSector().getCeilingOffsets();
        int[] floorOffsets = myController.getCurrSector().getFloorOffsets();

        setCeilingSpinners(ceilingOffsets);
        setFloorSpinners(floorOffsets);

        refreshCeilingMesh(ceilingOffsets);
        refreshFloorMesh(floorOffsets);

        sectorRotX.setAngle(DEFAULT_X_ANGLE);
        sectorRotY.setAngle(DEFAULT_Y_ANGLE);

        if (myController.getCurrSector().getCeilingAdjoin())
            ceilingText.setText(CEILING_TEXT + ADJOINED_TEXT);
        else
            ceilingText.setText(CEILING_TEXT);

        if (myController.getCurrSector().getFloorAdjoin())
            floorText.setText(FLOOR_TEXT + ADJOINED_TEXT);
        else
            floorText.setText(FLOOR_TEXT);
    }

    private void setCeilingSpinners(int[] ceilingOffsets)
    {
        ceilingNESpinner.getValueFactory().setValue(CEIL_H_BASE + ceilingOffsets[NORTH_EAST.index]);
        ceilingNWSpinner.getValueFactory().setValue(CEIL_H_BASE + ceilingOffsets[NORTH_WEST.index]);
        ceilingSESpinner.getValueFactory().setValue(CEIL_H_BASE + ceilingOffsets[SOUTH_EAST.index]);
        ceilingSWSpinner.getValueFactory().setValue(CEIL_H_BASE + ceilingOffsets[SOUTH_WEST.index]);
        ceilingCenterSpinner.getValueFactory().setValue(CEIL_H_BASE + ceilingOffsets[CENTER.index]);

        ceilingSpinnerPane.setDisable(myController.getCurrSector().getCeilingAdjoin());
    }

    private void setFloorSpinners(int[] floorOffsets)
    {
        floorNESpinner.getValueFactory().setValue(FLOOR_H_BASE + floorOffsets[NORTH_EAST.index]);
        floorNWSpinner.getValueFactory().setValue(FLOOR_H_BASE + floorOffsets[NORTH_WEST.index]);
        floorSESpinner.getValueFactory().setValue(FLOOR_H_BASE + floorOffsets[SOUTH_EAST.index]);
        floorSWSpinner.getValueFactory().setValue(FLOOR_H_BASE + floorOffsets[SOUTH_WEST.index]);
        floorCenterSpinner.getValueFactory().setValue(FLOOR_H_BASE + floorOffsets[CENTER.index]);

        floorSpinnerPane.setDisable(myController.getCurrSector().getFloorAdjoin());
    }

    // make sure the height offsets for each corner are valid, do not affect the one that was just changed
    private void correctOffsets(int[] offsets, Corner recent)
    {
        if (recent == CENTER) {
            for (int i = 0; i < CENTER.index; i++)
            {
                if (offsets[i] - offsets[recent.index] > 1)
                    offsets[i]--;
                else if (offsets[i] - offsets[recent.index] < -1)
                    offsets[i]++;
            }
        }
        else {
            // corners adjacent to recent may be too far away
            int prevIndex = (recent.index + 3) % 4, // same as subtracting one and keeping in range [0..3]
                    nextIndex = (recent.index + 1) % 4;

            if (offsets[prevIndex] - offsets[recent.index] > 1)
                offsets[prevIndex]--;
            else if (offsets[prevIndex] - offsets[recent.index] < -1)
                offsets[prevIndex]++;

            if (offsets[nextIndex] - offsets[recent.index] > 1)
                offsets[nextIndex]--;
            else if (offsets[nextIndex] - offsets[recent.index] < -1)
                offsets[nextIndex]++;

            if (offsets[CENTER.index] - offsets[recent.index] > 1)
                offsets[CENTER.index]--;
            else if (offsets[CENTER.index] - offsets[recent.index] < -1)
                offsets[CENTER.index]++;
        }
    }

    private void refreshCeilingMesh(int[] offsets)
    {
        if (myController.getCurrSector().getCeilingAdjoin())
            ceilingMeshView.setMesh(null);
        else
            ceilingMeshView.setMesh(buildMesh(offsets));
    }

    private void refreshFloorMesh(int[] offsets)
    {
        if (myController.getCurrSector().getFloorAdjoin())
            floorMeshView.setMesh(null);
        else
            floorMeshView.setMesh(buildMesh(offsets));
    }

    private final float[] meshPoints = {
            -1.f, 0.f, 1.f, // north-east
            1.f, 0.f, 1.f, // south-east
            1.f, 0.f, -1.f, // south-west
            -1.f, 0.f, -1.f, // north-west
            0.f, 0.f, 0.f // centre
    };
    private TriangleMesh buildMesh(int[] yOffsets)
    {
        // write corner y offsets to meshPoints, sum them to get average for centre
        for (Corner c : Corner.values())
        {
            meshPoints[c.index * POINT3D_LEN + I_Y] = yOffsets[c.index] * -1.f;
        }

        // make TriangleMesh using meshPoints
        TriangleMesh result = new TriangleMesh(VertexFormat.POINT_TEXCOORD);
        result.getPoints().addAll(meshPoints, 0, meshPoints.length);
        result.getTexCoords().addAll(0.f, 0.f); // no texture used

        for (int i = 0; i < 4; i++)
            result.getFaces().addAll(4, 0, i, 0, (i + 1) % 4, 0);

        return result;
    }

    @SuppressWarnings("unused")
    @FXML private void displayHelp()
    {
        helpDialog.show();
    }
}
