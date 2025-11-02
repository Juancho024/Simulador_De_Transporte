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
        final String sql = "INSERT INTO ruta (origen, destino, distancia, tiempoRecorrido, costo, numTransbordo, \"posibleEvento\") VALUES (?, ?, ?, ?, ?, ?, ?)";

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

                Long referencia = ruta.getId();
                rutas.computeIfAbsent(referencia, k -> new LinkedList<>()).add(ruta);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return rutas;
    }
    public void actualizarRuta(Ruta ruta){
        final String sql = "UPDATE ruta SET origen = ?, destino = ?, distancia = ?, tiempoRecorrido = ?, costo = ?, numTransbordo = ?, \"posibleEvento\" = ? WHERE id = ?";
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


    private Parada obtenerParadaPorId(long destinoId) {
        Parada parada = null;
        final String sql = "SELECT * FROM parada WHERE id = ?";
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String nombre = resultSet.getString("nombre");
                 String tipoTransporte = resultSet.getString("tipoTransporte");
                 int posiciony = resultSet.getInt("posiciony");
                 int posicionx = resultSet.getInt("posicionx");
                 parada = new Parada(nombre, tipoTransporte, posicionx, posiciony);
                 parada.setId(resultSet.getLong("id"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return parada;
    }
}
