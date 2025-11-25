package Controller;

import DataBase.ParadaDAO;
import Model.Parada;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;

public class RegistroParada implements Initializable {
    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnIcono;

    @FXML
    private Button btnRegistrar;

    @FXML
    private ComboBox<String> cbxTipoTransporte;

    @FXML
    private Label lbPrincipal;

    @FXML
    private Label lbTelefono;

    @FXML
    private Label lbTelefono1;

    @FXML
    private Label lbTelefono11;

    @FXML
    private Label lbTelefono111;

    @FXML
    private Spinner<Double> spnLatitud;

    @FXML
    private Spinner<Double> spnLongitud;

    @FXML
    private TextField txtNombre;

    @FXML
    private ImageView ImgIcono;

    private byte[] iconoBytes;

    @FXML
    private ImageView imgIconoDefault;

    @FXML
    private ComboBox<String> cbxUbicacion;

    @FXML
    void Cancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    @FXML
    void agregarIcono(ActionEvent event) {
        imgIconoDefault.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Icono");
        fileChooser.setInitialDirectory(new File("C:/Users/esteb/Downloads"));

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Archivos de Imagen", Arrays.asList("*.png", "*.jpg", "*.jpeg"));

        fileChooser.getExtensionFilters().add(imageFilter);
        File file = fileChooser.showOpenDialog(btnIcono.getScene().getWindow());

        if (file != null) {
            try {
                iconoBytes = Files.readAllBytes(file.toPath());
                Image img = new Image(new java.io.ByteArrayInputStream(iconoBytes));
                ImgIcono.setImage(img);

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Archivo");
                alert.setHeaderText("Error al cargar la imagen");
                alert.setContentText("No se pudo leer el archivo: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    void registrarParada(ActionEvent event) throws IOException {
        String tipoTransporte = cbxTipoTransporte.getValue();
        String nombre = txtNombre.getText();
        String mensaje = cbxUbicacion.getValue();

        if(cbxTipoTransporte.getValue() == null|| txtNombre.getText().isEmpty() || cbxUbicacion.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de validación");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, complete todos los campos obligatorios.");
            alert.showAndWait();
            return;
        }
        if (iconoBytes == null) {
            iconoBytes = imageToBytes(imgIconoDefault.getImage());
        }
        Point2D coordenada = colocarParadaByUbicacion(mensaje);
        if(ParadaDAO.getInstance().validarParadaByCoordenada((int) coordenada.getX(), (int) coordenada.getY())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Existe una Parada en esa Ubicación");
            alert.setContentText("Por favor, elegir una nueva Ubicación.");
            alert.showAndWait();
            return;
        }

        try{
            Parada nuevaParada = new Parada(nombre, tipoTransporte, coordenada.getX(), coordenada.getY(), iconoBytes);
            ParadaDAO.getInstance().guardarParada(nuevaParada);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro Exitoso");
            alert.setHeaderText(null);
            alert.setContentText("La parada ha sido registrada exitosamente.");
            alert.showAndWait();
            limpiarCampos();
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al registrar");
            alert.setHeaderText(null);
            alert.setContentText("Ha ocurrido un error al registrar la parada: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void limpiarCampos() {
        cbxTipoTransporte.setValue(null);
        txtNombre.setText("");
        iconoBytes = null;
        ImgIcono.setImage(null);
        imgIconoDefault.setVisible(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarSpinnersandCombox();
        iconoBytes = null;
    }

    private void configurarSpinnersandCombox() {
        cbxTipoTransporte.getItems().addAll("Bus","Tren","Metro","Tranvía","Ferry");
        cbxUbicacion.getItems().addAll("Avenida Estrella Sadhala, Esquina PUCMM",
                "Avenida 27 de Febrero",
                "Autopista Juan Pablo Duarte",
                "Avenida Las Carreras",
                "Avenida Francia",
                "Avenida Salvador Estrella Sadhalá",
                "Avenida Hispanoamericana",
                "Avenida Circunvalación Norte",
                "Avenida Circunvalación Sur",
                "Avenida Juan Pablo II",
                "Avenida Fernando Valerio",
                "Avenida Bartolomé Colón",
                "Avenida Joaquín Balaguer",
                "Avenida Antonio Guzmán",
                "Avenida Presidente Antonio Guzmán Fernández",
                "Avenida Imbert",
                "Calle El Sol",
                "Calle Del Comercio",
                "Calle República de Argentina",
                "Calle Daniel Espinal",
                "Calle Maimón",
                "Calle Restauración",
                "Calle 30 de Marzo",
                "Calle Fernando Bermúdez",
                "Calle Cuba",
                "Calle Sabana Larga",
                "Calle Del Sol, Esquina 30 de Marzo",
                "Calle Del Sol, Esquina Restauración",
                "Calle Juan Goico Alix",
                "Calle Pedro Francisco Bonó",
                "Calle Manuel de Jesús Peña y Reynoso",
                "Calle Daniel Espinal, Esquina Francia",
                "Calle Juan Pablo Duarte",
                "Calle San Luis",
                "Calle Benito Monción",
                "Calle Independencia",
                "Calle Sánchez",
                "Calle España",
                "Carretera Luperón",
                "Carretera Don Pedro",
                "Carretera Jacagua",
                "Carretera Santiago–Tamboril",
                "Carretera Gurabo",
                "Urbanización Villa Olga, Calle A",
                "Urbanización Villa Olga, Calle B",
                "Urbanización Jardines del Rey, Calle 1",
                "Urbanización Jardines del Rey, Calle 2",
                "Reparto Universitario, Calle Principal",
                "Reparto Consuelo, Calle 5",
                "Los Jardines Metropolitanos, Calle 8",
                "Los Jardines Metropolitanos, Calle 4",
                "Ensanche Libertad, Calle 2",
                "Ensanche Bolívar, Calle 3");
    }
    public Point2D colocarParadaByUbicacion(String mensaje){
        String[] Rutas = {"Avenida Estrella Sadhala, Esquina PUCMM",
                "Avenida 27 de Febrero",
                "Autopista Juan Pablo Duarte",
                "Avenida Las Carreras",
                "Avenida Francia",
                "Avenida Salvador Estrella Sadhalá",
                "Avenida Hispanoamericana",
                "Avenida Circunvalación Norte",
                "Avenida Circunvalación Sur",
                "Avenida Juan Pablo II",
                "Avenida Fernando Valerio",
                "Avenida Bartolomé Colón",
                "Avenida Joaquín Balaguer",
                "Avenida Antonio Guzmán",
                "Avenida Presidente Antonio Guzmán Fernández",
                "Avenida Imbert",
                "Calle El Sol",
                "Calle Del Comercio",
                "Calle República de Argentina",
                "Calle Daniel Espinal",
                "Calle Maimón",
                "Calle Restauración",
                "Calle 30 de Marzo",
                "Calle Fernando Bermúdez",
                "Calle Cuba",
                "Calle Sabana Larga",
                "Calle Del Sol, Esquina 30 de Marzo",
                "Calle Del Sol, Esquina Restauración",
                "Calle Juan Goico Alix",
                "Calle Pedro Francisco Bonó",
                "Calle Manuel de Jesús Peña y Reynoso",
                "Calle Daniel Espinal, Esquina Francia",
                "Calle Juan Pablo Duarte",
                "Calle San Luis",
                "Calle Benito Monción",
                "Calle Independencia",
                "Calle Sánchez",
                "Calle España",
                "Carretera Luperón",
                "Carretera Don Pedro",
                "Carretera Jacagua",
                "Carretera Santiago–Tamboril",
                "Carretera Gurabo",
                "Urbanización Villa Olga, Calle A",
                "Urbanización Villa Olga, Calle B",
                "Urbanización Jardines del Rey, Calle 1",
                "Urbanización Jardines del Rey, Calle 2",
                "Reparto Universitario, Calle Principal",
                "Reparto Consuelo, Calle 5",
                "Los Jardines Metropolitanos, Calle 8",
                "Los Jardines Metropolitanos, Calle 4",
                "Ensanche Libertad, Calle 2",
                "Ensanche Bolívar, Calle 3"
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
                {110, 350},
                {780, 470},
                {630, 140},
                {260, 280},
                {350, 430},
                {720, 200},
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
    public static byte[] imageToBytes(Image image) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", output);
        return output.toByteArray();
    }
}
