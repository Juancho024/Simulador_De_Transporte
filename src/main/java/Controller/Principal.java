package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.GrafoInfo;
import Model.Parada;
import Model.Ruta;
import Utilities.FixedSmartGraph;
import Utilities.paths;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    @FXML
    private Pane paneGrafos;

    private SmartGraphPanel<String, GrafoInfo> graphView;
    private Graph<String, GrafoInfo> graph;
    MostrarGrafos aux = new MostrarGrafos();

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
        cargarTablas();

        // Preparar la inserción del SmartGraphPanel cuando la escena/ventana estén visibles.
        // Esto evita llamar graphView.init() antes de que SmartGraph haya creado sus vistas.
        paneGrafos.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        // Cuando la ventana esté mostrada, construir el grafo y llamar a init.
                        newWindow.showingProperty().addListener((obsShowing, wasShowing, isShowing) -> {
                            if (isShowing) {
                                Platform.runLater(() -> {
                                    aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
                                });
                            }
                        });
                    }
                });
            }
        });
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
            stage.setOnHidden(e -> {
                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
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
            stage.setOnHidden(e -> {
                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cargarTablas() {
        TableParada.getItems().clear();
        TableParada.getItems().setAll(ParadaDAO.getInstance().obtenerParadas().values());
        TableParada.refresh();

        TableRuta.getItems().clear();
        List<Ruta> todasLasRutas = new ArrayList<>();

        for (LinkedList<Ruta> lista : RutaDAO.getInstancia().obtenerRutas().values()) {
            todasLasRutas.addAll(lista);
        }

        TableRuta.getItems().setAll(todasLasRutas);
        TableRuta.refresh();
    }

    @FXML
    void listarParada(ActionEvent event) {
        try {
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
            stage.setOnHidden(e -> {
                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void listarRutas(ActionEvent event) {
        try {
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
            stage.setOnHidden(e -> {
                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (Exception e) {
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
            stage.setOnHidden(e -> {
                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
