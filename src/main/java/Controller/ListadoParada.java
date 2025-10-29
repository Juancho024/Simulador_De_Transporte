package Controller;

import Model.Parada;
import Model.RedParada;
import com.sun.tools.javac.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import javafx.scene.image.ImageView;
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
    private TextField txtLatitud;

    @FXML
    private TextField txtLongitud;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTipoTransporte;

    @FXML
    void ActualizarParada(ActionEvent event) {
        tableParada.setOnMouseClicked(ActionEvent -> {
           int index = tableParada.getSelectionModel().getSelectedIndex();
           if(index >= 0){
               btnActualizar.setOnMouseClicked(event1 -> {
                   Parada parada = tableParada.getItems().get(index);
                   parada.setNombre(txtNombre.getText());
                   parada.setTipoTransporte(txtTipoTransporte.getText());
                   parada.setPosiciony(Integer.parseInt(txtLatitud.getText()));
                   parada.setPosicionx(Integer.parseInt(txtLongitud.getText()));

                   tableParada.getItems().set(index, parada);
                   tableParada.refresh();

                   paneModificacion.setVisible(false);
                   panePrincipal.setVisible(true);
               });
           }
        });
    }

    @FXML
    void buscarParada(ActionEvent event) {

    }

    @FXML
    void cancelarModificacion(ActionEvent event) {
        paneModificacion.setVisible(false);
        panePrincipal.setVisible(true);
    }

    @FXML
    void eliminiarParada(ActionEvent event) {

    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        panePrincipal.setVisible(false);
        paneModificacion.setVisible(true);

        tableParada.setOnMouseClicked(ActionEvent -> {
            Parada parada = tableParada.getSelectionModel().getSelectedItem();
            if (tableParada.getSelectionModel().getSelectedItem() != null) {
                cargarCamposMod();
            }
        });
    }

    private void cargarCamposMod() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        txtNombre.setText(parada.getNombre());
        txtTipoTransporte.setText(parada.getTipoTransporte());
        txtLatitud.setText(String.valueOf(parada.getPosiciony()));
        txtLongitud.setText(String.valueOf(parada.getPosicionx()));
    }

    @FXML
    void registrarParada(ActionEvent event) {

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));
        colLatitud.setCellValueFactory(new PropertyValueFactory<>("posiciony"));
        colLongitud.setCellValueFactory(new PropertyValueFactory<>("posicionx"));

        tableParada.getItems().clear();
        List<Parada> listaParadas = new LinkedList<>(RedParada.getInstance().getLugar().values());
        tableParada.getItems().setAll(listaParadas);
        tableParada.refresh();

        tableParada.setOnMouseClicked(ActionEvent -> {
            Parada parada = tableParada.getSelectionModel().getSelectedItem();
            if (tableParada.getSelectionModel().getSelectedItem() != null) {
                cargarCampos();
            }
        });
    }

    private void cargarCampos() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        lbNombre.setText(" "+parada.getNombre());
        lbTipoTransporte.setText(" "+ parada.getTipoTransporte());
        lbLatitud.setText(" "+ String.valueOf(parada.getPosiciony()));
        lbLongitud.setText(" "+ String.valueOf(parada.getPosicionx()));
    }

}
