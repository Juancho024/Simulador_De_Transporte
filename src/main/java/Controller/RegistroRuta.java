package Controller;

import Model.Parada;
import Model.RedParada;
import Model.Ruta;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;


public class RegistroRuta implements Initializable {
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
    private Spinner<Double> spnCosto;

    @FXML
    private Spinner<Double> spnDistancia;
    @FXML
    private Spinner<Double> spnTiempo;

    @FXML
    private Spinner<Integer> spnTransbordo;

    @FXML
    void Cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void registrarRuta(ActionEvent event) {
        String origen = cbxOrigen.getValue();
        String destino = cbxDestino.getValue();
        double costo = spnCosto.getValue();
        double distancia = spnDistancia.getValue();
        int transbordo = spnTransbordo.getValue();
        double tiempo = spnTiempo.getValue();

        Parada auxOrigen = RedParada.getInstance().buscarParadaPorNombre(origen);
        Parada auxDestino = RedParada.getInstance().buscarParadaPorNombre(destino);

        if(auxOrigen == null || auxDestino == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Parada no encontrada");
            alert.setContentText("La parada de origen o destino no existe.");
            alert.showAndWait();
            return;
        }
        if(origen.equals(destino)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Paradas iguales");
            alert.setContentText("La parada de origen y destino no pueden ser iguales.");
            alert.showAndWait();
            return;
        }
        if(RedParada.getInstance().existeRutaIgual(new Ruta(auxOrigen, auxDestino, (int) distancia, (float) tiempo, (float) costo, (float) transbordo, "Normales"))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Ruta existente");
            alert.setContentText("Ya existe una ruta entre las paradas seleccionadas.");
            alert.showAndWait();
            return;
        }
        if(distancia <= 0 || tiempo <= 0 || costo < 0 || transbordo < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Valores inválidos");
            alert.setContentText("Por favor, ingrese valores válidos para distancia, tiempo, costo y transbordos.");
            alert.showAndWait();
            return;
        }
        try{
                Ruta ruta = new Ruta(auxOrigen, auxDestino, (int) distancia, (float) tiempo, (float) costo, (float) transbordo, "Normales");
                RedParada.getInstance().agregarRuta(ruta);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText("Ruta registrada");
                alert.setContentText("La ruta ha sido registrada exitosamente.");
                alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al registrar");
            alert.setContentText("No se pudo registrar la ruta. Intente nuevamente.");
            alert.showAndWait();
        }
        limpiarCampos();
    }

    private void limpiarCampos() {
        cbxOrigen.setValue(null);
        cbxDestino.setValue(null);
        spnCosto.getValueFactory().setValue(100.0);
        spnDistancia.getValueFactory().setValue(1.0);
        spnTransbordo.getValueFactory().setValue(0);
        spnTiempo.getValueFactory().setValue(1.0);

    }

    @FXML
    void  initialize() {
    }

    private void configurarSpinners() {
        SpinnerValueFactory<Double> Distancia = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1);
        spnDistancia.setValueFactory(Distancia);
        SpinnerValueFactory<Double> costo = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 100.0, 1.0);
        spnCosto.setValueFactory(costo);
        SpinnerValueFactory<Double> tiempo = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 500.0, 1.0, 1.0);
        spnTiempo.setValueFactory(tiempo);
        SpinnerValueFactory<Integer> transbordo = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0, 1);
        spnTransbordo.setValueFactory(transbordo);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSpinners();
        cargarParadas();
    }

    public void cargarParadas() {
        RedParada redParada = RedParada.getInstance();
        if (redParada != null && !redParada.getLugar().isEmpty()) {
            cbxOrigen.setItems(FXCollections.observableArrayList(redParada.getLugar().keySet()));
            cbxDestino.setItems(FXCollections.observableArrayList(redParada.getLugar().keySet()));
        } else {
            cbxOrigen.setItems(FXCollections.observableArrayList());
            cbxDestino.setItems(FXCollections.observableArrayList());
        }
    }
}
