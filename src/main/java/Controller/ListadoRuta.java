package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ListadoRuta implements Initializable {
    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableColumn<?, ?> colCosto;

    @FXML
    private TableColumn<?, ?> colDestino;

    @FXML
    private TableColumn<?, ?> colDistancia;

    @FXML
    private TableColumn<?, ?> colOrigen;

    @FXML
    private TableColumn<?, ?> colTiempo;

    @FXML
    private TableColumn<?, ?> colTransbordo;

    @FXML
    private Label lbPrincipal;

    @FXML
    private TableView<?> tableRuta;

    @FXML
    void eliminarRuta(ActionEvent event) {

    }

    @FXML
    void modificarRuta(ActionEvent event) {

    }

    @FXML
    void registrarRUta(ActionEvent event) {

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
