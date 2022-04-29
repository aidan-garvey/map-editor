package aidan_garvey.mapeditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.*;
import java.util.Optional;

public class DoorOptions extends VBox {
    @FXML
    private GridPane doorOptionsPane;

    @FXML
    private CheckBox
            hasDoorButton,
            shootToOpenButton
    ;

    @FXML
    private Button
            orientButton,
            texButton1,
            texButton2,
            keySelect
    ;

    @FXML
    private RadioButton
            doorPos1,
            doorPos2,
            doorPos3,
            vOpenButton,
            hOpenButton,
            doorOpen1,
            doorOpen2,
            doorOpen3
    ;

    @FXML
    private TextField
            speedTextField,
            fileText1,
            fileText2
    ;

    @FXML
    private Slider speedSlider;

    @FXML
    private Text texLabel1, texLabel2;

    // for selecting a pre-existing key for a door
    private final ChoiceDialog<String> keyChooser;
    // for creating a new key for a door
    private final TextInputDialog keyMaker;

    private static final String
            // directions
            ORIENT_NS = "North-South",
            ORIENT_EW = "East-West",
            POS_N = "North",
            POS_E = "East",
            POS_S = "South",
            POS_W = "West",
            // reserved *key* words
            NO_KEY = "NONE",
            NEW_KEY = "Create Key..."
    ;

    private final Image FILE_ICON = new Image(new FileInputStream(MapEdController.FOLDER_ICON_PATH));
    private final Image KEY_ICON = new Image(new FileInputStream(MapEdController.KEY_ICON_PATH));

    private final MapEdController myController;
    private GameMap gameMap;

    public DoorOptions(MapEdController parent) throws FileNotFoundException {
        myController = parent;

        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("door-options.fxml"));
        myLoader.setRoot(this);
        myLoader.setController(this);

        try {
            myLoader.load();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // create dialog for making a new key
        keyMaker = new TextInputDialog();
        HBox makerHeader = new HBox(new ImageView(KEY_ICON), new Label("Enter New Key Name"));
        makerHeader.setAlignment(Pos.CENTER_LEFT);
        makerHeader.setPadding(new Insets(10));
        makerHeader.setSpacing(10);
        keyMaker.getDialogPane().setHeader(makerHeader);

        // create dialog for selecting a key
        keyChooser = new ChoiceDialog<>(NO_KEY, NO_KEY);
        HBox chooserHeader = new HBox(new ImageView(KEY_ICON), new Label("Select Key"));
        chooserHeader.setAlignment(Pos.CENTER_LEFT);
        chooserHeader.setPadding(new Insets(10));
        chooserHeader.setSpacing(10);
        keyChooser.getDialogPane().setHeader(chooserHeader);

        // add button to keyChooser that returns a reserved value to add a new key
        keyChooser.getDialogPane().getButtonTypes().add(new ButtonType(NEW_KEY, ButtonBar.ButtonData.OTHER));
        keyChooser.setResultConverter (
            buttonType -> switch(buttonType.getButtonData()) {
                case OK_DONE -> keyChooser.getSelectedItem();
                case OTHER -> NEW_KEY;
                default -> null;
            }
        );
    }

    @FXML
    public void initialize() {
        // when user stops moving the slider, update the value internally
        speedSlider.valueChangingProperty().addListener((observableValue, oldIsChanging, isChanging) -> {
            if (!isChanging && myController.getCurrSector() != null) {
                myController.getCurrSector().setDoorSpeed(speedSlider.getValue());
                setDoorOptions();
                myController.setIsModified();
            }
        });
        // as the user moves the slider, update the value in the text box only
        speedSlider.valueProperty().addListener(
                (observableValue, oldVal, newVal) -> speedTextField.setText(String.format("%f", (Double)newVal))
        );

        texButton1.setGraphic(new ImageView(FILE_ICON));
        texButton2.setGraphic(new ImageView(FILE_ICON));
    }

    public void setGameMap(GameMap m) {
        gameMap = m;
    }

    public void setDoorOptions() {
        Sector currSector = myController.getCurrSector();
        boolean northSouth = gameMap.doorIsNorthSouth(currSector);

        // check if sector has a door
        hasDoorButton.setSelected(gameMap.hasDoor(currSector));

        // if there's no door, disable all door-related options
        doorOptionsPane.setDisable(!hasDoorButton.isSelected());
        doorOptionsPane.setVisible(hasDoorButton.isSelected());

        // set text in door orientation button to current orientation
        orientButton.setText(northSouth ? ORIENT_NS : ORIENT_EW);

        // set text for door position options depending on orientation
        if (northSouth) {
            doorPos1.setText(POS_E);
            doorPos3.setText(POS_W);
        }
        else {
            doorPos1.setText(POS_N);
            doorPos3.setText(POS_S);
        }
        // set which button should be selected
        switch (gameMap.getDoorPos(currSector)) {
            case 0 -> doorPos1.setSelected(true);
            case 2 -> doorPos3.setSelected(true);
            default -> doorPos2.setSelected(true);
        }

        // determine if door opens vertically or horizontally
        // set text for where door opens (top/left, middle, or bottom/right)
        if (gameMap.doorIsVertical(currSector)) {
            vOpenButton.setSelected(true);
            doorOpen1.setText("Bottom");
            doorOpen3.setText("Top");
        }
        else {
            hOpenButton.setSelected(true);

            if (northSouth) {
                doorOpen1.setText(POS_N);
                doorOpen3.setText(POS_S);
            }
            else {
                doorOpen1.setText(POS_E);
                doorOpen3.setText(POS_W);
            }
        }

        // set door open type (top/left, middle, or bottom/right)
        switch(gameMap.getDoorType(currSector)) {
            case 1 -> doorOpen2.setSelected(true);
            case 2 -> doorOpen3.setSelected(true);
            default -> doorOpen1.setSelected(true);
        }

        // set door open time
        speedSlider.setValue(gameMap.getDoorSpeed(currSector)); // this should automatically stay in bounds
        speedTextField.setText(String.format("%f", gameMap.getDoorSpeed(currSector)));

        // set text for door texture labels
        if (gameMap.doorIsNorthSouth(currSector)) {
            texLabel1.setText("East Texture");
            texLabel2.setText("West Texture");
        }
        else {
            texLabel1.setText("North Texture");
            texLabel2.setText("South Texture");
        }

        // fill TextFields with current texture names
        String tex = gameMap.getDoorTex1(currSector);
        fileText1.setText(tex == null ? MapEdController.TEXTURE_NA : tex);
        tex = gameMap.getDoorTex2(currSector);
        fileText2.setText(tex == null ? MapEdController.TEXTURE_NA : tex);

        // set text in key Button to current key
        keySelect.setText(gameMap.getDoorKey(currSector) == null ? NO_KEY : gameMap.getDoorKey(currSector));

        shootToOpenButton.setSelected(currSector != null && currSector.getDoorShootToOpen());

        if (currSector != null) {
            myController.setNeedsRefresh();
        }
    }

