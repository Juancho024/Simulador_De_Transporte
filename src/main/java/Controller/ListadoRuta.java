package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.Parada;
import Model.RedParada;
import Model.Ruta;
import Utilities.paths;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    private Pane paneAccidente;

    @FXML
    private Pane paneHuelga;

    @FXML
    private Pane paneRetrasado;

    @FXML
    void buscarRuta() {
        String cosaBuscada = txtBuscarRuta.getText().toLowerCase();
        List<Ruta> rutasFiltradas = new ArrayList<>();

        for (LinkedList<Ruta> lista : RutaDAO.getInstancia().obtenerRutas().values()) {
            for (Ruta ruta : lista) {
                if (ruta.getOrigen().getNombre().toLowerCase().contains(cosaBuscada) ||
                        ruta.getDestino().getNombre().toLowerCase().contains(cosaBuscada) ||
                String.valueOf(ruta.getCosto()).contains(cosaBuscada) ||
                        String.valueOf(ruta.getNumTransbordos()).contains(cosaBuscada) ||
                String.valueOf(ruta.getDistancia()).contains(cosaBuscada) ||
                        String.valueOf(ruta.getTiempoRecorrido()).contains(cosaBuscada) ||
                        ruta.getPosibleEvento().toLowerCase().contains(cosaBuscada)){
                    rutasFiltradas.add(ruta);
                }
            }
        }

        tableRuta.getItems().setAll(rutasFiltradas);
        tableRuta.refresh();
    }

    @FXML
    void cancelarModificacion(ActionEvent event) {
        PaneModificar.setVisible(false);
        PanePrincipal.setVisible(true);
//Pruebas
//        tableRuta.getSelectionModel().clearSelection();
        limpiarCampos();
    }

    private void limpiarCampos() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        lbDestino.setText("");
        lbOrigen.setText("");
        lbDistancia.setText("");
        lbCosto.setText("");
        lbTiempo.setText("");
        lbTransbordo.setText("");
    }

    @FXML
    void eliminarRuta(ActionEvent event) {
        int index = tableRuta.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Estás seguro de que deseas eliminar esta ruta?");
            alert.setContentText("Esta acción no se puede deshacer.");
            Optional<ButtonType> result = alert.showAndWait();
            alert.setResizable(false);
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Ruta ruta = tableRuta.getItems().get(index);
                RutaDAO.getInstancia().eliminarRuta(ruta.getId());
                tableRuta.getItems().remove(index);
                tableRuta.refresh();
                limpiarCampos();
            }
        }
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
            if(cbxDestinoMod.getValue().equals(cbxOrigenMod.getValue())){
                Alert alertError = new Alert(Alert.AlertType.ERROR);
                alertError.setTitle("Error");
                alertError.setHeaderText("Paradas iguales");
                alertError.setContentText("La parada de origen y destino no pueden ser iguales.");
                alertError.showAndWait();
                return;
            }
            boolean diferenteOrigen = !tableRuta.getItems().get(index).getOrigen().getNombre().equals(cbxOrigenMod.getValue());
            boolean diferenteDestino = !tableRuta.getItems().get(index).getDestino().getNombre().equals(cbxDestinoMod.getValue());
            if((diferenteDestino || diferenteOrigen) && RutaDAO.getInstancia().existeRutaIgual(
                    new Ruta(RutaDAO.getInstancia().buscarParadaPorNombre(cbxOrigenMod.getValue()),
                            RutaDAO.getInstancia().buscarParadaPorNombre(cbxDestinoMod.getValue()),
                            spnDistanciaMod.getValue().floatValue(),
                            spnTiempoMod.getValue().floatValue(),
                            spnCostoMod.getValue().floatValue(),
                            spnTransbordoMod.getValue().intValue(),
                            "Normales"))){
                Alert alertError = new Alert(Alert.AlertType.ERROR);
                alertError.setTitle("Error");
                alertError.setHeaderText("Ruta existente");
                alertError.setContentText("Ya existe una ruta entre las paradas seleccionadas.");
                alertError.showAndWait();
                return;
            }
            if(spnDistanciaMod.getValue().floatValue() <= 0 ||
                    spnTiempoMod.getValue().floatValue() <= 0 ||
                    spnCostoMod.getValue().floatValue() < 0 ||
                    spnTransbordoMod.getValue().intValue() < 0){
                Alert alertError = new Alert(Alert.AlertType.ERROR);
                alertError.setTitle("Error de validación");
                alertError.setHeaderText(null);
                alertError.setContentText("Por favor, ingrese valores válidos para todos los campos.");
                alertError.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar modificación");
            alert.setHeaderText("¿Estás seguro de que deseas modificar esta ruta?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.setResizable(false);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Ruta ruta = tableRuta.getItems().get(index);
                var origen = RutaDAO.getInstancia().buscarParadaPorNombre(cbxOrigenMod.getValue());
                var destino = RutaDAO.getInstancia().buscarParadaPorNombre(cbxDestinoMod.getValue());

                ruta.setDestino(origen);
                ruta.setOrigen(destino);
                ruta.setDistancia(spnDistanciaMod.getValue().floatValue());
                ruta.setCosto(spnCostoMod.getValue().floatValue());
                ruta.setTiempoRecorrido(spnTiempoMod.getValue().floatValue());
                ruta.setNumTransbordos(spnTransbordoMod.getValue().intValue());
                RutaDAO.getInstancia().actualizarRuta(ruta);
                tableRuta.getItems().set(index, ruta);
                tableRuta.refresh();

                PaneModificar.setVisible(false);
                PanePrincipal.setVisible(true);
                cargarCampos();
            }
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
        paneAccidente.setVisible(false);
        paneHuelga.setVisible(false);
        paneRetrasado.setVisible(false);
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
        colEstado.setCellFactory(column -> new TableCell<Ruta, String>() {
            private final Circle statusCircle = new Circle(5);

            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (estado.equalsIgnoreCase("Normales")) {
                        statusCircle.setFill(Color.web("#66BB6A")); // Verde bonito
                    } else if(estado.equalsIgnoreCase("Huelga")) {
                        statusCircle.setFill(Color.web("#FFA726")); // Naranja bonito
                    } else if (estado.equalsIgnoreCase("Retraso")) {
                        statusCircle.setFill(Color.web("#EF5350")); // Rojo bonito
                    } else {
                        statusCircle.setFill(Color.GRAY); //Gris
                    }

                    // Centrar el círculo en la celda
                    setAlignment(Pos.CENTER);
                    setGraphic(statusCircle);
                    setText(null); // No mostrar texto (solo el gráfico)
                }
            }
        });

        cargarTablas();
        spnDistanciaMod.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1));
        spnCostoMod.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1));
        spnTiempoMod.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100.0, 1.0, 0.1));
        spnTransbordoMod.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, 0, 1));

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

        for (LinkedList<Ruta> lista : RutaDAO.getInstancia().obtenerRutas().values()) {
            todasLasRutas.addAll(lista);
        }

        tableRuta.getItems().setAll(todasLasRutas);
        tableRuta.refresh();
    }

    private void cargarCampos() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
        if(ruta.getPosibleEvento().equals("Normales")){
            paneAccidente.setVisible(false);
            paneHuelga.setVisible(false);
            paneRetrasado.setVisible(true); //Normales
        } else if (ruta.getPosibleEvento().equals("Accidente")) {
            paneAccidente.setVisible(true);
            paneHuelga.setVisible(false);
            paneRetrasado.setVisible(false);
        } else if (ruta.getPosibleEvento().equals("Huelga")) {
            paneAccidente.setVisible(false);
            paneHuelga.setVisible(true);
            paneRetrasado.setVisible(false);
        }

        lbDestino.setText(" " + ruta.getDestino().getNombre());
        lbOrigen.setText(" " + ruta.getOrigen().getNombre());
        lbDistancia.setText(" " + String.valueOf(ruta.getDistancia()));
        lbCosto.setText(" " + String.valueOf(ruta.getCosto()));
        lbTiempo.setText(" " + String.valueOf(ruta.getTiempoRecorrido()));
        lbTransbordo.setText(" " + String.valueOf(ruta.getNumTransbordos()));
    }

    private void cargarCamposMod() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
        if (ruta != null) {
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
        HashMap<Long, Parada> paradas = ParadaDAO.getInstance().obtenerParadas();
        if (paradas != null && !paradas.isEmpty()) {
            java.util.List<String> nombresParadas = paradas.values().stream()
                    .map(Parada::getNombre)
                    .collect(Collectors.toList());

            cbxDestinoMod.setItems(FXCollections.observableArrayList(nombresParadas));
            cbxOrigenMod.setItems(FXCollections.observableArrayList(nombresParadas));
        } else {
            cbxDestinoMod.setItems(FXCollections.observableArrayList("No hay ninguna Parada Registrada."));
            cbxOrigenMod.setItems(FXCollections.observableArrayList("No hay ninguna Parada Registrada."));
        }
    }

}
