package Controller;


import DataBase.ParadaDAO;
import Model.GrafoInfo;
import Model.Parada;
import Utilities.paths;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private ImageView imgLogo;


    //# de estaciones
    @FXML private Label totalBus;
    @FXML private Label totalFerry;
    @FXML private Label totalMetro;
    @FXML private Label totalTranvia;
    @FXML private Label totalTren;

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
        calcularTotalEstaciones();
        setupSidebarAnimation();

        // Preparar la insercion del SmartGraphPanel cuando la escena/ventana estén visibles.
        // Esto evita llamar graphView.init() antes de que SmartGraph haya creado sus vistas.
        paneGrafos.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        // Cuando la ventana este mostrada, construir el grafo y llamar a init.
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


    /** Configura la animación de expansión y colapso de la barra lateral
     */
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

    @FXML
    void crearParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_PARADA));
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Parada");
            stage.getIcons().add(icon);
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
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
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.getIcons().add(icon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
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
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Listado de Paradas");
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.getIcons().add(icon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
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
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Listado de Rutas");
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.getIcons().add(icon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Abre la ventana del Análisis Interactivo de Rutas (Floyd-Warshall)
     */
    @FXML
    void abrirAnalisisInteractivo(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.FLOYD_CONTROL_VISUAL));
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Análisis Interactivo de Rutas (Floyd-Warshall)");
            stage.setScene(new Scene(root));
            stage.getIcons().add(icon);
            stage.setMaximized(true);
            stage.show();
            stage.setOnHidden(e -> {
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            System.err.println("Error al abrir la vista de Análisis Interactivo:");
            e.printStackTrace();
        }
    }


    /** Abre la ventana del Planificador de Red Óptima (Kruscal)
     */
    @FXML
    void abrirPlanificadorRed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.KRUSCAL_CONTROL_VISUAL));
            Image icon = new Image(getClass().getResourceAsStream("/iconos/principallogo.png"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Planificador de Red Óptima (MST)");
            stage.setScene(new Scene(root));
            stage.getIcons().add(icon);
            stage.setMaximized(true);
            stage.show();
            stage.setOnHidden(e -> {
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            System.err.println("Error al abrir la vista del Planificador de Red:");
            e.printStackTrace();
        }
    }

    /** Abre la ventana de la Calculadora de Rutas (Vista Múltiple)
     */
    @FXML
    void abrirCalculadora(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.NUEVA_CALCULADORA_V2));
            Image icon = new Image(getClass().getResourceAsStream("/iconos/LogoCuadrado.png"));
            BorderPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Calculadora de Rutas - Vista Múltiple");
            Stage ownerStage = (Stage) sidebar.getScene().getWindow();
            stage.getIcons().add(icon);
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> {
                aux.buildAndShowGraphInPane(paneGrafos, graphView, graph);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void calcularTotalEstaciones(){
        int totalMetro1 = 0, totalBus1 = 0, totalFerry1 = 0, totalTren1 = 0, totalTranvia1 = 0;
        for(Parada aux: ParadaDAO.getInstance().obtenerParadas().values()){
            if(aux.getTipoTransporte().contains("Metro")){
                totalMetro1++;
            } else if(aux.getTipoTransporte().contains("Bus")){
                totalBus1++;
            } else if(aux.getTipoTransporte().contains("Ferry")){
                totalFerry1++;
            } else if (aux.getTipoTransporte().contains("Tranvia")) {
                totalTranvia1++;
            } else if(aux.getTipoTransporte().contains("Tren")){
                totalTren1++;
            }
        }
        totalMetro.setText(" "+totalMetro1);
        totalBus.setText(" "+totalBus1);
        totalFerry.setText(" "+totalFerry1);
        totalTranvia.setText(" "+totalTranvia1);
        totalTren.setText(" "+totalTren1);
    }

}
