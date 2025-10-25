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

public class ListadoParada implements Initializable {

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableColumn<?, ?> colNombre;

    @FXML
    private TableColumn<?, ?> colTIpoTransporte;

    @FXML
    private Label lbPrincipal;

    @FXML
    private TableView<?> tableRuta;

    @FXML
    void eliminarParada(ActionEvent event) {

    }

    @FXML
    void modificarParada(ActionEvent event) {

    }

    @FXML
    void registrarParada(ActionEvent event) {

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
