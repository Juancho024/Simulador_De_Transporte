package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.GrafoInfo;
import Model.Parada;
import Model.Ruta;
import Utilities.paths;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class Principal implements Initializable {

    // --- Nuevos componentes de la barra lateral ---
    @FXML private VBox sidebar;
    @FXML private Button btnCrearParada;
    @FXML private Button btnCrearRuta;
    @FXML private Button btnListadoParada;
    @FXML private Button btnListadoRuta;
    @FXML private Button btnCalcularRutas;
    @FXML private Button btnAnalisisInteractivo;
    @FXML private Button btnPlanificadorRed;

    // --- Componentes de las Tablas ---
//    @FXML private TableView<Parada> TableParada;
//    @FXML private TableView<Ruta> TableRuta;
//    @FXML private TableColumn<Ruta, String> colDestino;
//    @FXML private TableColumn<Ruta, Float> colDistancia;
//    @FXML private TableColumn<Parada, String> colNombre;
//    @FXML private TableColumn<Ruta, String> colOrigen;
//    @FXML private TableColumn<Ruta, Float> colPrecio;
//    @FXML private TableColumn<Parada, String> colTipoTransporte;
    @FXML
    private Pane paneGrafos;

    // Atributos para la animación
    private final double collapsedWidth = 60.0;
    private final double expandedWidth = 200.0;
    private final List<Button> sidebarButtons = new ArrayList<>();

    private SmartGraphPanel<String, GrafoInfo> graphView;
    private Graph<String, GrafoInfo> graph;
    MostrarGrafos aux = new MostrarGrafos();


    public Principal() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setupSidebarAnimation();

//        colOrigen.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrigen().getNombre()));
//        colDestino.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDestino().getNombre()));
//        colDistancia.setCellValueFactory(new PropertyValueFactory<>("distancia"));
//        colPrecio.setCellValueFactory(new PropertyValueFactory<>("costo"));
//        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
//        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));


//        cargarTablas();
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

    private void setupSidebarAnimation() {
        sidebarButtons.addAll(List.of(btnCrearParada, btnCrearRuta, btnListadoParada, btnListadoRuta, btnCalcularRutas, btnAnalisisInteractivo, btnPlanificadorRed));;

        // Ocultar texto de los botones al inicio
        for (Button button : sidebarButtons) {
            button.setText("");
        }

        Timeline expandAnimation = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), expandedWidth)));
        Timeline collapseAnimation = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), collapsedWidth)));

        sidebar.setOnMouseEntered(event -> {
            expandAnimation.play();
            // Asignar texto a cada botón al expandir
            btnCrearParada.setText("Crear Parada");
            btnCrearRuta.setText("Crear Ruta");
            btnListadoParada.setText("Listado Paradas");
            btnListadoRuta.setText("Listado Rutas");
            btnCalcularRutas.setText("Calcular Rutas");
            btnAnalisisInteractivo.setText("Análisis Interactivo");
            btnPlanificadorRed.setText("Planificador de Red");
        });

//            En caso de error
//        if(btnAnalisisInteractivo != null) btnAnalisisInteractivo.setText("Análisis Interactivo");
//        if(btnPlanificadorRed != null) btnPlanificadorRed.setText("Planificador de Red");

        sidebar.setOnMouseExited(event -> {
            collapseAnimation.play();
            // Ocultar texto al colapsar
            for (Button button : sidebarButtons) {
                button.setText("");
            }
        });
    }


//    public void cargarTablas() {
//        TableParada.getItems().clear();
//        TableParada.getItems().setAll(ParadaDAO.getInstance().obtenerParadas().values());
//        TableParada.refresh();
//
//        TableRuta.getItems().clear();
//        List<Ruta> todasLasRutas = new ArrayList<>();
//        for (LinkedList<Ruta> lista : RutaDAO.getInstancia().obtenerRutas().values()) {
//            todasLasRutas.addAll(lista);
//        }
//        TableRuta.getItems().setAll(todasLasRutas);
//        TableRuta.refresh();
//    }

    @FXML
    void crearParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Parada");
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
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
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void listarParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.LISTADO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Listado de Paradas");
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
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
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void abrirAnalisisInteractivo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.FLOYD_CONTROL_VISUAL));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Análisis Interactivo de Rutas (Floyd-Warshall)");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            System.err.println("Error al abrir la vista de Análisis Interactivo:");
            e.printStackTrace();
        }
    }

    @FXML
    void abrirPlanificadorRed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.KRUSCAL_CONTROL_VISUAL));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Planificador de Red Óptima (MST)");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            System.err.println("Error al abrir la vista del Planificador de Red:");
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
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
//                cargarTablas();
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
