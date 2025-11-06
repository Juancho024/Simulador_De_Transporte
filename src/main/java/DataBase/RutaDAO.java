package DataBase;

import Model.Parada;
import Model.Ruta;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;

public class RutaDAO {
    private static RutaDAO instancia;
    private RutaDAO() {}

    public static RutaDAO getInstancia() {
        if (instancia == null) {
            instancia = new RutaDAO();
        }
        return instancia;
    }

    public void guardarRuta(Ruta ruta) {
        final String sql = "INSERT INTO ruta (origen_id, destino_id, distancia, tiempoRecorrido, costo, numTransbordo, \"posibleevento\") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, ruta.getOrigen().getId());
            preparedStatement.setLong(2, ruta.getDestino().getId());
            preparedStatement.setFloat(3, ruta.getDistancia());
            preparedStatement.setFloat(4, ruta.getTiempoRecorrido());
            preparedStatement.setFloat(5, ruta.getCosto());
            preparedStatement.setInt(6, ruta.getNumTransbordos());
            preparedStatement.setString(7, ruta.getPosibleEvento());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public HashMap<Long, LinkedList<Ruta>> obtenerRutas(){
        HashMap<Long, LinkedList<Ruta>> rutas = new HashMap<>();
        final String sql = "SELECT * FROM ruta";

        try(Connection connection = DataBaseConnection.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()){
                Parada origen = obtenerParadaPorId(resultSet.getLong("origen_id"));
                Parada destino = obtenerParadaPorId(resultSet.getLong("destino_id"));
                float distancia = resultSet.getFloat("distancia");
                float tiempoRecorrido = resultSet.getFloat("tiempoRecorrido");
                float costo = resultSet.getFloat("costo");
                int numTransbordos = resultSet.getInt("numTransbordo");
                String posibleEvento = resultSet.getString("posibleEvento");

                Ruta ruta = new Ruta(origen, destino, distancia, tiempoRecorrido, costo, numTransbordos, posibleEvento);
                ruta.setId(resultSet.getLong("id"));

                // Usar el ID del Origen (Parada) como clave del mapa
                Long idOrigenParada = origen.getId(); // o resultSet.getLong("origen_id");

                // Si la clave (ID de Origen) no existe, crea una nueva lista. Luego, añade la ruta a esa lista.
                rutas.computeIfAbsent(idOrigenParada, k -> new LinkedList<>()).add(ruta);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return rutas;
    }
    public void actualizarRuta(Ruta ruta){
        final String sql = "UPDATE ruta SET origen_id = ?, destino_id = ?, distancia = ?, tiempoRecorrido = ?, costo = ?, numTransbordo = ?, \"posibleevento\" = ? WHERE id = ?";
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, ruta.getOrigen().getId());
            preparedStatement.setLong(2, ruta.getDestino().getId());
            preparedStatement.setFloat(3, ruta.getDistancia());
            preparedStatement.setFloat(4, ruta.getTiempoRecorrido());
            preparedStatement.setFloat(5, ruta.getCosto());
            preparedStatement.setInt(6, ruta.getNumTransbordos());
            preparedStatement.setString(7, ruta.getPosibleEvento());
            preparedStatement.setLong(8, ruta.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void eliminarRuta(Long id){
        final String sql = "DELETE FROM ruta WHERE id = ?";
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    private Parada obtenerParadaPorId(long id) {
        final String sql = "SELECT * FROM parada WHERE id = ?";
        Parada parada = null;

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String nombre = resultSet.getString("nombre");
                 String tipoTransporte = resultSet.getString("tipoTransporte");
                 int posiciony = resultSet.getInt("posiciony");
                 int posicionx = resultSet.getInt("posicionx");
                 byte[] icono = resultSet.getBytes("icono");
                 parada = new Parada(nombre, tipoTransporte, posicionx, posiciony, icono);
                 parada.setId(resultSet.getLong("id"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return parada;
    }

    public Parada buscarParadaPorNombre(String name) {
        final String sql = "SELECT * FROM parada WHERE nombre = ?";
        Parada aux = null;
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String nombre = resultSet.getString("nombre");
                String tipoTransporte = resultSet.getString("tipoTransporte");
                int posicionx = resultSet.getInt("posicionx");
                int posiciony = resultSet.getInt("posiciony");
                byte[] icono = resultSet.getBytes("icono");
                aux = new Parada(nombre, tipoTransporte, posicionx, posiciony, icono);
                aux.setId(resultSet.getLong("id"));

            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return aux;
    }
    public boolean existeRutaIgual(Ruta aux) {
        final String sql = "SELECT COUNT(*) AS count FROM ruta WHERE origen_id = ? AND destino_id = ?";
        boolean existe = false;

        try (Connection connection = DataBaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, aux.getOrigen().getId());
            preparedStatement.setLong(2, aux.getDestino().getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                existe = count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existe;
    }

    public void eliminarRutaByParada(long id) {
        final String sql = "DELETE FROM ruta WHERE origen_id = ? OR destino_id = ?";
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
