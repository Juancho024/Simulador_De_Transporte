package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.GrafoInfo;
import Model.Parada;
import Model.Ruta;
import Utilities.FixedSmartGraph;
import Utilities.paths;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;

import java.net.URL;
import java.util.*;

public class MostrarGrafos {

    public MostrarGrafos() {

    }

    public void buildAndShowGraphInPane(Pane paneGrafos, SmartGraphPanel<String, GrafoInfo> graphView, Graph<String, GrafoInfo> graph) {
        try {
            // 0) Eliminar vista anterior si existe
            if (graphView != null) {
                paneGrafos.getChildren().remove(graphView);
                graphView = null;
                graph = null;
            }

            // 1) Nuevo grafo
            graph = new com.brunomnsilva.smartgraph.graph.DigraphEdgeList<>();

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

                    } catch (Exception inner) {
                        inner.printStackTrace();
                    }
                }
            }

            // Crear un mapa con las posiciones guardadas en las paradas
            Map<String, Point2D> posiciones = new HashMap<>();

            for (Parada p : paradas) {
                posiciones.put(p.getNombre(), new Point2D(p.getPosicionx(), p.getPosiciony()));
            }

            // Usar la estrategia personalizada que usa coordenadas fijas
            SmartPlacementStrategy initialPlacement = new Utilities.SmartFixedPlacementStrategy(posiciones);
            graphView = new SmartGraphPanel<>(graph, initialPlacement);

            // 5) Ajuste de tamano
            graphView.prefWidthProperty().bind(paneGrafos.widthProperty());
            graphView.prefHeightProperty().bind(paneGrafos.heightProperty());

            // 6) Cargar CSS: comprobacion
            URL cssUrl = getClass().getResource(paths.SMART_GRAPH);
            if(cssUrl != null){
                graphView.getStylesheets().add(cssUrl.toExternalForm());
            }


            // 7) Añadir al pane (limpiando para evitar duplicados)
            paneGrafos.getChildren().clear();
            paneGrafos.getChildren().add(graphView);

            // 8) Inicializar la vista (debe hacerse después de que la Stage sea visible)
            SmartGraphPanel<String, GrafoInfo> finalGraphView = graphView;
            Platform.runLater(() -> {
                try {
                    finalGraphView.init();
                    Platform.runLater(() -> {
                        try {
                            Platform.runLater(() -> {
                                aplicarIconosParadas(finalGraphView);
                                FixedSmartGraph.lockNodes(finalGraphView);

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

    //Funcion para resaltar ruta
    public void resaltarRuta(SmartGraphPanel<String, GrafoInfo> graphView, List<Ruta> rutaEncontrada) {

        System.out.printf("Hola");
        if (graphView == null || rutaEncontrada == null) {
            return;
        }

        graphView.getSmartVertices().forEach(v -> v.removeStyleClass("ruta-resaltada"));
        graphView.getSmartEdges().forEach(e -> e.removeStyleClass("ruta-resaltada"));

        Set<String> verticesAResaltar = new HashSet<>();

        for (Ruta r : rutaEncontrada) {
            verticesAResaltar.add(r.getOrigen().getNombre());
            verticesAResaltar.add(r.getDestino().getNombre());
        }

        graphView.getSmartVertices().forEach(v -> {
            String nombreParada = v.getUnderlyingVertex().element();
            if (verticesAResaltar.contains(nombreParada)) {
                v.addStyleClass("ruta-resaltada");
            }
        });

        graphView.getSmartEdges().forEach(e -> {
            Vertex<String>[] vertices = e.getUnderlyingEdge().vertices();

            String v1 = vertices[0].element();
            String v2 = vertices[1].element();

            boolean debeResaltarse = false;

            for (Ruta r : rutaEncontrada) {
                String origen = r.getOrigen().getNombre();
                String destino = r.getDestino().getNombre();

                if ((v1.equals(origen) && v2.equals(destino)) || (v2.equals(origen) && v1.equals(destino))) {
                    debeResaltarse = true;
                    break;
                }
            }

            if (debeResaltarse) {
                e.addStyleClass("ruta-resaltada");
            }
        });
        System.out.printf("Hola2");
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

}
