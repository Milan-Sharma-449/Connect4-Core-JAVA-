module com.example.connect4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.game.connect4 to javafx.fxml;
    exports com.game.connect4;
}