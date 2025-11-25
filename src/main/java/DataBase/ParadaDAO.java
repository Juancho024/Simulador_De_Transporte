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
        final String sql = "INSERT INTO parada (\"nombre\", \"tipoTransporte\", posicionx, posiciony, icono) VALUES (?, ?, ?, ?, ?)";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipoTransporte());
            preparedStatement.setDouble(3, parada.getPosicionx());
            preparedStatement.setDouble(4, parada.getPosiciony());
            if(parada.getIcono() != null){
                preparedStatement.setBytes(5, parada.getIcono());
            } else {
                preparedStatement.setBytes(5, new  byte[]{});
            }
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
                byte[] icono;
                if(resultSet.getBytes("icono") != null){
                    icono = resultSet.getBytes("icono");
                } else {
                    icono = new  byte[]{};
                }
                Parada parada = new Parada(nombre, tipoTransporte, posicionx, posiciony, icono);
                parada.setId(resultSet.getLong("id"));
                paradas.put(parada.getId(), parada);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return paradas;
    }

    public void actualizarParada(Parada parada) {
        final String sql = "UPDATE parada SET nombre = ?, \"tipoTransporte\" = ?, posicionx = ?, posiciony = ?, icono = ? WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parada.getNombre());
            preparedStatement.setString(2, parada.getTipoTransporte());
            preparedStatement.setDouble(3, parada.getPosicionx());
            preparedStatement.setDouble(4, parada.getPosiciony());
            preparedStatement.setBytes(5, parada.getIcono());
            preparedStatement.setLong(6, parada.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public boolean validarParadaByCoordenada(int x, int y){
        final String sql = "SELECT * FROM parada WHERE posicionx = ? AND posiciony = ?";
        try(Connection connection = DataBaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, x);
            preparedStatement.setInt(2, y);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Parada buscarParadaByName(String name){
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

