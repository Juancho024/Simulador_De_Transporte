package Controller;

import Model.Parada;
import Model.RedParada;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RegistroParada implements Initializable {
    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnIcono;

    @FXML
    private Button btnRegistrar;

    @FXML
    private ComboBox<String> cbxTipoTransporte;

    @FXML
    private Label lbPrincipal;

    @FXML
    private Label lbTelefono;

    @FXML
    private Label lbTelefono1;

    @FXML
    private Label lbTelefono11;

    @FXML
    private Label lbTelefono111;

    @FXML
    private Spinner<Integer> spnLatitud;

    @FXML
    private Spinner<Integer> spnLongitud;

    @FXML
    private TextField txtNombre;

    @FXML
    void Cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void agregarIcono(ActionEvent event) {

    }

    @FXML
    void registrarParada(ActionEvent event) {
        String tipoTransporte = cbxTipoTransporte.getValue();
        String nombre = txtNombre.getText();
        int latitud = spnLatitud.getValue();
        int longitud = spnLongitud.getValue();

        if(cbxTipoTransporte.getValue() == null|| txtNombre.getText().isEmpty() || latitud == 0 || longitud == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de validación");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, complete todos los campos obligatorios.");
            alert.showAndWait();
            return;
        }

        try{
            Parada nuevaParada = new Parada(nombre, tipoTransporte, latitud, longitud);
            RedParada.getInstance().agregarParada(nuevaParada);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro Exitoso");
            alert.setHeaderText(null);
            alert.setContentText("La parada ha sido registrada exitosamente.");
            alert.showAndWait();
            limpiarCampos();
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al registrar");
            alert.setHeaderText(null);
            alert.setContentText("Ha ocurrido un error al registrar la parada: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void limpiarCampos() {
        cbxTipoTransporte.setValue(null);
        txtNombre.setText("");
        spnLatitud.getValueFactory().setValue(0);
        spnLongitud.getValueFactory().setValue(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSpinnersandCombox();

    }

    private void configurarSpinnersandCombox() {
        SpinnerValueFactory<Integer> Latitud = new SpinnerValueFactory.IntegerSpinnerValueFactory(-90, 90, 0, 1);
        spnLatitud.setValueFactory(Latitud);
        SpinnerValueFactory<Integer> Longitud = new SpinnerValueFactory.IntegerSpinnerValueFactory(-180, 180, 0, 1);
        spnLongitud.setValueFactory(Longitud);
        cbxTipoTransporte.getItems().addAll("Bus","Tren","Metro","Tranvía","Ferry");
    }
}
