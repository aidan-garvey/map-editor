package aidan_garvey.mapeditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TextureModeOptions extends VBox {

    @FXML private Button
            mainOpenFileButton,
            adjOpenFileButton,
            mainClearFileButton,
            adjClearFileButton
    ;

    @FXML private TextField
            mainFileName,
            adjFileName
    ;

    @FXML private Text wallsSelectedCount;

    private final Image
            FILE_ICON = new Image(new FileInputStream(MapEdController.FOLDER_ICON_PATH)),
            CANCEL_ICON = new Image(new FileInputStream(MapEdController.CANCEL_ICON_PATH))
    ;

    private final MapEdController myController;

    private TileEdDraw tileEdDraw;

    public TextureModeOptions(MapEdController parent) throws FileNotFoundException {
        myController = parent;
        tileEdDraw = null;

        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("texture-mode.fxml"));
        myLoader.setRoot(this);
        myLoader.setController(this);

        try {
            myLoader.load();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void setTileEdDraw(TileEdDraw ted) {
        tileEdDraw = ted;
    }

    @FXML public void initialize() {
        // set graphics for buttons
        mainOpenFileButton.setGraphic(new ImageView(FILE_ICON));
        adjOpenFileButton.setGraphic(new ImageView(FILE_ICON));
        mainClearFileButton.setGraphic(new ImageView(CANCEL_ICON));
        adjClearFileButton.setGraphic(new ImageView(CANCEL_ICON));
    }

    @FXML private void openMainTexture() {
        String name = myController.openTextureFile();

        if (name != null) {
            tileEdDraw.applyMainTexture(name);

            myController.setIsModified();
            setTextureModeOptions();
        }
    }

    @FXML private void openAdjTexture() {
        String name = myController.openTextureFile();

        if (name != null) {
            tileEdDraw.applyAdjTexture(name);

            myController.setIsModified();
            setTextureModeOptions();
        }
    }

    @FXML private void clearMainTexture() {
        tileEdDraw.applyMainTexture(null);

        setTextureModeOptions();
        myController.setIsModified();
    }

    @FXML private void clearAdjTexture() {
        tileEdDraw.applyAdjTexture(null);

        setTextureModeOptions();
        myController.setIsModified();
    }

    // when user enters a file name in the text field
    @FXML private void setMainTextureName() {
        // try to open file to verify it exists
        if (myController.adjustName(mainFileName.getText()) != null) {
            // if it does, set the texture file
            tileEdDraw.applyMainTexture(myController.adjustName(mainFileName.getText()));
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setTextureModeOptions();
        myController.setIsModified();
    }

    // when user enters a file name in the text field
    @FXML private void setAdjTextureName() {
        // try to open file to verify it exists
        if (myController.adjustName(adjFileName.getText()) != null) {
            // if it does, set the texture file
            tileEdDraw.applyAdjTexture(myController.adjustName(adjFileName.getText()));
        }
        // else {
            // todo: have pop-up to inform user of invalid name
        // }

        // update the options, resets name if an invalid one was entered
        setTextureModeOptions();
        myController.setIsModified();
    }

    public void setTextureModeOptions() {
        // get all the currently selected walls
        HashMap<Sector, List<Direction>> currWalls = tileEdDraw.getCurrWalls();
        int numWalls = setTextureNames(currWalls);

        wallsSelectedCount.setText(String.format("Walls Selected: %d", numWalls));
    }

    private int setTextureNames(HashMap<Sector, List<Direction>> walls) {
        Boolean mainMatch = null, adjMatch = null;
        String firstMain = null, firstAdj = null;

        int count = 0;

        for (Sector s : walls.keySet()) {
            for (Direction d : walls.get(s)) {
                ++count;

                if (mainMatch == null) {
                    mainMatch = true;
                    firstMain = s.getMainTexture(d);
                }
                else if (mainMatch && !Objects.equals(firstMain, s.getMainTexture(d))) {
                    mainMatch = false;
                }

                if (adjMatch == null) {
                    adjMatch = true;
                    firstAdj = s.getAdjTexture(d);
                }
                else if (adjMatch && !Objects.equals(firstAdj, s.getAdjTexture(d))) {
                    adjMatch = false;
                }
            }
        }

        if (mainMatch == null || !mainMatch) {
            mainFileName.setText(MapEdController.TEXTURE_NA);
        }
        else {
            mainFileName.setText(firstMain == null ? MapEdController.TEXTURE_NULL : firstMain);
        }

        if (adjMatch == null || !adjMatch) {
            adjFileName.setText(MapEdController.TEXTURE_NA);
        }
        else {
            adjFileName.setText(firstAdj == null ? MapEdController.TEXTURE_NULL : firstAdj);
        }

        return count;
    }
}
