package Model;/*
 TransportGraphFX.java
 Demo de visualización de una red de transporte usando solo JavaFX.
 - Nodos: representados por Circle + Text (NodeView)
 - Aristas: representadas por Line con binding a las coordenadas de los nodos (EdgeView)
 - Interacciones: arrastrar nodos, zoom con rueda, paneo con botón medio o Alt+click-drag
 - Función para resaltar un camino dado (highlightPath)
*/
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.*;

public class TransportGraphFX extends Application {

    public void start(Stage stage) throws Exception {
        Graph<String, String> g = new GraphEdgeList<>();
// ... see Examples below

        g.insertVertex("Sirena");
        g.insertVertex("Bravo");

        g.insertEdge("Sirena", "Bravo", "SirenaBravo");


        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(g, initialPlacement);
        Scene scene = new Scene(graphView, 1024, 768);

        String css = getClass().getResource("../css/smartgraph.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFXGraph Visualization");
        stage.setScene(scene);
        stage.show();

//IMPORTANT! - Called after scene is displayed, so we can initialize the graph visualization
        graphView.init();
    }
    public static void main(String[] args) {
        launch(args);
    }
//    // Grupos para organizar capas: edges detrás, nodos encima
//    private Group edgeGroup = new Group();
//    private Group nodeGroup = new Group();
//    private Group content = new Group(); // contiene edgeGroup y nodeGroup
//
//    // Escala global (usada en zoom)
//    private double scale = 1.0;
//
//    // Variables auxiliares para paneo (arrastrar la vista)
//    private double mouseOldX, mouseOldY;
//
//    // Estructuras para almacenar vistas/objetos del grafo
//    private Map<String, NodeView> nodes = new HashMap<>();
//    private Map<String, EdgeView> edges = new HashMap<>();
//
//    @Override
//    public void start(Stage stage) {
//        // Añadimos las capas en orden correcto
//        content.getChildren().addAll(edgeGroup, nodeGroup);
//
//        // Escena con color de fondo claro
//        Scene scene = new Scene(new Group(content), 900, 600, Color.web("#f4f4f4"));
//
//        // Habilitamos zoom y paneo
//        enableZoom(scene);
//        enablePan(scene);
//
//        stage.setTitle("Transport Graph - JavaFX Demo");
//        stage.setScene(scene);
//        stage.show();
//
//        // --- Demo: crear nodos y aristas ---
//        addNode("A", 100, 100, "Estación A");
//        addNode("B", 300, 150, "Parada B");
//        addNode("C", 220, 300, "Parada C");
//        addNode("D", 500, 250, "Final D");
//
//        addEdge("e1", "A", "B");
//        addEdge("e2", "B", "C");
//        addEdge("e3", "A", "C");
//        addEdge("e4", "B", "D");
//
//        // Ejemplo de resaltado de ruta: A -> B -> D
//        highlightPath(Arrays.asList("A", "B", "D"));
//    }
//
//    // Añade un nodo con id, posición y etiqueta
//    private void addNode(String id, double x, double y, String label) {
//        if (nodes.containsKey(id)) return;
//        NodeView nv = new NodeView(id, x, y, label);
//        nodes.put(id, nv);
//        nodeGroup.getChildren().add(nv);
//    }
//
//    // Añade una arista entre nodos ya existentes (no dirigida en este ejemplo)
//    private void addEdge(String id, String fromId, String toId) {
//        if (edges.containsKey(id)) return;
//        NodeView a = nodes.get(fromId);
//        NodeView b = nodes.get(toId);
//        if (a == null || b == null) return;
//        EdgeView ev = new EdgeView(id, a, b);
//        edges.put(id, ev);
//        edgeGroup.getChildren().add(ev.line);
//    }
//
//    // Resalta las aristas que conectan la secuencia de nodos dada por 'path'
//    private void highlightPath(List<String> path) {
//        // Primero: restaurar estilo normal para todas las aristas
//        for (EdgeView ev : edges.values()) ev.setNormal();
//
//        if (path == null || path.size() < 2) return;
//
//        // Para cada par consecutivo de nodos en path buscamos la arista correspondiente
//        Set<String> toHighlight = new HashSet<>();
//        for (int i = 0; i < path.size() - 1; i++) {
//            String u = path.get(i), v = path.get(i + 1);
//            for (EdgeView ev : edges.values()) {
//                if ((ev.a.id.equals(u) && ev.b.id.equals(v)) || (ev.a.id.equals(v) && ev.b.id.equals(u))) {
//                    toHighlight.add(ev.id);
//                }
//            }
//        }
//        for (String id : toHighlight) {
//            EdgeView ev = edges.get(id);
//            if (ev != null) ev.setHighlighted();
//        }
//    }
//
//    // Habilita zoom con rueda (Scroll). Ajusta la propiedad 'scale' y aplica transformación al grupo 'content'
//    private void enableZoom(Scene scene) {
//        scene.addEventFilter(ScrollEvent.SCROLL, e -> {
//            final double delta = 1.2; // factor por paso de rueda
//            double scaleFactor = (e.getDeltaY() > 0) ? delta : 1 / delta;
//            scale *= scaleFactor;
//            // Limitar escala para evitar zoom excesivo
//            if (scale < 0.2) scale = 0.2;
//            if (scale > 5) scale = 5;
//            content.setScaleX(scale);
//            content.setScaleY(scale);
//            e.consume();
//        });
//    }
//
//    // Habilita paneo: mover toda la vista arrastrando con botón medio o Alt + click izquierdo
//    private void enablePan(Scene scene) {
//        scene.setOnMousePressed(e -> {
//            if (e.getButton() == MouseButton.MIDDLE || (e.isPrimaryButtonDown() && e.isAltDown())) {
//                mouseOldX = e.getSceneX();
//                mouseOldY = e.getSceneY();
//                scene.setCursor(Cursor.MOVE);
//            }
//        });
//        scene.setOnMouseDragged(e -> {
//            if (e.getButton() == MouseButton.MIDDLE || (e.isPrimaryButtonDown() && e.isAltDown())) {
//                double dx = e.getSceneX() - mouseOldX;
//                double dy = e.getSceneY() - mouseOldY;
//                content.setTranslateX(content.getTranslateX() + dx);
//                content.setTranslateY(content.getTranslateY() + dy);
//                mouseOldX = e.getSceneX();
//                mouseOldY = e.getSceneY();
//            }
//        });
//        scene.setOnMouseReleased(e -> scene.setCursor(Cursor.DEFAULT));
//    }
//
//    // --- Clases internas que renderizan nodos y aristas ---
//
//    // Vista de nodo: Circle + Text; permite arrastrar la posición del nodo.
//    class NodeView extends Group {
//        String id;
//        Circle circle;
//        Text label;
//        double dragStartX, dragStartY;
//
//        NodeView(String id, double x, double y, String labelText) {
//            this.id = id;
//            // Creamos un circle con centro en (x,y)
//            circle = new Circle(x, y, 12, Color.web("#2c7be5"));
//            circle.setStroke(Color.web("#0b5ed7"));
//            circle.setStrokeWidth(2);
//
//            // Texto descriptivo posicionado relativo al círculo mediante binding
//            label = new Text(labelText);
//            label.setFont(Font.font(12));
//            label.setFill(Color.web("#03396c"));
//            label.xProperty().bind(circle.centerXProperty().add(16));
//            label.yProperty().bind(circle.centerYProperty().add(4));
//
//            this.getChildren().addAll(circle, label);
//
//            // Manejadores de eventos para arrastrar el nodo
//            circle.setOnMousePressed(ev -> {
//                dragStartX = ev.getSceneX();
//                dragStartY = ev.getSceneY();
//                circle.setCursor(Cursor.CLOSED_HAND);
//                ev.consume();
//            });
//            circle.setOnMouseReleased(ev -> {
//                circle.setCursor(Cursor.DEFAULT);
//                ev.consume();
//            });
//            circle.setOnMouseDragged(ev -> {
//                double dx = ev.getSceneX() - dragStartX;
//                double dy = ev.getSceneY() - dragStartY;
//                // Convertimos el delta de pantalla a delta en coordenadas del contenido
//                double invScale = 1.0 / Math.max(scale, 1e-6);
//                double newX = circle.getCenterX() + dx * invScale;
//                double newY = circle.getCenterY() + dy * invScale;
//                circle.setCenterX(newX);
//                circle.setCenterY(newY);
//                dragStartX = ev.getSceneX();
//                dragStartY = ev.getSceneY();
//                ev.consume();
//            });
//
//            // Doble click muestra info por consola (puedes abrir un panel en su lugar)
//            circle.setOnMouseClicked(ev -> {
//                if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
//                    System.out.println("Info nodo " + id + ": " + labelText);
//                }
//            });
//        }
//
//        // Exponemos propiedades centro para que las líneas las puedan bindear
//        javafx.beans.property.DoubleProperty centerXProperty() { return circle.centerXProperty(); }
//        javafx.beans.property.DoubleProperty centerYProperty() { return circle.centerYProperty(); }
//    }
//
//    // Vista de arista: Line cuyos extremos están enlazados por binding a los centros de los nodos
//    class EdgeView {
//        String id;
//        NodeView a, b;
//        Line line;
//
//        EdgeView(String id, NodeView a, NodeView b) {
//            this.id = id;
//            this.a = a;
//            this.b = b;
//            line = new Line();
//            // Binding: la línea sigue la posición de los nodos incluso cuando se mueven
//            line.startXProperty().bind(a.centerXProperty());
//            line.startYProperty().bind(a.centerYProperty());
//            line.endXProperty().bind(b.centerXProperty());
//            line.endYProperty().bind(b.centerYProperty());
//            line.setStroke(Color.web("#999"));
//            line.setStrokeWidth(3);
//            line.setOpacity(0.85);
//        }
//
//        void setHighlighted() {
//            line.setStroke(Color.web("#ff6b6b"));
//            line.setStrokeWidth(5);
//            line.setOpacity(1.0);
//        }
//
//        void setNormal() {
//            line.setStroke(Color.web("#999"));
//            line.setStrokeWidth(3);
//            line.setOpacity(0.85);
//        }
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
}