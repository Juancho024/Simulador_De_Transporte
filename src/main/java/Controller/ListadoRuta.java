package Controller;

import Model.RedParada;
import Model.Ruta;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

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
    private TextField txtCostoMod;

    @FXML
    private TextField txtDestinoMod;

    @FXML
    private TextField txtDistanciaMod;

    @FXML
    private TextField txtOrigenMod;

    @FXML
    private TextField txtTiempoMod;

    @FXML
    private TextField txtTransbordoMod;

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
    }

    @FXML
    void eliminarRuta(ActionEvent event) {

    }

    @FXML
    void modificarRuta(ActionEvent event) {
        PanePrincipal.setVisible(false);
        PaneModificar.setVisible(true);

        tableRuta.setOnMouseClicked(ActionEvent -> {
            Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
            if (tableRuta.getSelectionModel().getSelectedItem() != null) {
                cargarCamposMod();
            }
        });

    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        tableRuta.setOnMouseClicked(ActionEvent -> {
            int index = tableRuta.getSelectionModel().getSelectedIndex();
            if(index >= 0){
                Ruta ruta = tableRuta.getItems().get(index);
                ruta.setDistancia((float) Double.parseDouble(txtDistanciaMod.getText()));
                ruta.setCosto((float) Double.parseDouble(txtCostoMod.getText()));
                ruta.setTiempoRecorrido((float) Double.parseDouble(txtTiempoMod.getText()));
                ruta.setNumTransbordos(Integer.parseInt(txtTransbordoMod.getText()));

                tableRuta.getItems().set(index, ruta);
                tableRuta.refresh();

                PaneModificar.setVisible(false);
                PanePrincipal.setVisible(true);
            }
        });
    }

    @FXML
    void registrarRuta(ActionEvent event) {

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        tableRuta.getItems().clear();
        List<Ruta> todasLasRutas = new LinkedList<>();

        for (LinkedList<Ruta> lista : RedParada.getInstance().getRutas().values()) {
            todasLasRutas.addAll(lista);
        }

        tableRuta.getItems().setAll(todasLasRutas);
        tableRuta.refresh();

        tableRuta.setOnMouseClicked(event -> {
            Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
            if (tableRuta.getSelectionModel().getSelectedItem() != null) {
                cargarCampos();
            }
        });

    }

    private void cargarCampos() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
        lbDestino.setText(" "+ ruta.getDestino().getNombre());
        lbOrigen.setText(" "+ ruta.getOrigen().getNombre());
        lbDistancia.setText(" "+ String.valueOf(ruta.getDistancia()));
        lbCosto.setText(" "+ String.valueOf(ruta.getCosto()));
        lbTiempo.setText(" "+ String.valueOf(ruta.getTiempoRecorrido()));
        lbTransbordo.setText(" "+ String.valueOf(ruta.getNumTransbordos()));
    }

    private void cargarCamposMod() {
        Ruta ruta = tableRuta.getSelectionModel().getSelectedItem();
        txtOrigenMod.setText(ruta.getOrigen().getNombre());
        txtDestinoMod.setText(ruta.getDestino().getNombre());
        txtDistanciaMod.setText(String.valueOf(ruta.getDistancia()));
        txtCostoMod.setText(String.valueOf(ruta.getCosto()));
        txtTiempoMod.setText(String.valueOf(ruta.getTiempoRecorrido()));
        txtTransbordoMod.setText(String.valueOf(ruta.getNumTransbordos()));
    }

}
