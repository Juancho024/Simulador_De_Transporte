package Utilities;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.geometry.Point2D;

import java.util.Map;

//Para poner en la posicion guardada
public class SmartFixedPlacementStrategy implements SmartPlacementStrategy {

    private final Map<String, Point2D> posiciones;

    public SmartFixedPlacementStrategy(Map<String, Point2D> posiciones) {
        this.posiciones = posiciones;
    }

    @Override
    public <V, E> void place(double width, double height, SmartGraphPanel<V, E> panel) {
        for (SmartGraphVertex<V> vertex : panel.getSmartVertices()) {
            String id = vertex.getUnderlyingVertex().element().toString();
            Point2D pos = posiciones.get(id);
            if (pos != null) {
                vertex.setPosition(pos.getX(), pos.getY());
            } else {
                // Si no hay coordenadas guardadas, posición aleatoria
                vertex.setPosition(Math.random() * width, Math.random() * height);
            }
        }
    }
}