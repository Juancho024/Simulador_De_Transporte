package Controller;

import Model.RedParada;
import Model.ResultadoRuta;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.stream.Collectors;

public class NuevaCalculadoraV2 {

    // --- Componentes FXML de la Interfaz ---

    // Controles de Selección
    @FXML private ComboBox<String> cbOrigen;
    @FXML private ComboBox<String> cbDestino;
    @FXML private Button btnBuscar;

    // Panel 1: Mejor Ruta (Dijkstra Eficiente)
    @FXML private Label lblCosto1;
    @FXML private Label lblDistancia1;
    @FXML private Label lblTiempo1;
    @FXML private Label lblTransbordos1;

    // Panel 2: Menor Costo (Bellman-Ford)
    @FXML private Label lblCosto2;
    @FXML private Label lblDistancia2;
    @FXML private Label lblTiempo2;
    @FXML private Label lblTransbordos2;

    // Panel 3: Menor Distancia (Dijkstra Distancia)
    @FXML private Label lblCosto3;
    @FXML private Label lblDistancia3;
    @FXML private Label lblTiempo3;
    @FXML private Label lblTransbordos3;

    // Panel 4: Menor Tiempo (Pendiente)
    @FXML private Label lblCosto4;
    @FXML private Label lblDistancia4;
    @FXML private Label lblTiempo4;
    @FXML private Label lblTransbordos4;

    // Panel para el grafo (si se usa en el futuro)
    @FXML private AnchorPane paneGrafo;

    private RedParada redParada;


    @FXML
    void initialize() {
        this.redParada = RedParada.getInstance();
        cargarComboBoxes();
        limpiarTodosLosPaneles();
    }


    private void cargarComboBoxes() {
        // Obtiene la lista de nombres de las paradas y la ordena alfabéticamente
        var nombresParadas = redParada.getLugar().keySet().stream().sorted().collect(Collectors.toList());
        cbOrigen.getItems().setAll(nombresParadas);
        cbDestino.getItems().setAll(nombresParadas);
    }

    @FXML
    void buscarRutas() {
        String origen = cbOrigen.getValue();
        String destino = cbDestino.getValue();


        if (origen == null || destino == null) {
            mostrarAlerta("Error de Selección", "Debe seleccionar una parada de origen y una de destino.");
            return;
        }
        if (origen.equals(destino)) {
            mostrarAlerta("Error de Selección", "La parada de origen no puede ser la misma que la de destino.");
            return;
        }

        // Limpiar los resultados anteriores
        limpiarTodosLosPaneles();


        ResultadoRuta resultadoEficiente = redParada.calcularRutaMasEficiente(origen, destino);
        actualizarPanel(resultadoEficiente, lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1);


        ResultadoRuta resultadoMenorCosto = redParada.calcularRutaMenorCosto(origen, destino);
        actualizarPanel(resultadoMenorCosto, lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2);


        ResultadoRuta resultadoMenorDistancia = redParada.calcularRutaMenorDistancia(origen, destino);
        actualizarPanel(resultadoMenorDistancia, lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3);


        ResultadoRuta resultadoMenorTiempo = redParada.calcularRutaMenorTiempo(origen, destino);
        actualizarPanel(resultadoMenorTiempo, lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4);
    }

    /**
     * @param resultado El objeto ResultadoRuta que contiene los datos.
     * @param costoLabel La etiqueta para mostrar el costo.
     * @param distanciaLabel La etiqueta para mostrar la distancia.
     * @param tiempoLabel La etiqueta para mostrar el tiempo.
     * @param transbordosLabel La etiqueta para mostrar los transbordos.
     */
    private void actualizarPanel(ResultadoRuta resultado, Label costoLabel, Label distanciaLabel, Label tiempoLabel, Label transbordosLabel) {
        if (resultado.esAlcanzable()) {
            costoLabel.setText(String.format("$%.2f", resultado.getCostoTotal()));
            distanciaLabel.setText(String.format("%.2f km", resultado.getDistanciaTotal()));
            tiempoLabel.setText(String.format("%.2f min", resultado.getTiempoTotal()));
            transbordosLabel.setText(String.valueOf(resultado.getTransbordosTotales()));
        } else {

            costoLabel.setText(resultado.getMensajeError());
            distanciaLabel.setText("--");
            tiempoLabel.setText("--");
            transbordosLabel.setText("--");
        }
    }

    /**
     * Restablece todas las etiquetas de resultados a su estado inicial.
     */
    private void limpiarTodosLosPaneles() {
        String valorPorDefecto = "--";
        // Panel 1
        lblCosto1.setText(valorPorDefecto);
        lblDistancia1.setText(valorPorDefecto);
        lblTiempo1.setText(valorPorDefecto);
        lblTransbordos1.setText(valorPorDefecto);
        // Panel 2
        lblCosto2.setText(valorPorDefecto);
        lblDistancia2.setText(valorPorDefecto);
        lblTiempo2.setText(valorPorDefecto);
        lblTransbordos2.setText(valorPorDefecto);
        // Panel 3
        lblCosto3.setText(valorPorDefecto);
        lblDistancia3.setText(valorPorDefecto);
        lblTiempo3.setText(valorPorDefecto);
        lblTransbordos3.setText(valorPorDefecto);
        // Panel 4
        lblCosto4.setText(valorPorDefecto);
        lblDistancia4.setText(valorPorDefecto);
        lblTiempo4.setText(valorPorDefecto);
        lblTransbordos4.setText(valorPorDefecto);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}