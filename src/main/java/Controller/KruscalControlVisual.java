package Controller;

import Model.Parada;
import Model.RedParada;
import Model.Ruta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class KruscalControlVisual {

    @FXML private ComboBox<String> cmbCriterio;
    @FXML private Button btnCalcular;
    @FXML private AnchorPane graphPane;
    @FXML private Label lblCostoTotal;
    @FXML private Label lblDistanciaTotal;
    @FXML private Label lblRutasActivas;
    @FXML private ListView<String> lvRutasOptimas;

    private RedParada redParada;

    /** Inicializa el controlador y configura los elementos de la interfaz gráfica.
     */
    @FXML
    void initialize() {
        redParada = RedParada.getInstance();
        cmbCriterio.setItems(FXCollections.observableArrayList("Minimizar Costo", "Minimizar Distancia"));
        cmbCriterio.getSelectionModel().selectFirst();

        limpiarResultados();
        dibujarGrafoBase();
    }

    /** Calcula el árbol de expansión mínima utilizando el algoritmo de Kruskal y actualiza la interfaz gráfica con los resultados.
     */
    @FXML
    void calcularKruscal() {
        limpiarResultados();
        dibujarGrafoBase();

        boolean porCosto = cmbCriterio.getSelectionModel().getSelectedItem().equals("Minimizar Costo");


        List<Ruta> mst = redParada.calcularMstKruskal(porCosto);

        if (mst.isEmpty()) {
            lvRutasOptimas.getItems().add("No se pudo generar la red.");
            return;
        }

        // Actualizar panel de resultados
        double costoTotal = mst.stream().mapToDouble(Ruta::getCosto).sum();
        double distanciaTotal = mst.stream().mapToDouble(Ruta::getDistancia).sum();

        lblCostoTotal.setText(String.format("$ %.2f", costoTotal));
        lblDistanciaTotal.setText(String.format("%.2f km", distanciaTotal));
        lblRutasActivas.setText(String.format("%d / %d", mst.size(), redParada.getAllRutas().size()));

        List<String> rutasFormateadas = mst.stream()
                .map(r -> String.format("%s -> %s", r.getOrigen().getNombre(), r.getDestino().getNombre()))
                .collect(Collectors.toList());
        lvRutasOptimas.getItems().setAll(rutasFormateadas);

        // Resaltar las rutas del MST en el grafo
        resaltarRutasMst(mst);
    }

    /** Dibuja el grafo base con todas las rutas posibles y las paradas en el panel gráfico.
     */
    private void dibujarGrafoBase() {
        graphPane.getChildren().clear();

        // Dibujar todas las rutas posibles en gris como fondo
        for (Ruta ruta : redParada.getAllRutas()) {
            Parada origen = ruta.getOrigen();
            Parada destino = ruta.getDestino();
            Line linea = new Line(origen.getPosicionx(), origen.getPosiciony(), destino.getPosicionx(), destino.getPosiciony());
            linea.setStroke(Color.web("#4A5568", 0.5));
            linea.getStrokeDashArray().addAll(5d, 5d);
            graphPane.getChildren().add(linea);
        }

        // Dibujar las paradas
        for (Parada parada : redParada.getLugar().values()) {
            Circle circulo = new Circle(parada.getPosicionx(), parada.getPosiciony(), 8, Color.SLATEGRAY);
            circulo.setStroke(Color.WHITE);
            circulo.setStrokeWidth(1.5);
            Text texto = new Text(parada.getPosicionx() + 12, parada.getPosiciony() + 4, parada.getNombre());
            texto.setFill(Color.WHITE);
            texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            graphPane.getChildren().addAll(circulo, texto);
        }
    }

    /** Resalta las rutas que forman parte del árbol de expansión mínima en el panel gráfico.
     */
    private void resaltarRutasMst(List<Ruta> mst) {
        for (Ruta ruta : mst) {
            Parada origen = ruta.getOrigen();
            Parada destino = ruta.getDestino();
            Line linea = new Line(origen.getPosicionx(), origen.getPosiciony(), destino.getPosicionx(), destino.getPosiciony());
            linea.setStroke(Color.YELLOW);
            linea.setStrokeWidth(3.0);
            graphPane.getChildren().add(linea);
        }
    }

    private void limpiarResultados() {
        lblCostoTotal.setText("-- $");
        lblDistanciaTotal.setText("-- km");
        lblRutasActivas.setText("-- / --");
        lvRutasOptimas.getItems().clear();
    }
}