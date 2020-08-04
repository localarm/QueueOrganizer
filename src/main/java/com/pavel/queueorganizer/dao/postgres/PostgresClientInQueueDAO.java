package com.pavel.queueorganizer.dao.postgres;

import com.pavel.queueorganizer.ServedClient;
import com.pavel.queueorganizer.dao.ClientInQueueDAO;
import com.pavel.queueorganizer.ClientInQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresClientInQueueDAO implements ClientInQueueDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(PostgresClientInQueueDAO.class);
    
    @Override
    public void add(Connection conn, ClientInQueue clientInQueue) throws SQLException{
        String sql = "INSERT INTO client_queue(client_id, queue_id, place) VALUES(?,?,?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, clientInQueue.getClientId());
            stmt.setLong(2, clientInQueue.getQueueId());
            stmt.setLong(3, clientInQueue.getPlace());
            stmt.executeUpdate();
        }
    }

    @Override
    public ClientInQueue get(Connection conn, long clientId, long queueId) throws SQLException {
        String sql = "SELECT client_id, queue_id, start_time, end_time, place, complete, served_by_id, notification" +
                " FROM client_queue WHERE client_id = ? AND queue_id = ? AND complete = false";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,clientId);
            stmt.setLong(2,queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                if (resultSet.next()){
                    return fillClientFromResultSet(resultSet);
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public int getCountByQueue(Connection conn, long queueId) throws SQLException {
        String sql ="SELECT COUNT(*) FROM client_queue WHERE queue_id = ? and complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,queueId);
            stmt.execute();
            try(ResultSet resultSet = stmt.getResultSet()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public int getCountByClient(Connection conn, long clientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM client_queue WHERE client_id = ? and complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,clientId);
            stmt.execute();
            try(ResultSet resultSet = stmt.getResultSet()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public void completeByClient(Connection conn, long clientId, long queueId) throws SQLException{
        String sql = "UPDATE client_queue SET complete = true WHERE queue_id = ? AND client_id = ? AND complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, queueId);
            stmt.setLong(2, clientId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update client_queue row with client_id = {}, queue_id = {}", clientId,queueId);
            }
        }
    }

    @Override
    public void completeByExecutor(Connection conn, long clientId, long queueId, Timestamp time) throws
            SQLException {
        String sql = "UPDATE client_queue SET complete = true, end_time = ? WHERE queue_id = ? AND " +
                "client_id = ? AND complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1,time);
            stmt.setLong(2, queueId);
            stmt.setLong(3, clientId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update client_queue row with client_id = {}, queue_id = {}",
                        clientId, queueId);
            }
        }
    }

    @Override
    public List<ClientInQueue> getRemainingClients(Connection conn, long queueId) throws SQLException {
        String sql = "SELECT client_id, queue_id, start_time, end_time, place, complete, served_by_id, notification " +
                "FROM client_queue WHERE queue_id = ? AND complete = false AND start_time is null";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                ArrayList<ClientInQueue> remainingClients = new ArrayList<>();
                while(resultSet.next()){
                    remainingClients.add(fillClientFromResultSet(resultSet));
                }
                return remainingClients;
            }
        }
    }

    @Override
    public List<ClientInQueue> getNextClients(Connection conn, long queueId) throws SQLException {
        String sql = getNextClientsQuery();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                ArrayList<ClientInQueue> remainingClients = new ArrayList<>();
                while(resultSet.next()){
                    remainingClients.add(fillClientFromResultSet(resultSet));
                }
                return remainingClients;
            }
        }
    }

    protected String getNextClientsQuery() {
        return "SELECT client_id, queue_id, start_time, end_time, place, complete, served_by_id, notification FROM " +
                "client_queue WHERE queue_id = ? AND complete = false AND start_time is null ORDER BY place LIMIT 2";
    }

    @Override
    public List<ServedClient> getServedClients(Connection conn, long queueId, long execId) throws SQLException {
        String sql = getServedClientsSQLQuery();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, queueId);
            stmt.setLong(2, execId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                ArrayList<ServedClient> clientsInQueue = new ArrayList<>();
                while(resultSet.next()){
                    ServedClient servedClient = new ServedClient(resultSet.getString(1) + " " +
                            resultSet.getString(2), resultSet.getTimestamp(3),
                            resultSet.getTimestamp(4));
                    clientsInQueue.add(servedClient);
                }
                return clientsInQueue;
            }
        }
    }

    protected String getServedClientsSQLQuery(){
        return "SELECT c.first_name, c.last_name, cq.start_time, cq.end_time FROM client_queue AS cq JOIN client AS c " +
                "ON cq.client_id = c.id WHERE cq.queue_id = ? AND cq.served_by_id = ? ORDER BY cq.end_time DESC LIMIT 20";
    }

    @Override
    public void updateNotificationStatus(Connection conn, long clientId, long queueId, boolean status) throws
            SQLException {
        String sql = "UPDATE client_queue SET notification = ? WHERE queue_id = ? AND client_id = ? AND" +
                " complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setLong(2, queueId);
            stmt.setLong(3, clientId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update client_queue row with client_id = {}, queue_id = {}",
                        clientId, queueId);
            }
        }
    }

    @Override
    public void updateStartTimeAndServedId(Connection conn, long clientId, long queueId, long execId, Timestamp time)
            throws SQLException {
        String sql = "UPDATE client_queue SET start_time = ?, served_by_id = ? WHERE queue_id = ? AND client_id = ? " +
                "AND complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, time);
            stmt.setLong(2, execId);
            stmt.setLong(3, queueId);
            stmt.setLong(4, clientId);
            if (stmt.executeUpdate() == 0) {
                LOGGER.warn("Failed to update client_queue row with client_id = {}, queue_id = {}", clientId, queueId);
            }
        }
    }

    @Override
    public void completeAllClientsInQueue(Connection conn, long queueId) throws SQLException {
        String sql = "UPDATE client_queue SET complete = true WHERE queue_id = ? AND complete = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update client_queue rows with  queue_id = {}", queueId);
            }
        }
    }

    protected ClientInQueue fillClientFromResultSet(ResultSet resultSet) throws SQLException {
        ClientInQueue clientInQueue = new ClientInQueue (resultSet.getLong("client_id"),
                resultSet.getLong("queue_id"));
        clientInQueue.setStartTime(resultSet.getTimestamp("start_time"));
        clientInQueue.setEndTime(resultSet.getTimestamp("end_time"));
        clientInQueue.setComplete(resultSet.getBoolean("complete"));
        clientInQueue.setServedById(resultSet.getLong("served_by_id"));
        clientInQueue.setNotificationStatus(resultSet.getBoolean("notification"));
        clientInQueue.setPlace(resultSet.getLong("place"));
        return clientInQueue;
    }

}
