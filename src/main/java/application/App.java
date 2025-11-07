package application;

import Controller.Principal;
import Model.RedParada;
import Utilities.paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.PRINCIPAL));
        AnchorPane pane = (AnchorPane) loader.load(); //Cualquier cosa

        Scene scene = new Scene(pane);
        stage.setTitle("Simulador de Transporte de Ruta");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }
}
