package Controller;

import Model.Ruta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

public class RegistroRuta {
    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private ComboBox<String> cbxDestino;

    @FXML
    private ComboBox<String> cbxOrigen;

    @FXML
    private Label lbId;

    @FXML
    private Label lbId1;

    @FXML
    private Label lbId11;

    @FXML
    private Label lbId111;

    @FXML
    private Label lbId1111;

    @FXML
    private Label lbId11111;

    @FXML
    private Label lbPrincipal;

    @FXML
    private Spinner<Integer> spnCosto;

    @FXML
    private Spinner<Float> spnDistancia;

    @FXML
    private Spinner<Integer> spnTransbordo;

    @FXML
    void Cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void registrarRuta(ActionEvent event) {
        Ruta ruta = new Ruta();
        ruta.setOrigen(cbxOrigen.getValue());
        ruta.setDestino(cbxDestino.getValue());
        ruta.setCosto(spnCosto.getValue());
        ruta.setNumTransbordos(spnTransbordo.getValue());
        ruta.setDistancia(spnDistancia.getValue());



    }

    @FXML
    void  initialize() {

    }

}
