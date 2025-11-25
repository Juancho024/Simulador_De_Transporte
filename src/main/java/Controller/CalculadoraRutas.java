package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.*;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CalculadoraRutas {

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

    @FXML private AnchorPane paneGrafo;
    @FXML private Label lbEvento1;
    @FXML private Label lbEvento2;
    @FXML private Label lbEvento3;
    @FXML private Label lbEvento4;

    private RedParada redParada;
    private static final double PROBABILIDAD_EVENTO = 0.20; // 20% de probabilidad
    private static final double FACTOR_AUMENTO_COSTO = 1.30; // 30% + caro
    private static final double FACTOR_AUMENTO_TIEMPO = 1.50; // 50% + tiempo
    private static final double FACTOR_AUMENTO_DISTANCIA = 1.10; // 10% + distancia

    private SmartGraphPanel<String, GrafoInfo> graphView;
    private Graph<String, GrafoInfo> graph;
    MostrarGrafos aux = new MostrarGrafos();

    @FXML
    void initialize() {
        this.redParada = RedParada.getInstance();
        cargarComboBoxes();
        limpiarTodosLosPaneles();

        //Mostrar grafos
//        paneGrafo.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
//            if (newScene != null) {
//                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
//                    if (newWindow != null) {
//                        // Cuando la ventana esté mostrada, construir el grafo y llamar a init.
//                        newWindow.showingProperty().addListener((obsShowing, wasShowing, isShowing) -> {
//                            if (isShowing) {
//                                Platform.runLater(() -> {
//                                    aux.buildAndShowGraphInPane(paneGrafo, graphView, graph);
//                                });
//                            }
//                        });
//                    }
//                });
//            }
//        });

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

    private void cargarComboBoxes() {
        // En caso de error de BBDD, se previene un NullPointerException
        HashMap<Long, Parada> paradas = ParadaDAO.getInstance().obtenerParadas();
        if (paradas != null && !paradas.isEmpty()) {
            List<String> nombresParadas = paradas.values().stream()
                    .map(Parada::getNombre)
                    .sorted()
                    .collect(Collectors.toList());

            cbDestino.setItems(FXCollections.observableArrayList(nombresParadas));
            cbOrigen.setItems(FXCollections.observableArrayList(nombresParadas));
        } else {
            // Manejo de caso donde no se cargan paradas
            cbDestino.setItems(FXCollections.observableArrayList("No hay paradas"));
            cbOrigen.setItems(FXCollections.observableArrayList("No hay paradas"));
            mostrarAlerta("Error de Carga", "No se pudieron cargar las paradas desde la base de datos.");
        }
    }

    @FXML
    void buscarRutas() {
        String nombreOrigen = cbOrigen.getValue();
        String nombreDestino = cbDestino.getValue();

        if (nombreOrigen == null || nombreDestino == null) {
            mostrarAlerta("Error de Selección", "Debe seleccionar una parada de origen y una de destino.");
            return;
        }

        Long origenId = buscarParadaIdPorNombre(nombreOrigen);
        Long destinoId = buscarParadaIdPorNombre(nombreDestino);

        if (origenId == null || destinoId == null) {
            mostrarAlerta("Error Interno", "No se pudo encontrar el ID para la parada seleccionada.");
            return;
        }

        if (origenId.equals(destinoId)) {
            mostrarAlerta("Error de Selección", "La parada de origen no puede ser la misma que la de destino.");
            return;
        }

        limpiarTodosLosPaneles();
        ResultadoRuta resultadoEficiente = redParada.calcularRutaMasEficiente(origenId, destinoId);
        ResultadoRuta resultadoMenorCosto = redParada.calcularRutaMenorCosto(origenId, destinoId);
        ResultadoRuta resultadoMenorDistancia = redParada.calcularRutaMenorDistancia(origenId, destinoId);
        ResultadoRuta resultadoMenorTiempo = redParada.calcularRutaMenorTiempo(origenId, destinoId);

        //Logico evento
        String evento = null;
        boolean algunaRutaAlcanzable = (resultadoEficiente != null && resultadoEficiente.esAlcanzable()) ||
                (resultadoMenorCosto != null && resultadoMenorCosto.esAlcanzable()) ||
                (resultadoMenorDistancia != null && resultadoMenorDistancia.esAlcanzable()) ||
                (resultadoMenorTiempo != null && resultadoMenorTiempo.esAlcanzable());

        if(algunaRutaAlcanzable) {
            evento = simularEventoAleatorio();
            if (evento != null) {
                if(evento.contains("Choque")){
                    lbEvento1.setText("Choque");
                    lbEvento2.setText("Choque");
                    lbEvento3.setText("Choque");
                    lbEvento4.setText("Choque");

                } else if(evento.contains("Huelga")){
                    lbEvento1.setText("Huelga");
                    lbEvento2.setText("Huelga");
                    lbEvento3.setText("Huelga");
                    lbEvento4.setText("Huelga");
                } else if(evento.contains("Obras")){
                    lbEvento1.setText("Obras");
                    lbEvento2.setText("Obras");
                    lbEvento3.setText("Obras");
                    lbEvento4.setText("Obras");
                }

                mostrarAlerta("¡Alerta de Evento!", evento + "\nLos Costos de las Rutas serán penalizados.");
            } else {
                lbEvento1.setText("Normal");
                lbEvento2.setText("Normal");
                lbEvento3.setText("Normal");
                lbEvento4.setText("Normal");
            }
        } else {
            lbEvento1.setText("--");
            lbEvento2.setText("--");
            lbEvento3.setText("--");
            lbEvento4.setText("--");
        }

        // Calcular y mostrar resultados para los 4 paneles
        actualizarPanel(resultadoEficiente, lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1, evento != null);
        actualizarPanel(resultadoMenorCosto, lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2, evento != null);
        actualizarPanel(resultadoMenorDistancia, lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3, evento != null);
        actualizarPanel(resultadoMenorTiempo, lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4, evento != null);

//        List<Ruta> rutaParaResaltar = redParada.obtenerRutaEficienteComoListaRuta(origenId, destinoId);
//
//        if (!rutaParaResaltar.isEmpty()) {
//            Platform.runLater(() -> {
//                aux.resaltarRuta(graphView, rutaParaResaltar);
//            });
//        }

    }

    private Long buscarParadaIdPorNombre(String nombre) {
        if (nombre == null) return null;

        HashMap<Long, Parada> paradas = ParadaDAO.getInstance().obtenerParadas();
        if (paradas == null) return null;

        return paradas.values().stream()
                .filter(p -> nombre.equals(p.getNombre()))
                .map(Parada::getId)
                .findFirst()
                .orElse(null);
    }

    private void actualizarPanel(ResultadoRuta resultado, Label costoLabel, Label distanciaLabel, Label tiempoLabel, Label transbordosLabel, boolean eventoOcurrido) {
        if (resultado != null && resultado.esAlcanzable()) {
            double costo = resultado.getCostoTotal();
            double distancia = resultado.getDistanciaTotal();
            double tiempo = resultado.getTiempoTotal();

            if (eventoOcurrido) {
                // Aplicar penalización
                costo *= FACTOR_AUMENTO_COSTO;
                distancia *= FACTOR_AUMENTO_DISTANCIA;
                tiempo *= FACTOR_AUMENTO_TIEMPO;
            }

            costoLabel.setText(String.format("$%.2f", costo));
            distanciaLabel.setText(String.format("%.2f km", distancia));
            tiempoLabel.setText(String.format("%.2f min", tiempo));
            transbordosLabel.setText(String.valueOf(resultado.getTransbordosTotales()));
        } else {
            String mensajeError = (resultado != null) ? resultado.getMensajeError() : "No calculada";
            costoLabel.setText(mensajeError);
            distanciaLabel.setText("--");
            tiempoLabel.setText("--");
            transbordosLabel.setText("--");
        }
    }

    private void limpiarTodosLosPaneles() {
        String valorPorDefecto = "--";

        // Panel 1
        lblCosto1.setText(valorPorDefecto);
        lblDistancia1.setText(valorPorDefecto);
        lblTiempo1.setText(valorPorDefecto);
        lblTransbordos1.setText(valorPorDefecto);
        lbEvento1.setText(valorPorDefecto);
        // Panel 2
        lblCosto2.setText(valorPorDefecto);
        lblDistancia2.setText(valorPorDefecto);
        lblTiempo2.setText(valorPorDefecto);
        lblTransbordos2.setText(valorPorDefecto);
        lbEvento2.setText(valorPorDefecto);
        // Panel 3
        lblCosto3.setText(valorPorDefecto);
        lblDistancia3.setText(valorPorDefecto);
        lblTiempo3.setText(valorPorDefecto);
        lblTransbordos3.setText(valorPorDefecto);
        lbEvento3.setText(valorPorDefecto);
        // Panel 4
        lblCosto4.setText(valorPorDefecto);
        lblDistancia4.setText(valorPorDefecto);
        lblTiempo4.setText(valorPorDefecto);
        lblTransbordos4.setText(valorPorDefecto);
        lbEvento4.setText(valorPorDefecto);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}