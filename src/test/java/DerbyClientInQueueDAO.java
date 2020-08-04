import com.pavel.queueorganizer.ClientInQueue;
import com.pavel.queueorganizer.ServedClient;
import com.pavel.queueorganizer.dao.postgres.PostgresClientInQueueDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DerbyClientInQueueDAO extends PostgresClientInQueueDAO {

    public ClientInQueue getCompleted(Connection conn, long clientId, long queueId) throws SQLException {
        String sql = "SELECT client_id, queue_id, start_time, end_time, place, complete, served_by_id, notification " +
                "FROM client_queue WHERE queue_id = ? AND client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setLong(1,queueId);
            stmt.setLong(2, clientId);
            stmt.execute();
            try(ResultSet rs = stmt.getResultSet()){
                rs.next();
                return fillClientFromResultSet(rs);
            }
        }
    }

    @Override
    protected String getNextClientsQuery() {
        return "SELECT client_id, queue_id, start_time, end_time, place, complete, served_by_id, notification FROM " +
                "client_queue WHERE queue_id = ? AND complete = false AND start_time is null ORDER BY place {LIMIT 2}";
    }

    @Override
    protected String getServedClientsSQLQuery() {
        return "SELECT c.first_name, c.last_name, cq.start_time, cq.end_time FROM client_queue AS cq JOIN client AS c " +
                "ON cq.client_id = c.id WHERE cq.queue_id = ? AND cq.served_by_id = ? ORDER BY cq.end_time DESC {LIMIT 20}";
    }


}
