package Controller;

import DataBase.ParadaDAO;
import DataBase.RutaDAO;
import Model.Parada;
import Utilities.paths;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class ListadoParada implements Initializable {

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnRegistrar;

    @FXML
    private TableColumn<Parada, String> colNombre;

    @FXML
    private TableColumn<Parada, String> colTipoTransporte;

    @FXML
    private ImageView imgFondo;

    @FXML
    private ImageView imgFondoMod;

    @FXML
    private Label lbUbicacion;

    @FXML
    private ComboBox<String> cbxUbicacion;

    @FXML
    private Label lbNombre;

    @FXML
    private Label lbTipoTransporte;

    @FXML
    private Pane paneModificacion;

    @FXML
    private Pane panePrincipal;

    @FXML
    private TableView<Parada> tableParada;

    @FXML
    private TextField txtBuscarParada;

    @FXML
    private ComboBox<String> cbxTipoTransporte;

    @FXML
    private TableColumn<Parada, String> colUbicacion;

    @FXML
    private TextField txtNombre;

    @FXML
    private Button btnIcono;

    byte[] iconoBytes;

    //Funcion para agregar icono
    @FXML
    void agregarIcono(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Icono");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Archivos de Imagen", Arrays.asList("*.png", "*.jpg", "*.jpeg"));

        fileChooser.getExtensionFilters().add(imageFilter);
        File file = fileChooser.showOpenDialog(btnIcono.getScene().getWindow());

        if (file != null) {
            try {
                //Se crear un byte[] para guardar la img
                iconoBytes = Files.readAllBytes(file.toPath());
                Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));
                imgFondoMod.setImage(img);

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Archivo");
                alert.setHeaderText("Error al cargar la imagen");
                alert.setContentText("No se pudo leer el archivo: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    //Funcion para actualizar las paradas
    @FXML
    void ActualizarParada(ActionEvent event) {
        int index = tableParada.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            //Validacion para evitar perdida de data
            if(iconoBytes == null){
                Alert alertIcono = new Alert(Alert.AlertType.ERROR);
                alertIcono.setTitle("Error de validación");
                alertIcono.setHeaderText("Faltan datos obligatorios");
                alertIcono.setContentText("Por favor, asegúrate de haber seleccionado un icono para la parada.");
                alertIcono.showAndWait();
                return;
            }
            if(cbxTipoTransporte.getValue() == null ||cbxUbicacion.getValue() == null || txtNombre.getText().isEmpty()){
                Alert alertCampos = new Alert(Alert.AlertType.ERROR);
                alertCampos.setTitle("Error de validación");
                alertCampos.setHeaderText("Faltan datos obligatorios");
                alertCampos.setContentText("Por favor, completa todos los campos obligatorios antes de actualizar la parada.");
                alertCampos.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar modificación");
            alert.setHeaderText("¿Estás seguro de que deseas modificar esta parada?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.setResizable(false);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Parada parada = tableParada.getItems().get(index);
                parada.setNombre(txtNombre.getText());
                parada.setTipoTransporte(cbxTipoTransporte.getValue());
                String mensaje = cbxUbicacion.getValue();
                Point2D posicion = colocarParadaByUbicacion(mensaje);
                parada.setPosiciony(posicion.getY());
                parada.setPosicionx(posicion.getX());
                parada.setIcono(iconoBytes);
                ParadaDAO.getInstance().actualizarParada(parada);
                tableParada.getItems().set(index, parada);
                tableParada.refresh();

                paneModificacion.setVisible(false);
                panePrincipal.setVisible(true);
                cargarCampos();
            }
        }
    }

    //Funcion para buscar cualquier informacion de la tabla
    @FXML
    void buscarParada(ActionEvent event) {
        String criterio = txtBuscarParada.getText().toLowerCase();
        List<Parada> paradasFiltradas = new LinkedList<>();

        for (Parada parada : ParadaDAO.getInstance().obtenerParadas().values()) {
            Point2D punto = new Point2D(parada.getPosicionx(), parada.getPosiciony());
            String nombreUbicacion = obtenerMensajeByCoordenada(punto);
            //Se convierte todos a string para buscar
            if (parada.getNombre().toLowerCase().contains(criterio) || //Buscar formar de evaluar sin acento
                    parada.getTipoTransporte().toLowerCase().contains(criterio) ||
                    (nombreUbicacion != null && nombreUbicacion.toLowerCase().contains(criterio))) {
                paradasFiltradas.add(parada);
            }
        }

        tableParada.getItems().setAll(paradasFiltradas);
        tableParada.refresh();
    }

    @FXML
    void cancelarModificacion(ActionEvent event) {
        paneModificacion.setVisible(false);
        panePrincipal.setVisible(true);
        limpiarCampos();
    }

    //Funcion para resetear todos los campos del registro
    private void limpiarCampos() {
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        lbNombre.setText("");
        lbTipoTransporte.setText("");
        lbUbicacion.setText("");
        imgFondo.setImage(null);
        imgFondoMod.setImage(null);
        if (!tableParada.getItems().isEmpty()) {
            tableParada.getSelectionModel().selectFirst();
            cargarCampos();
            btnModificar.setDisable(false);
            btnEliminar.setDisable(false);
        }
    }

    @FXML
    void eliminiarParada(ActionEvent event) {
        int index = tableParada.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Estás seguro de que deseas eliminar esta parada?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.setResizable(false);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            } else {
                Parada parada = tableParada.getItems().get(index);
                ParadaDAO.getInstance().eliminarParada(parada.getId()); //Eliminar de la base de datos
                RutaDAO.getInstancia().eliminarRutaByParada(parada.getId()); //Eliminar rutas asociadas a la parada
                cargarTablas();
                tableParada.refresh();
                limpiarCampos();
            }
        }
    }

    @FXML
    void realizarModificacion(ActionEvent event) {
        panePrincipal.setVisible(false);
        paneModificacion.setVisible(true);
    }

    private void cargarCamposMod() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        if (parada != null) {
            txtNombre.setText(parada.getNombre());
            cbxTipoTransporte.setValue(parada.getTipoTransporte());
            Point2D punto = new Point2D(parada.getPosicionx(), parada.getPosiciony());
            String mensaje = obtenerMensajeByCoordenada(punto);
            cbxUbicacion.setValue(mensaje);
            iconoBytes = parada.getIcono();
            Image img = new Image(new java.io.ByteArrayInputStream(parada.getIcono()));
            imgFondoMod.setImage(img);
        }
    }

    @FXML
    void registrarParada(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(paths.REGISTRO_PARADA));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Parada");
            Stage ownerStage = (Stage) btnRegistrar.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
            stage.setOnHidden(e -> cargarTablas());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { //Poner los botones de modificar desabilidatos hasta que se seleccione una parada
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);
        cbxTipoTransporte.getItems().addAll("Bus", "Tren", "Metro", "Tranvía", "Ferry");
        cbxUbicacion.getItems().addAll("Avenida Estrella Sadhala, Esquina PUCMM",
                "Avenida 27 de Febrero",
                "Autopista Juan Pablo Duarte",
                "Avenida Las Carreras",
                "Avenida Francia",
                "Avenida Salvador Estrella Sadhala",
                "Avenida Hispanoamericana",
                "Avenida Circunvalacion Norte",
                "Avenida Circunvalacion Sur",
                "Avenida Juan Pablo II",
                "Avenida Fernando Valerio",
                "Avenida Bartolome Colón",
                "Avenida Joaquin Balaguer",
                "Avenida Antonio Guzman",
                "Avenida Presidente Antonio Guzmán Fernandez",
                "Avenida Imbert",
                "Calle El Sol",
                "Calle Del Comercio",
                "Calle Republica de Argentina",
                "Calle Daniel Espinal",
                "Calle Maimon",
                "Calle Restauracion",
                "Calle 30 de Marzo",
                "Calle Fernando Bermudez",
                "Calle Cuba",
                "Calle Sabana Larga",
                "Calle Del Sol, Esquina 30 de Marzo",
                "Calle Del Sol, Esquina Restauracion",
                "Calle Juan Goico Alix",
                "Calle Pedro Francisco Bono",
                "Calle Manuel de Jesús Peña y Reynoso",
                "Calle Daniel Espinal, Esquina Francia",
                "Calle Juan Pablo Duarte",
                "Calle San Luis",
                "Calle Benito Moncion",
                "Calle Independencia",
                "Calle Sanchez",
                "Calle España",
                "Carretera Luperon",
                "Carretera Don Pedro",
                "Carretera Jacagua",
                "Carretera Santiago–Tamboril",
                "Carretera Gurabo",
                "Urbanizacion Villa Olga, Calle A",
                "Urbanizacion Villa Olga, Calle B",
                "Urbanizacion Jardines del Rey, Calle 1",
                "Urbanizacion Jardines del Rey, Calle 2",
                "Reparto Universitario, Calle Principal",
                "Reparto Consuelo, Calle 5",
                "Los Jardines Metropolitanos, Calle 8",
                "Los Jardines Metropolitanos, Calle 4",
                "Ensanche Libertad, Calle 2",
                "Ensanche Bolivar, Calle 3");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoTransporte.setCellValueFactory(new PropertyValueFactory<>("tipoTransporte"));
        colUbicacion.setCellValueFactory(cellData -> {
            Parada parada = cellData.getValue();
            Point2D punto = new Point2D(parada.getPosicionx(), parada.getPosiciony());
            String nombreUbicacion = obtenerMensajeByCoordenada(punto);
            return new javafx.beans.property.SimpleStringProperty(
                    nombreUbicacion != null ? nombreUbicacion : "Ubicación no conocida"
            );
        });

        cargarTablas();
        tableParada.setOnMouseClicked(ActionEvent -> {
            Parada parada = tableParada.getSelectionModel().getSelectedItem();
            if (parada != null) {
                cargarCampos();
                cargarCamposMod();
                btnModificar.setDisable(false);
                btnEliminar.setDisable(false);
            } else {
                btnModificar.setDisable(true);
                btnEliminar.setDisable(true);
            }
        });
        if (!tableParada.getItems().isEmpty()) {
            tableParada.getSelectionModel().selectFirst();
            cargarCampos();
            btnModificar.setDisable(false);
            btnEliminar.setDisable(false);
        }
    }

    private void cargarTablas() {
        tableParada.getItems().clear();
        tableParada.getItems().setAll(ParadaDAO.getInstance().obtenerParadas().values());
        tableParada.refresh();
    }

    private void cargarCampos() {
        Parada parada = tableParada.getSelectionModel().getSelectedItem();
        lbNombre.setText(" " + parada.getNombre());
        lbTipoTransporte.setText(" " + parada.getTipoTransporte());
        Point2D posicion = new Point2D(parada.getPosicionx(), parada.getPosiciony());
        lbUbicacion.setText(" " + obtenerMensajeByCoordenada(posicion));
        byte[] iconoBytes = parada.getIcono();
        if (iconoBytes != null && iconoBytes.length > 0) {
            Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));
            imgFondo.setImage(img);
        } else {
            imgFondo.setImage(null);
        }
    }
    public String obtenerMensajeByCoordenada(Point2D punto) {
        String[] Rutas = {
                "Avenida Estrella Sadhala, Esquina PUCMM",
                "Avenida 27 de Febrero",
                "Autopista Juan Pablo Duarte",
                "Avenida Las Carreras",
                "Avenida Francia",
                "Avenida Salvador Estrella Sadhala",
                "Avenida Hispanoamericana",
                "Avenida Circunvalacion Norte",
                "Avenida Circunvalacion Sur",
                "Avenida Juan Pablo II",
                "Avenida Fernando Valerio",
                "Avenida Bartolome Colón",
                "Avenida Joaquin Balaguer",
                "Avenida Antonio Guzman",
                "Avenida Presidente Antonio Guzmán Fernandez",
                "Avenida Imbert",
                "Calle El Sol",
                "Calle Del Comercio",
                "Calle Republica de Argentina",
                "Calle Daniel Espinal",
                "Calle Maimon",
                "Calle Restauracion",
                "Calle 30 de Marzo",
                "Calle Fernando Bermudez",
                "Calle Cuba",
                "Calle Sabana Larga",
                "Calle Del Sol, Esquina 30 de Marzo",
                "Calle Del Sol, Esquina Restauracion",
                "Calle Juan Goico Alix",
                "Calle Pedro Francisco Bono",
                "Calle Manuel de Jesús Peña y Reynoso",
                "Calle Daniel Espinal, Esquina Francia",
                "Calle Juan Pablo Duarte",
                "Calle San Luis",
                "Calle Benito Moncion",
                "Calle Independencia",
                "Calle Sanchez",
                "Calle España",
                "Carretera Luperon",
                "Carretera Don Pedro",
                "Carretera Jacagua",
                "Carretera Santiago–Tamboril",
                "Carretera Gurabo",
                "Urbanizacion Villa Olga, Calle A",
                "Urbanizacion Villa Olga, Calle B",
                "Urbanizacion Jardines del Rey, Calle 1",
                "Urbanizacion Jardines del Rey, Calle 2",
                "Reparto Universitario, Calle Principal",
                "Reparto Consuelo, Calle 5",
                "Los Jardines Metropolitanos, Calle 8",
                "Los Jardines Metropolitanos, Calle 4",
                "Ensanche Libertad, Calle 2",
                "Ensanche Bolivar, Calle 3"
        };

        double[][] coordenada = {
                {120, 80},
                {450, 200},
                {300, 350},
                {700, 120},
                {850, 480},
                {600, 250},
                {90, 400},
                {780, 60},
                {500, 450},
                {350, 150},
                {820, 300},
                {640, 220},
                {100, 260},
                {420, 480},
                {760, 340},
                {200, 120},
                {880, 180},
                {520, 320},
                {250, 430},
                {150, 210},
                {330, 90},
                {450, 380},
                {720, 410},
                {540, 100},
                {290, 160},
                {860, 240},
                {400, 300},
                {100, 500},
                {500, 100},
                {550, 500},
                {100, 100},
                {850, 500},
                {875, 350},
                {550, 350},
                {90, 120},
                {840, 450},
                {140, 460},
                {450, 270},
                {20, 240},
                {880, 60},
                {300, 480},
                {740, 310},
                {580, 420},
                {200, 300},
                {460, 160},
                {330, 460},
                {810, 390},
                {150, 50},
                {690, 250},
                {500, 50},
                {400, 210}
        };

        // Buscar mensaje
        for (int i = 0; i < coordenada.length; i++) {
            if (punto.getX() == coordenada[i][0] && punto.getY() == coordenada[i][1]) {
                return Rutas[i];
            }
        }

        return null; // No encontrado
    }
    public Point2D colocarParadaByUbicacion(String mensaje){
        String[] Rutas = {"Avenida Estrella Sadhala, Esquina PUCMM",
                "Avenida 27 de Febrero",
                "Autopista Juan Pablo Duarte",
                "Avenida Las Carreras",
                "Avenida Francia",
                "Avenida Salvador Estrella Sadhala",
                "Avenida Hispanoamericana",
                "Avenida Circunvalacion Norte",
                "Avenida Circunvalacion Sur",
                "Avenida Juan Pablo II",
                "Avenida Fernando Valerio",
                "Avenida Bartolome Colón",
                "Avenida Joaquin Balaguer",
                "Avenida Antonio Guzman",
                "Avenida Presidente Antonio Guzmán Fernandez",
                "Avenida Imbert",
                "Calle El Sol",
                "Calle Del Comercio",
                "Calle Republica de Argentina",
                "Calle Daniel Espinal",
                "Calle Maimon",
                "Calle Restauracion",
                "Calle 30 de Marzo",
                "Calle Fernando Bermudez",
                "Calle Cuba",
                "Calle Sabana Larga",
                "Calle Del Sol, Esquina 30 de Marzo",
                "Calle Del Sol, Esquina Restauracion",
                "Calle Juan Goico Alix",
                "Calle Pedro Francisco Bono",
                "Calle Manuel de Jesús Peña y Reynoso",
                "Calle Daniel Espinal, Esquina Francia",
                "Calle Juan Pablo Duarte",
                "Calle San Luis",
                "Calle Benito Moncion",
                "Calle Independencia",
                "Calle Sanchez",
                "Calle España",
                "Carretera Luperon",
                "Carretera Don Pedro",
                "Carretera Jacagua",
                "Carretera Santiago–Tamboril",
                "Carretera Gurabo",
                "Urbanizacion Villa Olga, Calle A",
                "Urbanizacion Villa Olga, Calle B",
                "Urbanizacion Jardines del Rey, Calle 1",
                "Urbanizacion Jardines del Rey, Calle 2",
                "Reparto Universitario, Calle Principal",
                "Reparto Consuelo, Calle 5",
                "Los Jardines Metropolitanos, Calle 8",
                "Los Jardines Metropolitanos, Calle 4",
                "Ensanche Libertad, Calle 2",
                "Ensanche Bolivar, Calle 3"
        };
        // (x, y)
        double[][] coordenada = {
                {120, 80},
                {450, 200},
                {300, 350},
                {700, 120},
                {850, 480},
                {600, 250},
                {90, 400},
                {780, 60},
                {500, 450},
                {350, 150},
                {820, 300},
                {640, 220},
                {100, 260},
                {420, 480},
                {760, 340},
                {200, 120},
                {880, 180},
                {520, 320},
                {250, 430},
                {150, 210},
                {330, 90},
                {450, 380},
                {720, 410},
                {540, 100},
                {290, 160},
                {860, 240},
                {400, 300},
                {100, 500},
                {500, 100},
                {550, 500},
                {100, 100},
                {850, 500},
                {875, 350},
                {550, 350},
                {90, 120},
                {840, 450},
                {140, 460},
                {450, 270},
                {20, 240},
                {880, 60},
                {300, 480},
                {740, 310},
                {580, 420},
                {200, 300},
                {460, 160},
                {330, 460},
                {810, 390},
                {150, 50},
                {690, 250},
                {500, 50},
                {400, 210}
        };

        // Buscar la ruta
        for (int i = 0; i < Rutas.length; i++) {
            if (Rutas[i].equalsIgnoreCase(mensaje.trim())) {
                return new Point2D(coordenada[i][0], coordenada[i][1]);
            }
        }
        return null;
    }
}
