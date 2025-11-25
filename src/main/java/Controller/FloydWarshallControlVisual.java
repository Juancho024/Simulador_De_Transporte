package Controller;

import Model.Parada;
import Model.RedParada;
import Model.ResultadoRuta;
import Model.Ruta;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class FloydWarshallControlVisual {

    @FXML private AnchorPane graphPane;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label lblOrigen;
    @FXML private Label lblDestino;
    @FXML private Label lblDistancia;
    @FXML private Label lblCosto;
    @FXML private ListView<String> lvCamino;
    @FXML private Label lblStatus;

    private RedParada redParada;
    private Map<Long, NodeUI> uiNodes = new HashMap<>();
    private Long origenSeleccionado = null;
    private Line rutaResaltada = null;

    private static final Color COLOR_NODO = Color.DODGERBLUE;
    private static final Color COLOR_ORIGEN = Color.LIMEGREEN;
    private static final Color COLOR_DESTINO = Color.TOMATO;
    private static final Color COLOR_RUTA = Color.YELLOW;

    private static final double PROBABILIDAD_EVENTO = 0.20; // 20% de probabilidad
    private static final double FACTOR_AUMENTO_COSTO = 1.30; // 30% + caro
    private static final double FACTOR_AUMENTO_DISTANCIA = 1.10; // 10% + distancia

    @FXML
    void initialize() {
        redParada = RedParada.getInstance();
        limpiarPanelResultados();


        Task<Void> calcularTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                redParada.calcularTodasLasRutasMasCortas();
                return null;
            }
        };


        calcularTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                dibujarGrafoCompleto();
                lblStatus.setText("Haga clic en una parada de ORIGEN.");
            });
        });


        new Thread(calcularTask).start();
    }

    private void dibujarGrafoCompleto() {
        graphPane.getChildren().clear();


        for (Ruta ruta : redParada.getAllRutas()) {
            Parada origen = ruta.getOrigen();
            Parada destino = ruta.getDestino();
            Line linea = new Line(origen.getPosicionx(), origen.getPosiciony(), destino.getPosicionx(), destino.getPosiciony());
            linea.setStroke(Color.GRAY);
            linea.setStrokeWidth(1.5);
            graphPane.getChildren().add(linea);
        }


        for (Parada parada : redParada.getLugar().values()) {
            NodeUI nodoUI = new NodeUI(parada);
            uiNodes.put(parada.getId(), nodoUI);
            graphPane.getChildren().addAll(nodoUI.getCircle(), nodoUI.getLabel());


            nodoUI.getCircle().setOnMouseClicked(event -> handleNodeClick(parada.getId()));
        }
    }

    private void handleNodeClick(Long paradaId) {
        if (origenSeleccionado == null) {

            origenSeleccionado = paradaId;

            // Resetear colores y limpiar ruta anterior
            resetAllNodeColors();
            if (rutaResaltada != null) graphPane.getChildren().remove(rutaResaltada);
            limpiarPanelResultados();

            // Resaltar origen
            uiNodes.get(paradaId).setColor(COLOR_ORIGEN);
            lblOrigen.setText(redParada.getLugar().get(paradaId).getNombre());
            lblStatus.setText("Haga clic en una parada de DESTINO.");

        } else {

            if (paradaId.equals(origenSeleccionado)) return;

            Long destinoSeleccionado = paradaId;
            uiNodes.get(destinoSeleccionado).setColor(COLOR_DESTINO);


            ResultadoRuta resultado = redParada.obtenerRutaFloydWarshall(origenSeleccionado, destinoSeleccionado);
            actualizarUIConResultado(resultado);

            // Resaltar la ruta en el grafo
            resaltarCaminoEnGrafo(resultado);

            // Resetear para la próxima consulta
            origenSeleccionado = null;
            lblStatus.setText("Ruta calculada. Haga clic en un nuevo origen.");
        }
    }

    private void actualizarUIConResultado(ResultadoRuta resultado) {
        if (resultado.esAlcanzable()) {
            float costo = (float) resultado.getCostoTotal();
            float distancia = (float) resultado.getDistanciaTotal();
            String evento = simularEventoAleatorio();
            if (evento != null && !evento.equalsIgnoreCase("normal")) {
                // Aplicar penalización
                costo *= FACTOR_AUMENTO_COSTO;
                distancia *= FACTOR_AUMENTO_DISTANCIA;
                mostrarAlerta("¡Alerta de Evento!", evento + "\nLos Costos de las Rutas serán penalizados.");
            }
            lblDestino.setText(resultado.getRuta().get(resultado.getRuta().size() - 1));
            lblDistancia.setText(String.format("%.2f km", distancia));
            lblCosto.setText(String.format("$%.2f", costo));
            lvCamino.getItems().setAll(resultado.getRuta());

        } else {
            lblDestino.setText("Inalcanzable");
            lblDistancia.setText("-- km");
            lblCosto.setText("-- $");
            lvCamino.getItems().clear();
            lvCamino.getItems().add(resultado.getMensajeError());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String simularEventoAleatorio() {
        if (Math.random() < PROBABILIDAD_EVENTO) {
            // Tipos de eventos posibles
            String[] eventos = {"Hubo un choque en la Ruta", "Se encuentra una Huelga", "Se estan realizando Obras"};
            int indice = (int) (Math.random() * eventos.length);
            return eventos[indice] + " (Ruta con Retraso)";
        }
        return null;
    }

    private void resaltarCaminoEnGrafo(ResultadoRuta resultado) {

        if (resultado.esAlcanzable() && !resultado.getRuta().isEmpty()) {
            Parada origen = redParada.getLugar().get(redParada.buscarIdPorNombre(resultado.getRuta().get(0)));
            Parada destino = redParada.getLugar().get(redParada.buscarIdPorNombre(resultado.getRuta().get(resultado.getRuta().size() - 1)));

            if (rutaResaltada != null) graphPane.getChildren().remove(rutaResaltada);

            rutaResaltada = new Line(origen.getPosicionx(), origen.getPosiciony(), destino.getPosicionx(), destino.getPosiciony());
            rutaResaltada.setStroke(COLOR_RUTA);
            rutaResaltada.setStrokeWidth(4.0);
            rutaResaltada.toBack();
            graphPane.getChildren().add(rutaResaltada);
        }
    }

    private void limpiarPanelResultados() {
        lblOrigen.setText("--");
        lblDestino.setText("--");
        lblDistancia.setText("-- km");
        lblCosto.setText("-- $");
        lvCamino.getItems().clear();
    }

    private void resetAllNodeColors() {
        for (NodeUI node : uiNodes.values()) {
            node.setColor(COLOR_NODO);
        }
    }

//      Clase interna para agrupar un Círculo y un Label por cada Parada.

    private static class NodeUI {
        private Circle circle;
        private Text label;

        public NodeUI(Parada parada) {
            this.circle = new Circle(parada.getPosicionx(), parada.getPosiciony(), 10, COLOR_NODO);
            this.circle.setStroke(Color.WHITE);
            this.circle.setStrokeWidth(2);

            this.label = new Text(parada.getPosicionx() + 15, parada.getPosiciony() + 5, parada.getNombre());
            this.label.setFill(Color.WHITE);
            this.label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        }

        public Circle getCircle() { return circle; }
        public Text getLabel() { return label; }
        public void setColor(Color color) { this.circle.setFill(color); }
    }
}