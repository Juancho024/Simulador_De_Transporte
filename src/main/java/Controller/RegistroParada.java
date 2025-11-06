package Controller;

import DataBase.ParadaDAO;
import Model.Parada;
import Model.RedParada;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
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
    private ImageView ImgIcono;

    private byte[] iconoBytes;

    @FXML
    private ImageView imgIconoDefault;

    @FXML
    void Cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void agregarIcono(ActionEvent event) {
        imgIconoDefault.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Icono");
        fileChooser.setInitialDirectory(new File("C:/Users/esteb/Downloads"));

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Archivos de Imagen", Arrays.asList("*.png", "*.jpg", "*.jpeg"));

        fileChooser.getExtensionFilters().add(imageFilter);
        File file = fileChooser.showOpenDialog(btnIcono.getScene().getWindow());

        if (file != null) {
            try {
                iconoBytes = Files.readAllBytes(file.toPath());
                Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));
                ImgIcono.setImage(img);

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Archivo");
                alert.setHeaderText("Error al cargar la imagen");
                alert.setContentText("No se pudo leer el archivo: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    void registrarParada(ActionEvent event) {
        String tipoTransporte = cbxTipoTransporte.getValue();
        String nombre = txtNombre.getText();
        int latitud = spnLatitud.getValue();
        int longitud = spnLongitud.getValue();

        if(iconoBytes == null || cbxTipoTransporte.getValue() == null|| txtNombre.getText().isEmpty() || latitud == 0 || longitud == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de validación");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, complete todos los campos obligatorios.");
            alert.showAndWait();
            return;
        }

        try{
            Parada nuevaParada = new Parada(nombre, tipoTransporte, latitud, longitud, iconoBytes);
            ParadaDAO.getInstance().guardarParada(nuevaParada);
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
        iconoBytes = null;
        ImgIcono.setImage(null);
        spnLatitud.getValueFactory().setValue(0);
        spnLongitud.getValueFactory().setValue(0);
        imgIconoDefault.setVisible(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSpinnersandCombox();
        iconoBytes = null;
    }

    private void configurarSpinnersandCombox() {
        SpinnerValueFactory<Integer> Latitud = new SpinnerValueFactory.IntegerSpinnerValueFactory(-90, 90, 0, 1);
        spnLatitud.setValueFactory(Latitud);
        SpinnerValueFactory<Integer> Longitud = new SpinnerValueFactory.IntegerSpinnerValueFactory(-180, 180, 0, 1);
        spnLongitud.setValueFactory(Longitud);
        cbxTipoTransporte.getItems().addAll("Bus","Tren","Metro","Tranvía","Ferry");
    }
}
