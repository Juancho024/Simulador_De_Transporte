package Utilities;

import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertexNode;
import javafx.application.Platform;

public class FixedSmartGraph {

    public static void lockNodes(SmartGraphPanel<?, ?> graphView) {
        Platform.runLater(() -> {
            for (Object vertexObj : graphView.getSmartVertices()) {
                SmartGraphVertexNode<?> node = (SmartGraphVertexNode<?>) vertexObj;

                // Bloquear cualquier acción de mouse (click, arrastre, hover)
                node.setOnMousePressed(e -> e.consume());
                node.setOnMouseDragged(e -> e.consume());
                node.setOnMouseReleased(e -> e.consume());
                node.setOnMouseEntered(e -> e.consume());
                node.setOnMouseExited(e -> e.consume());
            }
        });
    }
}