    // attempt to add or remove a door in the current sector
    @FXML private void toggleDoor() {
        if (hasDoorButton.isSelected())
            gameMap.addDoor(myController.getCurrSector());
        else
            gameMap.removeDoor(myController.getCurrSector());

        setDoorOptions();
        myController.setIsModified();
    }

    // attempt to switch door orientation for current door
    @FXML private void orientDoor() {
        gameMap.toggleDoorOrientation(myController.getCurrSector());

        setDoorOptions();
        myController.setIsModified();
    }

    // change position of door in current sector
    @FXML private void changeDoorPos() {
        int pos;

        if (doorPos1.isSelected())
            pos = 0;
        else if (doorPos2.isSelected())
            pos = 1;
        else
            pos = 2;

        gameMap.setDoorPos(myController.getCurrSector(), pos);

        setDoorOptions();
        myController.setIsModified();
    }

    // change opening direction (h/v) of door
    @FXML private void changeDirection() {
        gameMap.setDoorVertical(myController.getCurrSector(), vOpenButton.isSelected());

        setDoorOptions();
        myController.setIsModified();
    }

    // change which side of the door opens (or if it opens in the middle)
    @FXML private void changeOpening() {
        int pos;

        if (doorOpen1.isSelected())
            pos = 0;
        else if (doorOpen2.isSelected())
            pos = 1;
        else
            pos = 2;

        gameMap.setDoorType(myController.getCurrSector(), pos);

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void setDoorSpeed() {
        try {
            double newVal = Double.parseDouble(speedTextField.getText());
            myController.getCurrSector().setDoorSpeed(newVal);
        }
        catch (NumberFormatException nfe) {
            // if value is invalid, do nothing
        }

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void openTex1() {
        String t = myController.openTextureFile();

        if (t != null)
            gameMap.setDoorTex1(myController.getCurrSector(), t);

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void openTex2() {
        String t = myController.openTextureFile();

        if (t != null)
            gameMap.setDoorTex2(myController.getCurrSector(), t);

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void setTex1() {
        String fullName = myController.adjustName(fileText1.getText());

        if (fullName != null) {
            gameMap.setDoorTex1(myController.getCurrSector(), fullName);
        }

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void setTex2() {
        String fullName = myController.adjustName(fileText2.getText());

        if (fullName != null) {
            gameMap.setDoorTex2(myController.getCurrSector(), fullName);
        }

        setDoorOptions();
        myController.setIsModified();
    }

    @FXML private void toggleShootToOpen() {
        myController.getCurrSector().setDoorShootToOpen(shootToOpenButton.isSelected());
    }

    // show a ChoiceDialog to let the user choose a key for the door
    @FXML private void chooseKey() {
        // change the list of options to the keys in the GameMap
        keyChooser.getItems().clear();
        keyChooser.getItems().add(NO_KEY); // include an option for no key
        keyChooser.getItems().addAll(gameMap.getKeys());
        // make the default choice the current key
        String currKey = myController.getCurrSector().getDoorKey();
        keyChooser.setSelectedItem(currKey == null ? NO_KEY : currKey);

        // get the user's choice
        Optional<String> os = keyChooser.showAndWait();

        if (os.isPresent()) {
            String choice = os.get();
            System.out.println("Choice: " + choice);

            // for no key, set the sector's key to null
            if (choice.equals(NO_KEY)) {
                gameMap.setDoorKey(myController.getCurrSector(), null);
            }

            // for a new key, show a new dialog
            else if (choice.equals(NEW_KEY)) {
                String newKey = makeNewKey();
                if (newKey != null)
                    gameMap.setDoorKey(myController.getCurrSector(), newKey);
            }

            // otherwise, set the key name
            else {
                gameMap.setDoorKey(myController.getCurrSector(), choice);
            }
        }

        setDoorOptions();
        myController.setIsModified();
    }

    // prompt the user to create a new key for the door
    // add it to the map's list of keys
    // return null if unsuccessful
    private String makeNewKey() {
        Optional<String> newKey = keyMaker.showAndWait();

        if (newKey.isPresent() && !newKey.get().equals(NO_KEY) && !newKey.get().equals(NEW_KEY)) {
            gameMap.addKey(newKey.get());
            return newKey.get();
        }
        else {
            return null;
        }
    }
}
