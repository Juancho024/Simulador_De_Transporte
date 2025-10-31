package Controller;

import Model.RedParada;
import Model.Ruta;
import Utilities.paths;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class ListadoRuta implements Initializable {
    @FXML
    private Pane PaneModificar;

    @FXML
    private Pane PanePrincipal;

    @FXML
    private Button btnCancelarMod;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnRealizarMod;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableColumn<Ruta, Double> colCosto;

    @FXML
    private TableColumn<Ruta, String> colDestino;

    @FXML
    private TableColumn<Ruta, Double> colDistancia;

    @FXML
    private TableColumn<Ruta, String> colOrigen;

    @FXML
    private TableColumn<Ruta, Double> colTiempo;

    @FXML
    private TableColumn<Ruta, Integer> colTransbordo;

    @FXML
    private TableColumn<Ruta, String> colEstado;


    @FXML
    private Label lbCosto;

    @FXML
    private Label lbDestino;

    @FXML
    private Label lbDistancia;

    @FXML
    private Label lbOrigen;

    @FXML
    private Label lbPrincipal;

    @FXML
    private Label lbTiempo;

    @FXML
    private Label lbTransbordo;

    @FXML
    private TableView<Ruta> tableRuta;

    @FXML
    private ComboBox<String> cbxDestinoMod;

    @FXML
    private ComboBox<String> cbxOrigenMod;

    @FXML
    private Spinner<Double> spnCostoMod;

    @FXML
    private Spinner<Double> spnDistanciaMod;

    @FXML
    private Spinner<Double> spnTiempoMod;

    @FXML
    private Spinner<Double> spnTransbordoMod;

    @FXML
    private TextField txtBuscarRuta;

    @FXML
    void buscarRuta() {
        txtBuscarRuta.setOnMouseEntered(event -> {
            String cosaBuscada = txtBuscarRuta.getText().toLowerCase();
            List<Ruta> rutasFiltradas = new ArrayList<>();

            for (LinkedList<Ruta> lista : RedParada.getInstance().getRutas().values()) {
                for (Ruta ruta : lista) {
                    if (ruta.getOrigen().getNombre().toLowerCase().contains(cosaBuscada) ||
                            ruta.getDestino().getNombre().toLowerCase().contains(cosaBuscada)) {
                        rutasFiltradas.add(ruta);
                    }
                }
            }

            tableRuta.getItems().setAll(rutasFiltradas);
            tableRuta.refresh();
        });
    }

    @FXML
    void cancelarModificacion(ActionEvent event) {
        PaneModificar.setVisible(false);
        PanePrincipal.setVisible(true);
//Pruebas
//        tableRuta.getSelectionModel().clearSelection();
//        btnRealizarMod.setDisable(true);
//        btnCancelarMod.setDisable(true);
    }

    @FXML
    void eliminarRuta(ActionEvent event) {

    }

    @FXML
    void modificarRuta(ActionEvent event) {
        PanePrincipal.setVisible(false);
        PaneModificar.setVisible(true);
    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        int index = tableRuta.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Ruta ruta = tableRuta.getItems().get(index);
            var origen = RedParada.getInstance().buscarParadaPorNombre(cbxOrigenMod.getValue());
            var destino = RedParada.getInstance().buscarParadaPorNombre(cbxDestinoMod.getValue());

            ruta.setDestino(origen);
            ruta.setOrigen(destino);
            ruta.setDistancia(spnDistanciaMod.getValue().floatValue());
            ruta.setCosto(spnCostoMod.getValue().floatValue());
            ruta.setTiempoRecorrido(spnTiempoMod.getValue().floatValue());
            ruta.setNumTransbordos(spnTransbordoMod.getValue().intValue());

            tableRuta.getItems().set(index, ruta);
            tableRuta.refresh();

            PaneModificar.setVisible(false);
            PanePrincipal.setVisible(true);
            cargarCampos();
        }
    }

    @FXML
    void registrarRuta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_RUTA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Ruta");
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        colOrigen.setCellValueFactory(cellData -> {
            Ruta ruta = cellData.getValue();
            return new SimpleStringProperty(ruta.getOrigen().getNombre());
        });
        colDestino.setCellValueFactory(cellData -> {
            Ruta ruta = cellData.getValue();
            return new SimpleStringProperty(ruta.getDestino().getNombre());
        });
        colDistancia.setCellValueFactory(new PropertyValueFactory<>("Distancia"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("Costo"));
        colTiempo.setCellValueFactory(new PropertyValueFactory<>("tiempoRecorrido"));
        colTransbordo.setCellValueFactory(new PropertyValueFactory<>("numTransbordos"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("posibleEvento"));

        cargarTablas();
        spnDistanciaMod.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1)
        );
        spnCostoMod.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1)
        );
        spnTiempoMod.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1)
        );
        spnTransbordoMod.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, 0, 1)
        );

        tableRuta.setOnMouseClicked(event -> {
            Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
            if (ruta != null) {
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
        tableRuta.getItems().clear();
        List<Ruta> todasLasRutas = new LinkedList<>();

        for (LinkedList<Ruta> lista : RedParada.getInstance().getRutas().values()) {
            todasLasRutas.addAll(lista);
        }

        tableRuta.getItems().setAll(todasLasRutas);
        tableRuta.refresh();
    }

    private void cargarCampos() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
        lbDestino.setText(" " + ruta.getDestino().getNombre());
        lbOrigen.setText(" " + ruta.getOrigen().getNombre());
        lbDistancia.setText(" " + String.valueOf(ruta.getDistancia()));
        lbCosto.setText(" " + String.valueOf(ruta.getCosto()));
        lbTiempo.setText(" " + String.valueOf(ruta.getTiempoRecorrido()));
        lbTransbordo.setText(" " + String.valueOf(ruta.getNumTransbordos()));
    }

    private void cargarCamposMod() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
       if(ruta != null){
           cbxDestinoMod.setValue(ruta.getDestino().getNombre());
           cbxOrigenMod.setValue(ruta.getOrigen().getNombre());
           spnDistanciaMod.getValueFactory().setValue((double) ruta.getDistancia());
           spnCostoMod.getValueFactory().setValue((double) ruta.getCosto());
           spnTiempoMod.getValueFactory().setValue((double) ruta.getTiempoRecorrido());
           spnTransbordoMod.getValueFactory().setValue((double) ruta.getNumTransbordos());
           cargarParadas();
       }
    }

    public void cargarParadas() {
        RedParada redParada = RedParada.getInstance();
        if (redParada != null && !redParada.getLugar().isEmpty()) {
            cbxDestinoMod.setItems(FXCollections.observableArrayList(redParada.getLugar().keySet()));
            cbxOrigenMod.setItems(FXCollections.observableArrayList(redParada.getLugar().keySet()));
        } else {
            cbxDestinoMod.setItems(FXCollections.observableArrayList());
            cbxOrigenMod.setItems(FXCollections.observableArrayList());
        }
    }
}
