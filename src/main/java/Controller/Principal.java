package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.Parada;
import Model.RedParada;
import Model.Ruta;
import Utilities.paths;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
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

    private SmartGraphPanel<String, String> graphView;
    private Graph<String, String> graph;

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
                                Platform.runLater(this::buildAndShowGraphInPane);
                            }
                        });
                    }
                });
            }
        });
    }
    /**
     * Construye el grafo usando tus DAOs y coloca el SmartGraphPanel dentro de paneGrafos.
     * Si ya existe un graphView previo lo reemplaza.
     */
    private void buildAndShowGraphInPane() {
        try {
            // 0) Eliminar vista anterior si existe
            if (graphView != null) {
                paneGrafos.getChildren().remove(graphView);
                graphView = null;
                graph = null;
            }

            // 1) Nuevo grafo
            graph = new GraphEdgeList<>();

            // Estructuras auxiliares
            Set<String> addedVertices = new HashSet<>();
            Map<String, Integer> pairCount = new HashMap<>(); // cuenta por par origen->destino
            int globalEdgeCounter = 0;

            // 2) Insertar Paradas (ParadaDAO)
            Collection<Parada> paradas = ParadaDAO.getInstance().obtenerParadas().values();
            for (Parada p : paradas) {
                if (p != null && p.getNombre() != null) {
                    String nombre = p.getNombre();
                    if (!addedVertices.contains(nombre)) {
                        graph.insertVertex(nombre);
                        addedVertices.add(nombre);
                    }
                }
            }

            // 3) Insertar aristas (RutaDAO)
            Map<?, LinkedList<Ruta>> rutasMap = RutaDAO.getInstancia().obtenerRutas();
            for (LinkedList<Ruta> lista : rutasMap.values()) {
                for (Ruta r : lista) {
                    try {
                        String origen = r.getOrigen() != null ? r.getOrigen().getNombre() : null;
                        String destino = r.getDestino() != null ? r.getDestino().getNombre() : null;
                        if (origen == null || destino == null) continue;

                        // Asegurar vértices en grafo
                        if (!addedVertices.contains(origen)) {
                            graph.insertVertex(origen);
                            addedVertices.add(origen);
                        }
                        if (!addedVertices.contains(destino)) {
                            graph.insertVertex(destino);
                            addedVertices.add(destino);
                        }

                        // Generar ID/etiqueta única para la arista pero mostrar la distancia
                        String pairKey = origen + "->" + destino;
                        int countForPair = pairCount.getOrDefault(pairKey, 0);
                        pairCount.put(pairKey, countForPair + 1);

                        // Mostrar distancia en la etiqueta pero añadir sufijo global para unicidad
                        String distanciaStr = String.valueOf(r.getDistancia());
                        String edgeElement = distanciaStr + (countForPair > 0 ? ("#" + countForPair) : "");
                        // Asegurar unicidad global con contador
                        edgeElement = edgeElement + "." + (globalEdgeCounter++);

                        // Insertar la arista (si por cualquier motivo hay duplicado capturamos la excepción y seguimos)
                        try {
                            graph.insertEdge(origen, destino, "KM : " + edgeElement);
                        } catch (com.brunomnsilva.smartgraph.graph.InvalidEdgeException iee) {
                            System.out.println("Aviso: arista duplicada omitida: " + edgeElement + " (" + origen + "->" + destino + ")");
                            // continuar con la siguiente arista
                        }

                    } catch (Exception inner) {
                        inner.printStackTrace();
                    }
                }
            }

            // 4) Crear SmartGraphPanel con layout
            SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
            graphView = new SmartGraphPanel<>(graph, initialPlacement);

            // 5) Ajuste de tamaño
            graphView.prefWidthProperty().bind(paneGrafos.widthProperty());
            graphView.prefHeightProperty().bind(paneGrafos.heightProperty());

            // 6) Cargar CSS: comprobación / logging para depuración
            URL cssUrl = getClass().getResource(paths.SMART_GRAPH);
            graphView.getStylesheets().add(cssUrl.toExternalForm());


            // 7) Añadir al pane (limpiando para evitar duplicados)
            paneGrafos.getChildren().clear();
            paneGrafos.getChildren().add(graphView);

            // 8) Inicializar la vista (debe hacerse después de que la Stage sea visible)
            Platform.runLater(() -> {
                try {
                    graphView.init();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    // Llama a este método pasando el id de la arista (ej. "e0")
//    private void highlightEdgeById(String edgeId) {
//        if (edgeId == null || graphView == null) return;
//
//        // Buscamos todas las etiquetas de arista y comparamos su texto
//        // SmartGraph usa la clase .smart-edge-label para los textos de arista
//        Set<Node> edgeLabels = graphView.lookupAll(".smart-edge-label");
//        for (Node labelNode : edgeLabels) {
//            if (!(labelNode instanceof Text)) continue;
//            Text t = (Text) labelNode;
//            if (edgeId.equals(t.getText())) {
//                // Encontramos la etiqueta; su ancestro directo suele ser el grupo que contiene la arista
//                Parent parent = t.getParent();
//                if (parent != null) {
//                    // Intentamos añadir la clase 'highlighted' al nodo que contiene la línea de la arista
//                    // Dependiendo de la versión de SmartGraph, esto puede necesitar adaptaciones.
//                    parent.getStyleClass().add("highlighted");
//                    // También añadir directamente a los hijos con clase .smart-edge
//                    for (Node child : parent.getChildrenUnmodifiable()) {
//                        if (child.getStyleClass().contains("smart-edge")) {
//                            child.getStyleClass().add("highlighted");
//                        }
//                    }
//                }
//                // Si quieres solo 1 coincidencia, puedes break aquí
//                // break;
//            }
//        }
//    }
//    // helper simple: comprobar existencia de vértice (GraphEdgeList no expone containsVertex directamente)
//    // Si tu Graph ofrece containsVertex usa ese; aquí iteramos para mayor compatibilidad.
//    private boolean containsVertex(Graph<String, String> g, String v) {
//        try {
//            // GraphEdgeList tiene iteradores; intentamos recorrer vertices vía toString de la API general.
//            // Si la implementación tiene otro método, reemplaza por g.containsVertex(v).
//            for (Vertex<String> vertex : g.vertices()) {
//                if (vertex.equals(v)) return true;
//            }
//        } catch (Exception ex) {
//            // fallback: intentar insertar y capturar excepción (no ideal)
//        }
//        return false;
//    }

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
