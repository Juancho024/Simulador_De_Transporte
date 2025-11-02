package DataBase;

import Model.Parada;

import java.sql.*;
import java.util.HashMap;

public class ParadaDAO {
    private static ParadaDAO instance = null;
    private ParadaDAO() {}

    public static ParadaDAO getInstance() {
        if (instance == null) {
            instance = new ParadaDAO();
        }
        return instance;
    }

    public void guardarParada(Parada parada) {
        final String sql = "INSERT INTO parada (\"nombre\", \"tipoTransporte\", posicionx, posiciony) VALUES (?, ?, ?, ?)";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipoTransporte());
            preparedStatement.setInt(3, parada.getPosicionx());
            preparedStatement.setInt(4, parada.getPosiciony());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error al guardar la parada: " + e.getMessage());
        }
    }

    public HashMap<Long, Parada> obtenerParadas() {
        HashMap<Long, Parada> paradas = new HashMap<>();
        final String sql = "SELECT * FROM parada";
        try(Connection connection = DataBaseConnection.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()){
                String nombre = resultSet.getString("nombre");
                String tipoTransporte = resultSet.getString("tipoTransporte");
                int posicionx = resultSet.getInt("posicionx");
                int posiciony = resultSet.getInt("posiciony");
                Parada parada = new Parada(nombre, tipoTransporte, posicionx, posiciony);
                parada.setId(resultSet.getLong("id"));
                paradas.put(parada.getId(), parada);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return paradas;
    }

    public void actualizarParada(Parada parada) {
        final String sql = "UPDATE parada SET nombre = ?, \"tipoTransporte\" = ?, posicionx = ?, posiciony = ? WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipoTransporte());
            preparedStatement.setInt(3, parada.getPosicionx());
            preparedStatement.setInt(4, parada.getPosiciony());
            preparedStatement.setLong(5, parada.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void eliminarParada(Long id) {
        final String sql = "DELETE FROM parada WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
