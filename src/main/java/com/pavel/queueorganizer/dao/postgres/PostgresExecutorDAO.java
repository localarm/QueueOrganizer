package com.pavel.queueorganizer.dao.postgres;
import com.pavel.queueorganizer.dao.ExecutorDAO;
import com.pavel.queueorganizer.Executor;
import com.pavel.queueorganizer.dao.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PostgresExecutorDAO implements ExecutorDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(PostgresExecutorDAO.class);


    @Override
    public void add(Connection conn, Executor executor) throws SQLException{
        String sql = "INSERT INTO executor(client_id, name, queue_id) VALUES(?,?,?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,executor.getClientId());
            stmt.setString(2,executor.getName());
            stmt.setLong(3,executor.getQueueId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateServeAndServingClientStatus(Connection conn, long execId, long queueId, long servingClientId)
            throws SQLException {
        String sql = "UPDATE executor SET serve_client = true, serving_client_id = ? WHERE client_id = ? AND " +
                "queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, servingClientId);
            stmt.setLong(2,execId);
            stmt.setLong(3,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public void updateServeAndServingIdWhileWaiting(Connection conn, long execId, long queueId,
                                                    long servingClientId) throws SQLException {
        String sql = "UPDATE executor SET serve_client = true, serving_client_id = ?, waiting = false WHERE " +
                "client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, servingClientId);
            stmt.setLong(2,execId);
            stmt.setLong(3,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public Executor get(Connection conn, long queueId, long execId) throws SQLException {
        String sql = "SELECT client_id, queue_id, name, serve_client, invalid, active, serving_client_id, waiting FROM"+
                " executor WHERE client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,execId);
            stmt.setLong(2,queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                if (resultSet.next()) {
                    Executor executor = new Executor();
                    executor.setClientId(execId);
                    executor.setQueueId(queueId);
                    executor.setName(resultSet.getString("name"));
                    executor.setServeClient(resultSet.getBoolean("serve_client"));
                    executor.setActiveNow(resultSet.getBoolean("active"));
                    executor.setInvalid(resultSet.getBoolean("invalid"));
                    executor.setServingClient(resultSet.getLong("serving_client_id"));
                    executor.setWaiting(resultSet.getBoolean("waiting"));
                    return executor;
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public List<Executor> getAllByQueue(Connection conn, long queueId) throws SQLException {
        String sql = "SELECT client_id, queue_id, name, serve_client, invalid, active, serving_client_id, waiting FROM " +
                "executor WHERE invalid = false AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                ArrayList<Executor> executors = new ArrayList<>();
                while (resultSet.next()) {
                    Executor executor = new Executor();
                    executor.setClientId(resultSet.getLong("client_id"));
                    executor.setQueueId(resultSet.getLong("queue_id"));
                    executor.setName(resultSet.getString("name"));
                    executor.setServeClient(resultSet.getBoolean("serve_client"));
                    executor.setActiveNow(resultSet.getBoolean("active"));
                    executor.setServingClient(resultSet.getLong("serving_client_id"));
                    executors.add(executor);
                }
                return executors;
            }
        }
    }

    @Override
    public void updateActiveStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException {
        String sql = "UPDATE executor SET active= ? WHERE client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setLong(2,execId);
            stmt.setLong(3,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public void updateName(Connection conn, long execId, long queueId, String newName) throws SQLException {
        String sql = "UPDATE executor SET name= ? WHERE client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setLong(2,execId);
            stmt.setLong(3,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public int getCountByQueue(Connection conn, long queueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM executor WHERE queue_id = ? AND invalid = FALSE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, queueId);
            stmt.execute();
            try(ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                LOGGER.warn("Failed to select count from executor by queue id = {}", queueId);
                return 0;
            }
        }
    }

    @Override
    public Executor getByName(Connection conn, String name, long queueId) throws SQLException {
        String sql = "SELECT client_id, queue_id, name, serve_client, invalid, active, serving_client_id, waiting FROM " +
                "executor WHERE name = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, name);
            stmt.setLong(2,queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                if (resultSet.next()) {
                    Executor executor = new Executor();
                    executor.setClientId(resultSet.getLong("client_Id"));
                    executor.setQueueId(queueId);
                    executor.setName(resultSet.getString("name"));
                    executor.setServeClient(resultSet.getBoolean("serve_client"));
                    executor.setActiveNow(resultSet.getBoolean("active"));
                    executor.setInvalid(resultSet.getBoolean("invalid"));
                    executor.setWaiting(resultSet.getBoolean("waiting"));
                    return executor;
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public void updateInvalidStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException {
        String sql = "UPDATE executor SET invalid = ? WHERE client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1,status);
            stmt.setLong(2, execId);
            stmt.setLong(3, queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    public void setInvalidAndActiveStatus(Connection conn, long execId, long queueId) throws SQLException {
        String sql = "UPDATE executor SET invalid = true, active = false WHERE client_id = ? AND queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, execId);
            stmt.setLong(2, queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public void updateQuitStatuses(Connection conn, long execId, long queueId) throws SQLException {
        String sql = "UPDATE executor SET active = false, serve_client = false, waiting = false WHERE client_id= ? " +
                "AND queue_id = ?";
        execute(conn, sql, x->{
            x.setLong(1, execId);
            x.setLong(2, queueId);
            if (x.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
            return null;
        });
    }

    @Override
    public void setInvalidToQueue(Connection conn, long queueId) throws SQLException {
        String sql = "UPDATE executor SET invalid = true WHERE queue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to set invalid status at executor rows where queue_id = {}",  queueId);
            }
        }
    }

    @Override
    public void updateServeStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException {
        String sql = "UPDATE executor SET serve_client= ? WHERE client_id = ? AND queue_id = ? ";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setLong(2,execId);
            stmt.setLong(3,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public void setWaitingStatus(Connection conn, long execId, long queueId) throws SQLException {
        String sql = "UPDATE executor SET waiting = true WHERE client_id = ? AND queue_id = ? ";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1,execId);
            stmt.setLong(2,queueId);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update executor row with client_id = {} and queue_id = {}", execId, queueId);
            }
        }
    }

    @Override
    public Executor getFreeExecutor(Connection conn, long queueId) throws SQLException {
        String sql = getFreeExecutorSQLQuery();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, queueId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()){
                if (resultSet.next()) {
                    Executor executor = new Executor();
                    executor.setClientId(resultSet.getLong("client_Id"));
                    executor.setQueueId(queueId);
                    executor.setName(resultSet.getString("name"));
                    executor.setServeClient(resultSet.getBoolean("serve_client"));
                    executor.setActiveNow(resultSet.getBoolean("active"));
                    executor.setInvalid(resultSet.getBoolean("invalid"));
                    executor.setWaiting(resultSet.getBoolean("waiting"));
                    return executor;
                } else {
                    return null;
                }
            }
        }
    }

    protected String getFreeExecutorSQLQuery() {
        return "SELECT client_id, name, serve_client, invalid, active, serving_client_id, waiting FROM executor" +
                " WHERE waiting = true AND queue_id = ? LIMIT 1";
    }

    private <T> T execute(Connection conn, String sql, Functional<T> lambda) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            return lambda.setup(stmt);
        }
    }
}
