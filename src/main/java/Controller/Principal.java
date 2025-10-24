package Controller;

import Model.RedParada;
import Utilities.paths;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Principal {

    private RedParada redParada;

    @FXML
    private Button btnListados;

    @FXML
    private Button btnRegistros;

    @FXML
    private MenuItem itemParada;

    @FXML
    private MenuItem itemRuta;

    @FXML
    private ContextMenu menuRegistros;

    public Principal() {
        //debe ir asi
    }

    @FXML
    void initialize() {
        btnRegistros.setOnAction(e -> {
            menuRegistros.show(btnRegistros, Side.BOTTOM, 0, 0);
        });
    }

    @FXML
    void crearParada(ActionEvent event) {

    }
    @FXML
    void crearRuta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../RegistroRuta.fxml"));
            AnchorPane root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Registro de Ruta");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RedParada getRedParada() {
        return redParada;
    }

    public void setRedParada(RedParada redParada) {
        this.redParada = redParada;
    }
}
