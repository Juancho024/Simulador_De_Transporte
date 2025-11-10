package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.Parada;
import Utilities.paths;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class ListadoParada implements Initializable {

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnCancelarMod;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableColumn<Parada, Integer> colLatitud;

    @FXML
    private TableColumn<Parada, Integer> colLongitud;

    @FXML
    private TableColumn<Parada, String> colNombre;

    @FXML
    private TableColumn<Parada, String> colTipoTransporte;

    @FXML
    private ImageView imgFondo;

    @FXML
    private ImageView imgFondoMod;

    @FXML
    private Label lbLatitud;

    @FXML
    private Label lbLongitud;

    @FXML
    private Label lbNombre;

    @FXML
    private Label lbPrincipal;

    @FXML
    private Label lbTipoTransporte;

    @FXML
    private Pane paneModificacion;

    @FXML
    private Pane panePrincipal;

    @FXML
    private TableView<Parada> tableParada;

    @FXML
    private TextField txtBuscarParada;

    @FXML
    private ComboBox<String> cbxTipoTransporte;

    @FXML
    private Spinner<Double> spnLatitud;

    @FXML
    private Spinner<Double> spnLongitud;

    @FXML
    private TextField txtNombre;

    @FXML
    private Button btnIcono;

    byte[] iconoBytes;

    @FXML
    void agregarIcono(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Icono");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Archivos de Imagen", Arrays.asList("*.png", "*.jpg", "*.jpeg"));

        fileChooser.getExtensionFilters().add(imageFilter);
        File file = fileChooser.showOpenDialog(btnIcono.getScene().getWindow());

        if (file != null) {
            try {
                iconoBytes = Files.readAllBytes(file.toPath());
                Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));
                imgFondoMod.setImage(img);

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
    void ActualizarParada(ActionEvent event) {
        int index = tableParada.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            if(iconoBytes == null){
                Alert alertIcono = new Alert(Alert.AlertType.ERROR);
                alertIcono.setTitle("Error de validación");
                alertIcono.setHeaderText("Faltan datos obligatorios");
                alertIcono.setContentText("Por favor, asegúrate de haber seleccionado un icono para la parada.");
                alertIcono.showAndWait();
                return;
            }
            if(cbxTipoTransporte.getValue() == null || txtNombre.getText().isEmpty()){
                Alert alertCampos = new Alert(Alert.AlertType.ERROR);
                alertCampos.setTitle("Error de validación");
                alertCampos.setHeaderText("Faltan datos obligatorios");
                alertCampos.setContentText("Por favor, completa todos los campos obligatorios antes de actualizar la parada.");
                alertCampos.showAndWait();
                return;
            }
            if(spnLatitud.getValue() == 0 || spnLongitud.getValue() == 0){
                Alert alertCampos = new Alert(Alert.AlertType.ERROR);
                alertCampos.setTitle("Error de validación");
                alertCampos.setHeaderText("Faltan datos obligatorios");
                alertCampos.setContentText("La latitud y longitud no pueden ser cero. Por favor, ingresa valores válidos.");
                alertCampos.showAndWait();
                return;
            }
            if ((spnLatitud.getValue() >= 0 && spnLatitud.getValue() <= 550) &&
                    (spnLongitud.getValue() >= 0 && spnLongitud.getValue() <= 900)){
                Alert alertCampos = new Alert(Alert.AlertType.ERROR);
                alertCampos.setTitle("Error de validación");
                alertCampos.setHeaderText("Valores fuera de rango");
                alertCampos.setContentText("La latitud y longitud deben estar entre -100 y 100. Por favor, ingresa valores válidos.");
                alertCampos.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar modificación");
            alert.setHeaderText("¿Estás seguro de que deseas modificar esta parada?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.setResizable(false);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Parada parada = tableParada.getItems().get(index);
                parada.setNombre(txtNombre.getText());
                parada.setTipoTransporte(cbxTipoTransporte.getValue());
                parada.setPosiciony(Integer.parseInt(spnLatitud.getValue().toString()));
                parada.setPosicionx(Integer.parseInt(spnLongitud.getValue().toString()));
                parada.setIcono(iconoBytes);

                ParadaDAO.getInstance().actualizarParada(parada);
                tableParada.getItems().set(index, parada);
                tableParada.refresh();

                paneModificacion.setVisible(false);
                panePrincipal.setVisible(true);
                cargarCampos();
            }
        }
    }

    @FXML
    void buscarParada(ActionEvent event) {
        String criterio = txtBuscarParada.getText().toLowerCase();
        List<Parada> paradasFiltradas = new LinkedList<>();

        for (Parada parada : ParadaDAO.getInstance().obtenerParadas().values()) {
            if (parada.getNombre().toLowerCase().contains(criterio) || //Buscar formar de evaluar sin acento
                    parada.getTipoTransporte().toLowerCase().contains(criterio) ||
                    String.valueOf(parada.getPosiciony()).contains(criterio) ||
                    String.valueOf(parada.getPosicionx()).contains(criterio)) {
                paradasFiltradas.add(parada);
            }
        }

        tableParada.getItems().setAll(paradasFiltradas);
        tableParada.refresh();
    }

    @FXML
    void cancelarModificacion(ActionEvent event) {
        paneModificacion.setVisible(false);
        panePrincipal.setVisible(true);
        //Pruebas
//        tableParada.getSelectionModel().clearSelection();
        limpiarCampos();
    }

    private void limpiarCampos() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        lbNombre.setText("");
        lbTipoTransporte.setText("");
        lbLatitud.setText("");
        lbLongitud.setText("");
    }

    @FXML
    void eliminiarParada(ActionEvent event) {
        int index = tableParada.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Estás seguro de que deseas eliminar esta parada?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.setResizable(false);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Parada parada = tableParada.getItems().get(index);
                ParadaDAO.getInstance().eliminarParada(parada.getId()); //Eliminar de la base de datos
                RutaDAO.getInstancia().eliminarRutaByParada(parada.getId()); //Eliminar rutas asociadas a la parada
                cargarTablas();
                tableParada.refresh();
                limpiarCampos();
            }
        }
    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        panePrincipal.setVisible(false);
        paneModificacion.setVisible(true);
    }

    private void cargarCamposMod() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        if (parada != null) {
            txtNombre.setText(parada.getNombre());
            cbxTipoTransporte.setValue(parada.getTipoTransporte());
            spnLatitud.getValueFactory().setValue(parada.getPosiciony());
            spnLongitud.getValueFactory().setValue(parada.getPosicionx());
            iconoBytes = parada.getIcono();
            Image img = new Image(new java.io.ByteArrayInputStream(parada.getIcono()));
            imgFondoMod.setImage(img);
        }
    }

    @FXML
    void registrarParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Parada");
            Stage ownerStage = (Stage) btnRegistrar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> cargarTablas());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { //Poner los botones de modificar desabilidatos hasta que se seleccione una parada
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        cbxTipoTransporte.getItems().addAll("Bus", "Tren", "Metro", "Tranvía", "Ferry");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));
        colLatitud.setCellValueFactory(new PropertyValueFactory<>("posiciony"));
        colLongitud.setCellValueFactory(new PropertyValueFactory<>("posicionx"));

        spnLatitud.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 550, 0)
        );

        spnLongitud.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 900, 0)
        );

        cargarTablas();
        tableParada.setOnMouseClicked(ActionEvent -> {
            Parada parada = tableParada.getSelectionModel().getSelectedItem();
            if (parada != null) {
                cargarCampos();
                cargarCamposMod();
                btnModificar.setDisable(false);
                btnEliminar.setDisable(false);
            } else {
                btnModificar.setDisable(true);
                btnEliminar.setDisable(true);
            }
        });
    }

    private void cargarTablas() {
        tableParada.getItems().clear();
        tableParada.getItems().setAll(ParadaDAO.getInstance().obtenerParadas().values());
        tableParada.refresh();
    }

    private void cargarCampos() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        lbNombre.setText(" " + parada.getNombre());
        lbTipoTransporte.setText(" " + parada.getTipoTransporte());
        lbLatitud.setText(" " + String.valueOf(parada.getPosiciony()));
        lbLongitud.setText(" " + String.valueOf(parada.getPosicionx()));
        Image img = new Image(new java.io.ByteArrayInputStream(parada.getIcono()));
        imgFondo.setImage(img); //Revisar y probar
    }

}
