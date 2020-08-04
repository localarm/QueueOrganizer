import com.pavel.queueorganizer.dao.postgres.PostgresExecutorDAO;

public class DerbyExecutorDAO extends PostgresExecutorDAO {
    @Override
    protected String getFreeExecutorSQLQuery() {
        return "SELECT client_id, name, serve_client, invalid, active, serving_client_id, waiting FROM executor" +
                " WHERE waiting = true AND queue_id = ? {LIMIT 1}";
    }
}
