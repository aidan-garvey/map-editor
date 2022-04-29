package aidan_garvey.mapeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MapEditor extends Application {

    private static int DEFAULT_WIDTH = 1200, DEFAULT_HEIGHT = 675;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader mapEdLoader = new FXMLLoader(MapEditor.class.getResource("map-editor.fxml"));
        Scene mainScene = new Scene(mapEdLoader.load(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setTitle("Map Editor");
        stage.setScene(mainScene);

        MapEdController mec = mapEdLoader.getController();
        mec.setStage(stage);

        stage.show();

        mec.newFile();
    }

    public static void main(String[] args) {
        launch();
    }
}