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
            int contador = 1;
            Map<?, LinkedList<Ruta>> rutasMap = RutaDAO.getInstancia().obtenerRutas();
            for (LinkedList<Ruta> lista : rutasMap.values()) {
                for (Ruta r : lista) {
                    try {
                        //Revisar que los datos esten guardados y el nombre
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

                        //Etiqueta de la distancia y ruta
                        String distanciaLabel = "Ruta #" + (contador++) + "\nDistancia: " + String.format("%.2f km", r.getDistancia());
                        String edgeId = String.valueOf(r.getId());
                        GrafoInfo ei = new GrafoInfo(edgeId, distanciaLabel);
                        graph.insertEdge(origen, destino, ei);
//                        try{
//                        } catch (com.brunomnsilva.smartgraph.graph.InvalidEdgeException iee) {
//                            System.out.println("Aviso: arista duplicada omitida (id): " + ei.getId());
//                        }

                    } catch (Exception inner) {
                        inner.printStackTrace();
                    }
                }
            }

            // Crear un mapa con las posiciones guardadas en las paradas
            Map<String, Point2D> posiciones = new HashMap<>();

            for (Parada p : paradas) {
                // Suponiendo que Parada tiene getX() y getY()
                posiciones.put(p.getNombre(), new Point2D(p.getPosicionx(), p.getPosiciony()));
            }

            // Usar la estrategia personalizada que usa coordenadas fijas
            SmartPlacementStrategy initialPlacement = new Utilities.SmartFixedPlacementStrategy(posiciones);
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
                    Platform.runLater(() -> {
                        try {
                            Platform.runLater(() -> {
                                aplicarIconosParadas(graphView);
                                FixedSmartGraph.lockNodes(graphView);

                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aplicarIconosParadas(SmartGraphPanel<String, GrafoInfo> graphView) {
        try {
            // Obtener todas las paradas
            Map<Long, Parada> mapaOriginal = ParadaDAO.getInstance().obtenerParadas();

            // Crear un mapa auxiliar por nombre
            Map<String, Parada> mapaPorNombre = new HashMap<>();
            for (Parada p : mapaOriginal.values()) {
                mapaPorNombre.put(p.getNombre(), p);
            }

            for (var vertexNode : graphView.getSmartVertices()) {
                String nombre = vertexNode.getUnderlyingVertex().element();
                Parada parada = mapaPorNombre.get(nombre);

                if (parada != null && parada.getIcono() != null) {
                    byte[] iconoBytes = parada.getIcono();
                    try {
                        Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));

                        // Obtener shape interno
                        var shapeField = vertexNode.getClass().getDeclaredField("shapeProxy");
                        shapeField.setAccessible(true);
                        Object shapeProxy = shapeField.get(vertexNode);

                        var getShape = shapeProxy.getClass().getDeclaredMethod("getShape");
                        getShape.setAccessible(true);
                        javafx.scene.shape.Shape shape = (javafx.scene.shape.Shape) getShape.invoke(shapeProxy);

                        shape.setFill(new ImagePattern(img));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                cargarTablas();              // Actualiza las tablas
                buildAndShowGraphInPane();   // Vuelve a construir y mostrar el grafo actualizado
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
                cargarTablas();              // Actualiza las tablas
                buildAndShowGraphInPane();   // Vuelve a construir y mostrar el grafo actualizado
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
                cargarTablas();              // Actualiza las tablas
                buildAndShowGraphInPane();   // Vuelve a construir y mostrar el grafo actualizado
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
                cargarTablas();              // Actualiza las tablas
                buildAndShowGraphInPane();   // Vuelve a construir y mostrar el grafo actualizado
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
                cargarTablas();              // Actualiza las tablas
                buildAndShowGraphInPane();   // Vuelve a construir y mostrar el grafo actualizado
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
