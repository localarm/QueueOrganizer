package com.pavel.queueorganizer.dao.postgres;
import com.pavel.queueorganizer.dao.ClientDAO;
import com.pavel.queueorganizer.Client;
import java.sql.*;

public class PostgresClientDAO implements ClientDAO {
   
    @Override
    public void add(Connection conn, Client client) throws SQLException {
        String sql = "INSERT INTO client (first_name, last_name, id) VALUES (?,?,?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,client.getFirstName());
            stmt.setString(2,client.getLastName());
            stmt.setLong(3,client.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public Client get(Connection conn, long clientId) throws SQLException {
        String sql = "SELECT first_name, last_name, id FROM client WHERE id=?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1,clientId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                if (resultSet.next()) {
                    Client client = new Client(resultSet.getLong("id"),
                            resultSet.getString("first_name"));
                    String lastName = resultSet.getString("last_name");
                    if (lastName != null){
                        client.setLastName(lastName);
                    }
                    return client;
                } else {
                    return null;
                }
            }

        }
    }
}
