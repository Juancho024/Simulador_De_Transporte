package Controller;

import Model.Parada;
import Model.RedParada;
import Utilities.paths;
import com.sun.tools.javac.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
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
    private Spinner<Integer> spnLatitud;

    @FXML
    private Spinner<Integer> spnLongitud;

    @FXML
    private TextField txtNombre;

    @FXML
    void ActualizarParada(ActionEvent event) {
            int index = tableParada.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                Parada parada = tableParada.getItems().get(index);
                parada.setNombre(txtNombre.getText());
                parada.setTipoTransporte(cbxTipoTransporte.getValue());
                parada.setPosiciony(Integer.parseInt(spnLatitud.getValue().toString()));
                parada.setPosicionx(Integer.parseInt(spnLongitud.getValue().toString()));

                tableParada.getItems().set(index, parada);
                tableParada.refresh();

                paneModificacion.setVisible(false);
                panePrincipal.setVisible(true);
                cargarCampos();
            }
    }

    @FXML
    void buscarParada(ActionEvent event) {
        String criterio = txtBuscarParada.getText().toLowerCase();
        List<Parada> paradasFiltradas = new LinkedList<>();

        for (Parada parada : RedParada.getInstance().getLugar().values()) {
            if (parada.getNombre().toLowerCase().contains(criterio) ||
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
//        btnModificar.setDisable(true);
//        btnEliminar.setDisable(true);
    }

    @FXML
    void eliminiarParada(ActionEvent event) {

    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        panePrincipal.setVisible(false);
        paneModificacion.setVisible(true);
    }

    private void cargarCamposMod() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        if(parada != null) {
            txtNombre.setText(parada.getNombre());
            cbxTipoTransporte.setValue(parada.getTipoTransporte());
            spnLatitud.getValueFactory().setValue(parada.getPosiciony());
            spnLongitud.getValueFactory().setValue(parada.getPosicionx());
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
        cbxTipoTransporte.getItems().addAll("Bus","Tren","Metro","Tranvía","Ferry");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));
        colLatitud.setCellValueFactory(new PropertyValueFactory<>("posiciony"));
        colLongitud.setCellValueFactory(new PropertyValueFactory<>("posicionx"));

        spnLatitud.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0)
        );

        spnLongitud.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0)
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
    private void cargarTablas(){
        tableParada.getItems().clear();
        tableParada.getItems().setAll(RedParada.getInstance().getLugar().values());
        tableParada.refresh();
    }

    private void cargarCampos() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        lbNombre.setText(" " + parada.getNombre());
        lbTipoTransporte.setText(" " + parada.getTipoTransporte());
        lbLatitud.setText(" " + String.valueOf(parada.getPosiciony()));
        lbLongitud.setText(" " + String.valueOf(parada.getPosicionx()));
    }

}
