package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.Parada;
import Model.RedParada;
import Model.ResultadoRuta;
import Model.Ruta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculadoraRutas {

    // --- FXML: Controles Principales ---
    @FXML private ComboBox<String> cbOrigen;
    @FXML private ComboBox<String> cbDestino;
    @FXML private Button btnBuscar;
    @FXML private AnchorPane paneGrafo;

    // --- FXML: Paneles Intercambiables ---
    @FXML private VBox panelResultados; // Panel con las 4 tarjetas
    @FXML private VBox panelDetalles;   // Panel comparativo (izq/der)

    // --- FXML: Etiquetas Tarjetas (Resumen) ---
    @FXML private Label lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1, lbEvento1;
    @FXML private Label lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2, lbEvento2;
    @FXML private Label lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3, lbEvento3;
    @FXML private Label lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4, lbEvento4;

    // --- FXML: Panel Detalles (Columna Izquierda - Principal) ---
    @FXML private Label lblEstadoPrincipal;
    @FXML private Label lblDetCosto1, lblDetTiempo1, lblDetTransb1;
    @FXML private ListView<String> lvRutaPrincipal;

    // --- FXML: Panel Detalles (Columna Derecha - Alternativa) ---
    @FXML private Label lblEstadoSecundaria;
    @FXML private Label lblDetCosto2, lblDetTiempo2, lblDetTransb2;
    @FXML private ListView<String> lvRutaSecundaria;

    // --- Lógica y Modelo ---
    private RedParada redParada;
    private ResultadoRuta resEficiente, resCosto, resDistancia, resTiempo;

    // --- Visualización (Mapa) ---
    private Map<Long, NodeUI> uiNodes = new HashMap<>();
    private Group grupoRutas = new Group(); // Capa para dibujar líneas encima del mapa base

    // Definición de Colores
    private static final Color COLOR_NODO_NORMAL = Color.DODGERBLUE;
    private static final Color COLOR_NODO_SELECCIONADO = Color.LIMEGREEN;
    private static final Color COLOR_RUTA_MEJOR = Color.GOLD;      // Amarilla
    private static final Color COLOR_RUTA_SEGUNDA = Color.RED;     // Roja

    @FXML
    void initialize() {
        this.redParada = RedParada.getInstance();
        cargarComboBoxes();
        limpiarTodosLosPaneles();

        // Configurar mapa inicial
        paneGrafo.getChildren().add(grupoRutas);
        dibujarGrafoBase();

        // Listeners para cambiar color de nodos al seleccionar origen/destino
        cbOrigen.valueProperty().addListener((obs, oldVal, newVal) -> actualizarColoresNodos());
        cbDestino.valueProperty().addListener((obs, oldVal, newVal) -> actualizarColoresNodos());
    }

    // --------------------------------------------------------
    // MÉTODOS DE BÚSQUEDA (Lógica Principal)
    // --------------------------------------------------------

    @FXML
    void buscarRutas() {

        redParada.recargarGrafo();

        dibujarGrafoBase();
        String nombreOrigen = cbOrigen.getValue();
        String nombreDestino = cbDestino.getValue();

        if (nombreOrigen == null || nombreDestino == null || nombreOrigen.equals(nombreDestino)) {
            mostrarAlerta("Datos inválidos", "Seleccione un origen y un destino diferentes.");
            return;
        }

        Long origenId = buscarParadaIdPorNombre(nombreOrigen);
        Long destinoId = buscarParadaIdPorNombre(nombreDestino);

        // 1. Limpiar estado anterior
        grupoRutas.getChildren().clear();
        volverAResultados();

        // 2. Calcular las 4 variantes
        Map<String, ResultadoRuta> resultados = redParada.calcularTodasLasRutasConEvento(origenId, destinoId);

        // 3. Asignar resultados a las variables del controlador
        resEficiente = resultados.get("eficiente");
        resCosto = resultados.get("costo");
        resDistancia = resultados.get("distancia");
        resTiempo = resultados.get("tiempo");

        actualizarTarjetaResumen(resEficiente, lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1, lbEvento1);
        actualizarTarjetaResumen(resCosto, lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2, lbEvento2);
        actualizarTarjetaResumen(resDistancia, lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3, lbEvento3);
        actualizarTarjetaResumen(resTiempo, lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4, lbEvento4);

        // 3. Dibujar Inmediatamente la mejor ruta (Eficiente) en Amarillo
        dibujarRutaEnMapa(resEficiente, COLOR_RUTA_MEJOR, 4.0);
    }

    private void actualizarTarjetaResumen(ResultadoRuta res, Label c, Label d, Label t, Label tr, Label e) {
        if (res != null && res.esAlcanzable()) {
            c.setText(String.format("$%.2f", res.getCostoTotal()));
            d.setText(String.format("%.2f km", res.getDistanciaTotal()));
            t.setText(String.format("%.0f min", res.getTiempoTotal()));
            tr.setText(String.valueOf(res.getTransbordosTotales()));
            String evento  = res.getEvento();
            if(evento != null && !evento.equals("")){
                e.setText(" "+evento);
            } else {
                e.setText(" normal");
            }
        } else {
            String mensajeError = (res != null) ? res.getMensajeError() : "No hay Ruta disponible.";
            c.setText(mensajeError);
            d.setText("--"); t.setText("--"); tr.setText("--"); e.setText("--");
        }
    }

    // --------------------------------------------------------
    // MÉTODOS DE DETALLE (Vista Comparativa)
    // --------------------------------------------------------

    @FXML void verDetallesEficiente() { cargarVistaComparativa(resEficiente, "eficiente"); }
    @FXML void verDetallesCosto() { cargarVistaComparativa(resCosto, "costo"); }
    @FXML void verDetallesDistancia() { cargarVistaComparativa(resDistancia, "distancia"); }
    @FXML void verDetallesTiempo() { cargarVistaComparativa(resTiempo, "tiempo"); }

    private void cargarVistaComparativa(ResultadoRuta mejorRuta, String criterio) {
        if (mejorRuta == null || !mejorRuta.esAlcanzable()) return;

        // 1. Switchear Paneles (Ocultar resultados, mostrar detalles)
        panelResultados.setVisible(false);
        panelResultados.setManaged(false);
        panelDetalles.setVisible(true);
        panelDetalles.setManaged(true);

        // 2. Calcular la Segunda Mejor Ruta (Alternativa)
        Long idOrigen = buscarParadaIdPorNombre(cbOrigen.getValue());
        Long idDestino = buscarParadaIdPorNombre(cbDestino.getValue());
        ResultadoRuta segundaRuta = redParada.calcularSegundaMejorRuta(idOrigen, idDestino, criterio);

        // 3. Llenar Datos Visuales
        llenarColumnaDetalle(mejorRuta, lblDetCosto1, lblDetTiempo1, lblDetTransb1, lvRutaPrincipal);
        llenarColumnaDetalle(segundaRuta, lblDetCosto2, lblDetTiempo2, lblDetTransb2, lvRutaSecundaria);

        // Configurar Estados (Simulación)
        lblEstadoPrincipal.setText(" A TIEMPO");
        lblEstadoPrincipal.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");

        if (segundaRuta.esAlcanzable()) {
            lblEstadoSecundaria.setText(" POSIBLE RETRASO");
            lblEstadoSecundaria.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");
        } else {
            lblEstadoSecundaria.setText(" NO DISPONIBLE");
            lblEstadoSecundaria.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");
        }

        // 4. Dibujar en el Mapa (Comparación)
        grupoRutas.getChildren().clear();

        // Dibujamos primero la alternativa (roja) para que quede al fondo
        dibujarRutaEnMapa(segundaRuta, COLOR_RUTA_SEGUNDA, 2.5);

        // Dibujamos encima la principal (amarilla) para que resalte
        dibujarRutaEnMapa(mejorRuta, COLOR_RUTA_MEJOR, 4.0);
    }

    private void llenarColumnaDetalle(ResultadoRuta res, Label costo, Label tiempo, Label transb, ListView<String> lista) {
        if (res != null && res.esAlcanzable()) {
            costo.setText(String.format("$%.2f", res.getCostoTotal()));
            tiempo.setText(String.format("%.1f min", res.getTiempoTotal()));
            transb.setText(String.valueOf(res.getTransbordosTotales()));
            lista.getItems().setAll(res.getRuta());
        } else {
            costo.setText("--"); tiempo.setText("--"); transb.setText("--");
            lista.getItems().setAll("No disponible");
        }
    }

    @FXML
    void volverAResultados() {
        panelDetalles.setVisible(false);
        panelDetalles.setManaged(false);
        panelResultados.setVisible(true);
        panelResultados.setManaged(true);

        // Opcional: Al volver, limpiamos la roja y dejamos solo la amarilla
        if (resEficiente != null && resEficiente.esAlcanzable()) {
            grupoRutas.getChildren().clear();
            dibujarRutaEnMapa(resEficiente, COLOR_RUTA_MEJOR, 4.0);
        }
    }

    // --------------------------------------------------------

    private void dibujarGrafoBase() {
        paneGrafo.getChildren().clear();
        uiNodes.clear();

        // 1. Líneas Base (Gris)
        for (Ruta ruta : redParada.getAllRutas()) {
            Parada o = ruta.getOrigen();
            Parada d = ruta.getDestino();
            Line linea = new Line(o.getPosicionx(), o.getPosiciony(), d.getPosicionx(), d.getPosiciony());
            linea.setStroke(Color.GRAY);
            linea.setStrokeWidth(0.5);
            paneGrafo.getChildren().add(linea);
        }

        // 2. Nodos
        for (Parada p : redParada.getLugar().values()) {
            NodeUI nodo = new NodeUI(p);
            uiNodes.put(p.getId(), nodo);
            paneGrafo.getChildren().addAll(nodo.getCircle(), nodo.getLabel());
        }

        // 3. Capa de Rutas Dinámicas
        paneGrafo.getChildren().add(grupoRutas);
        grupoRutas.toBack();
    }

    private void dibujarRutaEnMapa(ResultadoRuta res, Color color, double grosor) {
        if (res == null || !res.esAlcanzable()) return;

        List<String> nodos = res.getRuta();
        for (int i = 0; i < nodos.size() - 1; i++) {
            Long id1 = buscarParadaIdPorNombre(nodos.get(i));
            Long id2 = buscarParadaIdPorNombre(nodos.get(i+1));

            if (id1 != null && id2 != null) {
                NodeUI n1 = uiNodes.get(id1);
                NodeUI n2 = uiNodes.get(id2);

                Line linea = new Line(n1.x, n1.y, n2.x, n2.y);
                linea.setStroke(color);
                linea.setStrokeWidth(grosor);
                linea.setOpacity(0.8);
                grupoRutas.getChildren().add(linea);
            }
        }
    }

    private void actualizarColoresNodos() {

        grupoRutas.getChildren().clear();

        limpiarTodosLosPaneles();

        volverAResultados();
        // ---------------------------

        uiNodes.values().forEach(n -> n.setColor(COLOR_NODO_NORMAL));

        Long idO = buscarParadaIdPorNombre(cbOrigen.getValue());
        if (idO != null && uiNodes.containsKey(idO)) uiNodes.get(idO).setColor(COLOR_NODO_SELECCIONADO);

        Long idD = buscarParadaIdPorNombre(cbDestino.getValue());
        if (idD != null && uiNodes.containsKey(idD)) uiNodes.get(idD).setColor(COLOR_NODO_SELECCIONADO);
    }

  //-------------------------------------------------------

    private void cargarComboBoxes() {
        HashMap<Long, Parada> paradas = ParadaDAO.getInstance().obtenerParadas();
        if (paradas != null && !paradas.isEmpty()) {
            List<String> nombres = paradas.values().stream()
                    .map(Parada::getNombre).sorted().collect(Collectors.toList());
            cbOrigen.setItems(FXCollections.observableArrayList(nombres));
            cbDestino.setItems(FXCollections.observableArrayList(nombres));
        }
    }

    private Long buscarParadaIdPorNombre(String nombre) {
        if (nombre == null) return null;
        return ParadaDAO.getInstance().obtenerParadas().values().stream()
                .filter(p -> nombre.equals(p.getNombre()))
                .map(Parada::getId).findFirst().orElse(null);
    }

    private void limpiarTodosLosPaneles() {
        Label[] labels = {lblCosto1, lblDistancia1, lblCosto2, lblDistancia2,
                lblCosto3, lblDistancia3, lblCosto4, lblDistancia4};
        for (Label l : labels) if(l != null) l.setText("--");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clase interna visual
    private static class NodeUI {
        Circle circle;
        Text label;
        double x, y;

        public NodeUI(Parada p) {
            this.x = p.getPosicionx();
            this.y = p.getPosiciony();
            this.circle = new Circle(x, y, 6, COLOR_NODO_NORMAL);
            this.circle.setStroke(Color.WHITE);
            this.circle.setStrokeWidth(1.5);

            this.label = new Text(x + 8, y - 5, p.getNombre());
            this.label.setFill(Color.WHITE);
            this.label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        }

        public Circle getCircle() { return circle; }
        public Text getLabel() { return label; }
        public void setColor(Color c) { this.circle.setFill(c); }
    }
}