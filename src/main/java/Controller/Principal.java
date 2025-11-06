package Controller;

import Model.Parada;
import Model.RedParada;
import Model.Ruta;
import Utilities.paths;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Principal {

    @FXML
    private Button btnListados;

    @FXML
    private Button btnRegistros;

    @FXML
    private ImageView imgMapa;

    @FXML
    private MenuItem itemParada;

    @FXML
    private MenuItem itemRuta;

    @FXML
    private ContextMenu menuRegistros;

    @FXML
    private TableView<Parada> TableParada;

    @FXML
    private TableView<Ruta> TableRuta;

    @FXML
    private TableColumn<Ruta, String> colDestino;

    @FXML
    private TableColumn<Ruta, Float> colDistancia;

    @FXML
    private TableColumn<Parada, String> colNombre;

    @FXML
    private TableColumn<Ruta, String> colOrigen;

    @FXML
    private TableColumn<Ruta, Float> colPrecio;

    @FXML
    private Button btnCalculadora;

    @FXML
    private TableColumn<Parada, String> colTipoTransporte;

    @FXML
    private MenuItem itemListadoParada;

    @FXML
    private MenuItem itemListadoRuta;

    @FXML
    private ContextMenu MenuListados;


    public Principal() {
        //debe ir asi
    }

    @FXML
    void initialize() {
        btnRegistros.setOnAction(e -> {
            menuRegistros.show(btnRegistros, Side.BOTTOM, 0, 0);
        });
        btnListados.setOnAction(Event -> {
            MenuListados.show(btnListados, Side.BOTTOM, 0, 0);
        });


        colOrigen.setCellValueFactory(cellData -> {
            Ruta ruta = cellData.getValue();
            return new SimpleStringProperty(ruta.getOrigen().getNombre());
        });
        colDestino.setCellValueFactory(cellData -> {
            Ruta ruta = cellData.getValue();
            return new SimpleStringProperty(ruta.getDestino().getNombre());
        });
        colDistancia.setCellValueFactory(new PropertyValueFactory<>("distancia"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));

    }

    @FXML
    void crearParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Parada");
            Stage ownerStage = (Stage) btnRegistros.getScene().getWindow();
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

    @FXML
    void crearRuta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_RUTA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Ruta");
            Stage ownerStage = (Stage) btnRegistros.getScene().getWindow();
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
    public void cargarTablas(){
        TableParada.getItems().clear();
        TableParada.getItems().setAll(RedParada.getInstance().getLugar().values());
        TableParada.refresh();

        TableRuta.getItems().clear();
        List<Ruta> todasLasRutas = new ArrayList<>();

        for (LinkedList<Ruta> lista : RedParada.getInstance().getRutas().values()) {
            todasLasRutas.addAll(lista);
        }

        TableRuta.getItems().setAll(todasLasRutas);
        TableRuta.refresh();
    }
    @FXML
    void listarParada(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.LISTADO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Listado de Paradas");
            Stage ownerStage = (Stage) btnListados.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> cargarTablas());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void listarRutas(ActionEvent event) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.LISTADO_RUTA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Listado de Rutas");
            Stage ownerStage = (Stage) btnListados.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> cargarTablas());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

        @FXML
        void abrirCalculadora(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.NUEVA_CALCULADORA_V2));
                BorderPane root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Calculadora de Rutas - Vista Múltiple");
                Stage ownerStage = (Stage) btnCalculadora.getScene().getWindow();
                stage.initOwner(ownerStage);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
