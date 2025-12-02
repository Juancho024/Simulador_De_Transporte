package Controller;

import DataBase.ParadaDAO;
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
    @FXML private VBox panelResultados;
    @FXML private VBox panelDetalles;

    // --- FXML: Etiquetas Tarjetas (Resumen) ---
    // AÑADIDOS lbEvento1, 2, 3, 4 que faltaban en tu código original
    @FXML private Label lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1, lbEvento1;
    @FXML private Label lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2, lbEvento2;
    @FXML private Label lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3, lbEvento3;
    @FXML private Label lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4, lbEvento4;

    // --- FXML: Panel Detalles ---
    @FXML private Label lblEstadoPrincipal;
    @FXML private Label lblDetCosto1, lblDetTiempo1, lblDetTransb1;
    @FXML private ListView<String> lvRutaPrincipal;

    @FXML private Label lblEstadoSecundaria;
    @FXML private Label lblDetCosto2, lblDetTiempo2, lblDetTransb2;
    @FXML private ListView<String> lvRutaSecundaria;

    // --- Lógica y Modelo ---
    private RedParada redParada;
    private ResultadoRuta resEficiente, resCosto, resDistancia, resTiempo;

    // --- Visualización (Mapa) ---
    private Map<Long, NodeUI> uiNodes = new HashMap<>();
    private Group grupoRutas = new Group();

    private static final Color COLOR_NODO_NORMAL = Color.DODGERBLUE;
    private static final Color COLOR_NODO_SELECCIONADO = Color.LIMEGREEN;
    private static final Color COLOR_RUTA_MEJOR = Color.GOLD;
    private static final Color COLOR_RUTA_SEGUNDA = Color.RED;

    @FXML
    void initialize() {
        this.redParada = RedParada.getInstance();
        cargarComboBoxes();
        limpiarTodosLosPaneles();

        paneGrafo.getChildren().add(grupoRutas);

        // DIBUJAR EL GRAFO AL INICIO
        // Importante: Aseguramos que tenemos datos frescos para dibujar
        redParada.recargarGrafo();
        dibujarGrafoBase();

        cbOrigen.valueProperty().addListener((obs, oldVal, newVal) -> actualizarColoresNodos());
        cbDestino.valueProperty().addListener((obs, oldVal, newVal) -> actualizarColoresNodos());
    }

    @FXML
    void buscarRutas() {
        String nombreOrigen = cbOrigen.getValue();
        String nombreDestino = cbDestino.getValue();

        if (nombreOrigen == null || nombreDestino == null || nombreOrigen.equals(nombreDestino)) {
            mostrarAlerta("Datos inválidos", "Seleccione un origen y un destino diferentes.");
            return;
        }

        // CORRECCIÓN CLAVE 1: Recargar el grafo antes de buscar
        // Esto sincroniza la memoria (Singleton) con la Base de Datos actualizada
        redParada.recargarGrafo();

        // Redibujar el grafo base por si hubo cambios en paradas/rutas nuevas
        dibujarGrafoBase();

        Long origenId = buscarParadaIdPorNombre(nombreOrigen);
        Long destinoId = buscarParadaIdPorNombre(nombreDestino);

        grupoRutas.getChildren().clear();
        volverAResultados();

        // Calcular las 4 variantes
        resEficiente = redParada.calcularRutaMasEficiente(origenId, destinoId);
        actualizarTarjetaResumen(resEficiente, lblCosto1, lblDistancia1, lblTiempo1, lblTransbordos1, lbEvento1);

        resCosto = redParada.calcularRutaMenorCosto(origenId, destinoId);
        actualizarTarjetaResumen(resCosto, lblCosto2, lblDistancia2, lblTiempo2, lblTransbordos2, lbEvento2);

        resDistancia = redParada.calcularRutaMenorDistancia(origenId, destinoId);
        actualizarTarjetaResumen(resDistancia, lblCosto3, lblDistancia3, lblTiempo3, lblTransbordos3, lbEvento3);

        resTiempo = redParada.calcularRutaMenorTiempo(origenId, destinoId);
        actualizarTarjetaResumen(resTiempo, lblCosto4, lblDistancia4, lblTiempo4, lblTransbordos4, lbEvento4);

        dibujarRutaEnMapa(resEficiente, COLOR_RUTA_MEJOR, 4.0);
    }

    // CORRECCIÓN CLAVE 2: Añadido el Label de evento a la actualización
    private void actualizarTarjetaResumen(ResultadoRuta res, Label c, Label d, Label t, Label tr, Label evt) {
        if (res != null && res.esAlcanzable()) {
            c.setText(String.format("$%.2f", res.getCostoTotal()));
            d.setText(String.format("%.2f km", res.getDistanciaTotal()));
            t.setText(String.format("%.0f min", res.getTiempoTotal()));
            tr.setText(String.valueOf(res.getTransbordosTotales()));

            // Lógica simple para mostrar evento si existe (asumiendo lógica futura)
            // Por ahora mostramos "Normal" si es alcanzable
            evt.setText("Normal");
        } else {
            c.setText("--"); d.setText("--"); t.setText("--"); tr.setText("--"); evt.setText("--");
        }
    }

    @FXML void verDetallesEficiente() { cargarVistaComparativa(resEficiente, "eficiente"); }
    @FXML void verDetallesCosto() { cargarVistaComparativa(resCosto, "costo"); }
    @FXML void verDetallesDistancia() { cargarVistaComparativa(resDistancia, "distancia"); }
    @FXML void verDetallesTiempo() { cargarVistaComparativa(resTiempo, "tiempo"); }

    private void cargarVistaComparativa(ResultadoRuta mejorRuta, String criterio) {
        if (mejorRuta == null || !mejorRuta.esAlcanzable()) return;

        panelResultados.setVisible(false);
        panelResultados.setManaged(false);
        panelDetalles.setVisible(true);
        panelDetalles.setManaged(true);

        Long idOrigen = buscarParadaIdPorNombre(cbOrigen.getValue());
        Long idDestino = buscarParadaIdPorNombre(cbDestino.getValue());
        ResultadoRuta segundaRuta = redParada.calcularSegundaMejorRuta(idOrigen, idDestino, criterio);

        llenarColumnaDetalle(mejorRuta, lblDetCosto1, lblDetTiempo1, lblDetTransb1, lvRutaPrincipal);
        llenarColumnaDetalle(segundaRuta, lblDetCosto2, lblDetTiempo2, lblDetTransb2, lvRutaSecundaria);

        // Estilos de estado
        lblEstadoPrincipal.setText(" A TIEMPO");
        lblEstadoPrincipal.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");

        if (segundaRuta.esAlcanzable()) {
            lblEstadoSecundaria.setText(" ALTERNATIVA");
            lblEstadoSecundaria.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");
        } else {
            lblEstadoSecundaria.setText(" NO DISPONIBLE");
            lblEstadoSecundaria.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3; -fx-background-radius: 3;");
        }

        grupoRutas.getChildren().clear();
        dibujarRutaEnMapa(segundaRuta, COLOR_RUTA_SEGUNDA, 2.5);
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

        if (resEficiente != null && resEficiente.esAlcanzable()) {
            grupoRutas.getChildren().clear();
            dibujarRutaEnMapa(resEficiente, COLOR_RUTA_MEJOR, 4.0);
        }
    }

    private void dibujarGrafoBase() {
        paneGrafo.getChildren().clear();
        uiNodes.clear();

        // CORRECCIÓN CLAVE 3: Usar datos de RedParada (que ya recargamos)
        // para asegurar consistencia visual con el cálculo
        for (Ruta ruta : redParada.getAllRutas()) {
            Parada o = ruta.getOrigen();
            Parada d = ruta.getDestino();
            // Aseguramos que existen las coordenadas (por si acaso)
            if(o != null && d != null) {
                Line linea = new Line(o.getPosicionx(), o.getPosiciony(), d.getPosicionx(), d.getPosiciony());
                linea.setStroke(Color.GRAY);
                linea.setStrokeWidth(0.5);
                paneGrafo.getChildren().add(linea);
            }
        }

        for (Parada p : redParada.getLugar().values()) {
            NodeUI nodo = new NodeUI(p);
            uiNodes.put(p.getId(), nodo);
            paneGrafo.getChildren().addAll(nodo.getCircle(), nodo.getLabel());
        }

        paneGrafo.getChildren().add(grupoRutas);
        grupoRutas.toBack();
    }

    private void dibujarRutaEnMapa(ResultadoRuta res, Color color, double grosor) {
        if (res == null || !res.esAlcanzable()) return;

        List<String> nodos = res.getRuta();
        for (int i = 0; i < nodos.size() - 1; i++) {
            Long id1 = buscarParadaIdPorNombre(nodos.get(i));
            Long id2 = buscarParadaIdPorNombre(nodos.get(i+1));

            if (id1 != null && id2 != null && uiNodes.containsKey(id1) && uiNodes.containsKey(id2)) {
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
        uiNodes.values().forEach(n -> n.setColor(COLOR_NODO_NORMAL));

        Long idO = buscarParadaIdPorNombre(cbOrigen.getValue());
        if (idO != null && uiNodes.containsKey(idO)) uiNodes.get(idO).setColor(COLOR_NODO_SELECCIONADO);

        Long idD = buscarParadaIdPorNombre(cbDestino.getValue());
        if (idD != null && uiNodes.containsKey(idD)) uiNodes.get(idD).setColor(COLOR_NODO_SELECCIONADO);
    }

    private void cargarComboBoxes() {
        // Obtenemos paradas frescas de DB
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
        // Buscamos en la caché de RedParada para asegurar consistencia con el algoritmo Dijkstra
        // (Ya que hicimos recargarGrafo antes)
        return redParada.buscarIdPorNombre(nombre);
    }

    private void limpiarTodosLosPaneles() {
        Label[] labels = {lblCosto1, lblDistancia1, lblCosto2, lblDistancia2,
                lblCosto3, lblDistancia3, lblCosto4, lblDistancia4,
                lbEvento1, lbEvento2, lbEvento3, lbEvento4}; // Agregados eventos
        for (Label l : labels) if(l != null) l.setText("--");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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