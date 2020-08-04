import com.pavel.queueorganizer.Queue;
import com.pavel.queueorganizer.dao.postgres.PostgresQueueDAO;
import java.sql.*;

public class DerbyQueueDAO extends PostgresQueueDAO {

    public DerbyQueueDAO(boolean ignoreDistance) {
        super(ignoreDistance);
    }

    @Override
    public long create(Connection conn, Queue queue) throws SQLException {
        String sql = "INSERT INTO queue(name, longitude, latitude, admin) VALUES(?,?,?,?)";
        try (
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1,queue.getName());
            stmt.setFloat(2,queue.getLongitude());
            stmt.setFloat(3,queue.getLatitude());
            stmt.setLong(4,queue.getAdmin());
            stmt.executeUpdate();
            try (ResultSet resultSet = stmt.getGeneratedKeys()){
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    protected String getByDistanceQuery() {
        return "SELECT q.id, q.name, q.longitude, q.latitude, q.admin, q.work_hours, q.active, q.invalid," +
                " q.start_time, q.end_time, q.last_place FROM queue AS q LEFT JOIN (SELECT queue_id, " +
                "COUNT(DISTINCT client_id) AS count from client_queue GROUP BY queue_id) AS cq ON q.id = " +
                "cq.queue_id WHERE q.invalid = false ORDER BY count DESC NULLS LAST {LIMIT 30}";
    }
}
