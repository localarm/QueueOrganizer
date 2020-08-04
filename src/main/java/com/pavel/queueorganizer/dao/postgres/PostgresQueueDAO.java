package com.pavel.queueorganizer.dao.postgres;
import com.pavel.queueorganizer.dao.QueueDAO;
import com.pavel.queueorganizer.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresQueueDAO implements QueueDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(PostgresQueueDAO.class);
    private final boolean ignoreDistance;

    public PostgresQueueDAO (boolean ignoreDistance) {
        this.ignoreDistance = ignoreDistance;
    }

    @Override
    public long create(Connection conn, Queue queue) throws SQLException {
      String sql = "INSERT INTO queue(name, longitude, latitude, admin, start_time, geom) VALUES(?,?,?,?,? " +
              "ST_SetSRID(ST_MakePoint(?,?), 4326))";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1,queue.getName());
            stmt.setFloat(2,queue.getLongitude());
            stmt.setFloat(3,queue.getLatitude());
            stmt.setLong(4,queue.getAdmin());
            stmt.setTimestamp(5, queue.getStartTime());
            stmt.setFloat(6,queue.getLongitude());
            stmt.setFloat(7,queue.getLatitude());
            stmt.executeUpdate();
            try (ResultSet resultSet = stmt.getGeneratedKeys()){
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    public Queue get(Connection conn, long id) throws SQLException {
        String sql = "SELECT id, name, longitude, latitude, admin, work_hours, active, invalid, start_time, end_time, " +
                "last_place FROM queue WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,id);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()){
                    return fillQueueFromResultSet(resultSet);
                } else {
                    return null;
                }
            }
        }
    }


    @Override
    public List<Queue> getByDistance(Connection conn, float longitude, float latitude) throws SQLException {
        String sql = getByDistanceQuery();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            if (!ignoreDistance) {
                stmt.setFloat(1, longitude);
                stmt.setFloat(2, latitude);
            }
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()) {
                ArrayList<Queue> returnedQueues = new ArrayList<>();
                while (resultSet.next()) {
                    returnedQueues.add(fillQueueFromResultSet(resultSet));
                }
                return returnedQueues;
            }
        }
    }

    protected String getByDistanceQuery() {
        if (ignoreDistance) {
            return "SELECT q.id, q.name, q.longitude, q.latitude, q.admin, q.work_hours, q.active, q.invalid," +
                    " q.start_time, q.end_time, q.last_place FROM queue AS q LEFT JOIN (SELECT queue_id, " +
                    "COUNT(DISTINCT client_id) AS count from client_queue GROUP BY queue_id) AS cq ON q.id = " +
                    "cq.queue_id WHERE q.invalid = false ORDER BY count DESC NULLS LAST LIMIT 30";
        } else {
            return "SELECT q.id, q.name, q.longitude, q.latitude, q.admin, q.work_hours, q.active, q.invalid," +
                    " q.start_time, q.end_time, q.last_place FROM queue AS q LEFT JOIN (SELECT queue_id, " +
                    "COUNT(DISTINCT client_id) AS count from client_queue GROUP BY queue_id) AS cq ON q.id = " +
                    "cq.queue_id WHERE q.invalid = false and ST_DWithin(geom::geography, " +
                    "ST_SetSRID(ST_MakePoint(?, ?),4326)::geography, 60) ORDER BY count DESC NULLS LAST LIMIT 30";
        }
    }

    @Override
    public void updateWorkHours(Connection conn, long id, Timestamp startTime, Timestamp endTime,
                                String workHours) throws SQLException {
        String sql = "UPDATE queue SET work_hours = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,workHours);
            stmt.setTimestamp(2,startTime);
            stmt.setTimestamp(3,endTime);
            stmt.setLong(4, id);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update queue row, nonexistent id = {}", id);
            }
        }
    }

    @Override
    public void updateActivity(Connection conn, long id, boolean status) throws SQLException {
        String sql = "UPDATE queue SET active = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, status);
            stmt.setLong(2, id);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update queue row, nonexistent id = {}", id);
            }
        }
    }

    @Override
    public void close(Connection conn, long id) throws SQLException {
        String sql = "UPDATE queue SET invalid = true, end_time = current_timestamp WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update queue row, nonexistent id = {}", id);
            }
        }
    }

    @Override
    public int getCountByAdmin(Connection conn, long adminId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM queue WHERE admin = ? AND invalid = FALSE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1, adminId);
            stmt.execute();
            try(ResultSet resultSet = stmt.getResultSet()) {
                if (!resultSet.next()){
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public void updateLastPlace(Connection conn, long id, long place) throws SQLException {
        String sql = "UPDATE queue SET last_place = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1,place);
            stmt.setLong(2,id);
            if (stmt.executeUpdate()==0) {
                LOGGER.warn("Failed to update queue row, nonexistent id = {}", id);
            }
        }
    }
    @Override
    public List<Queue> getAllQueuesByAdmin(Connection conn, long adminId) throws SQLException {
        String sql = "SELECT id, name, longitude, latitude, admin, work_hours, active, invalid, start_time, end_time, " +
                "last_place FROM queue WHERE admin = ? and invalid = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,adminId);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()) {
                ArrayList<Queue> returnedQueues = new ArrayList<>();
                while (resultSet.next()) {
                    returnedQueues.add(fillQueueFromResultSet(resultSet));
                }
                return returnedQueues;
            }
        }
    }

    @Override
    public boolean checkQueueName(Connection conn, String queueName) throws SQLException {
        String sql = "SELECT name FROM queue WHERE name = ? and invalid = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, queueName);
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public List<Queue> getQueuesByExecutor(Connection conn, long execId, float longitude, float latitude) throws
            SQLException {
        String sql = getQueuesByExecutorQuery();
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,execId);
            stmt.setLong(2,execId);
            if (!ignoreDistance) {
                stmt.setFloat(3, longitude);
                stmt.setFloat(4, latitude);
            }
            stmt.execute();
            try (ResultSet resultSet = stmt.getResultSet()) {
                ArrayList<Queue> returnedQueues = new ArrayList<>();
                while (resultSet.next()) {
                    returnedQueues.add(fillQueueFromResultSet(resultSet));
                }
                return returnedQueues;
            }
        }
    }

    private String getQueuesByExecutorQuery() {
        if (ignoreDistance) {
            return "SELECT q.id, q.name, q.longitude, q.latitude, q.admin, q.work_hours, q.active, q.invalid, " +
                    "q.start_time, q.end_time, q.last_place FROM queue AS q JOIN executor ON q.id = executor.queue_id "+
                    "LEFT JOIN (SELECT queue_id, COUNT(DISTINCT served_by_id) AS count from client_queue WHERE " +
                    "served_by_id = ? GROUP BY queue_id) AS cq ON cq.queue_id = q.id WHERE executor.invalid =" +
                    " false AND executor.client_id = ? AND q.invalid = false ORDER BY count NULLS LAST";
        } else {
            return "SELECT q.id, q.name, q.longitude, q.latitude, q.admin, q.work_hours, q.active, q.invalid, " +
                    "q.start_time, q.end_time, q.last_place FROM queue AS q JOIN executor ON q.id = executor.queue_id "+
                    "LEFT JOIN (SELECT queue_id, COUNT(DISTINCT served_by_id) AS count from client_queue WHERE " +
                    "served_by_id = ? GROUP BY queue_id) AS cq ON cq.queue_id = q.id WHERE executor.invalid =" +
                    " false AND executor.client_id = ? AND q.invalid = false and " +
                    "ST_DWithin(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326), 60) ORDER BY count NULLS LAST";
        }
    }


    protected Queue fillQueueFromResultSet(ResultSet resultSet) throws SQLException {
        Queue returnedQueue = new Queue(resultSet.getString("name"),
                resultSet.getFloat("longitude"), resultSet.getFloat("latitude"),
                resultSet.getLong("admin"), resultSet.getTimestamp("start_time"));
        returnedQueue.setId(resultSet.getLong("id"));
        returnedQueue.setWorkHours(resultSet.getString("work_hours"));
        returnedQueue.setActive(resultSet.getBoolean("active"));
        returnedQueue.setInvalid(resultSet.getBoolean("invalid"));
        returnedQueue.setEndTime(resultSet.getTimestamp("end_time"));
        returnedQueue.setLastPlace(resultSet.getLong("last_place"));
        return returnedQueue;
    }
}
