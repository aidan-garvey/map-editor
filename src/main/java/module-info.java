module aidan_garvey.mapeditor {
    requires javafx.controls;
    requires javafx.fxml;


    opens aidan_garvey.mapeditor to javafx.fxml;
    exports aidan_garvey.mapeditor;
}