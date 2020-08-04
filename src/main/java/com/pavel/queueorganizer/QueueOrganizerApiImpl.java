package com.pavel.queueorganizer;
import com.pavel.queueorganizer.dao.ClientDAO;
import com.pavel.queueorganizer.dao.ClientInQueueDAO;
import com.pavel.queueorganizer.dao.ExecutorDAO;
import com.pavel.queueorganizer.dao.QueueDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class QueueOrganizerApiImpl implements QueueOrganizerApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueueOrganizerApiImpl.class);
    private final QueueDAO queueDao;
    private final ClientDAO clientDao;
    private final ExecutorDAO executorDao;
    private final ClientInQueueDAO clientInQueueDao;
    private final DataSource ds;

    public QueueOrganizerApiImpl(DataSource ds, QueueDAO queueDao, ClientDAO clientDao,
                                 ExecutorDAO executorDao, ClientInQueueDAO clientInQueueDao) {
        this.ds = ds;
        this.queueDao = queueDao;
        this.clientDao = clientDao;
        this.executorDao = executorDao;
        this.clientInQueueDao = clientInQueueDao;
    }

    @Override
    public void addClient(long clientId, String firstName, String lastName) throws QueueOrganizerException {
        try (Connection conn = ds.getConnection()) {
            if (clientDao.get(conn, clientId) == null){
                Client client = new Client(clientId, firstName);
                if (lastName != null ) {
                    client.setLastName(lastName);
                }
                clientDao.add(conn, client);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to add client in database", e);
        }
    }

    @Override
    public void createQueue(String queueName, String adminFirstName, String adminLastName, float longitude,
                            float latitude, long adminId) throws OverlimitException, QueueOrganizerException,
            NameCollisionException, NameCharsLimitException {
        try (Connection conn = ds.getConnection()) {
            if (queueName.length()>64){
                throw new NameCharsLimitException("Queue name " + queueName + " is too long");
            }
            int ownedQueueAmount = queueDao.getCountByAdmin(conn, adminId);
            if (ownedQueueAmount >= 10 ){
                throw new OverlimitException("Queue limit by " + adminId + " reached");
            }
            if (queueDao.checkQueueName(conn, queueName)){
                throw new NameCollisionException("Queue with " + queueName + " name already exist");
            }
            conn.setAutoCommit(false);
            try {
                Queue newQueue = new Queue(queueName, longitude, latitude, adminId,
                        new Timestamp(System.currentTimeMillis()));
                long queueId = queueDao.create(conn, newQueue);
                //добавление администратора первым исполнителем очереди
                Executor firstExecutor = new Executor();
                firstExecutor.setClientId(adminId);
                String executorName = adminFirstName;
                if (adminLastName != null) {
                    executorName = executorName + " " + adminLastName;
                }
                firstExecutor.setName(executorName);
                firstExecutor.setQueueId(queueId);
                executorDao.add(conn, firstExecutor);
                conn.commit();
                LOGGER.info("New queue {} was created by client {}", queueId, adminId);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to create new queue", e);
        }
    }

    @Override
    public List<ClientInQueue> closeQueue(long adminId, long queueId) throws AccessException, QueueOrganizerException,
            NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkAndGetQueueByAdmin(conn, queueId, adminId);
            conn.setAutoCommit(false);
            List<ClientInQueue> clientsToNotify = clientInQueueDao.getRemainingClients(conn, queueId);
            try {
                queueDao.close(conn, queueId);
                //завершаю ожидание в очереди всех клиентов и увольняю исполнтелей
                clientInQueueDao.completeAllClientsInQueue(conn, queueId);
                executorDao.setInvalidToQueue(conn, queueId);
                conn.commit();
                LOGGER.info("Queue {} successfully closed by admin {}", queueId, adminId);
            } catch (Exception e){
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            return clientsToNotify;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to close queue with " + queueId + "id", e);
        }
    }

    @Override
    public List<Queue> getQueuesByAdmin(long adminId) throws QueueOrganizerException {
        try(Connection conn = ds.getConnection()){
        List<Queue> returnedQueues = queueDao.getAllQueuesByAdmin(conn, adminId);
        LOGGER.info("Admin {} requested list of his queues", adminId);
        return returnedQueues;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed get list of queues by admin " + adminId, e);
        }
    }

    @Override
    public void changeWorkHours(long adminId, long queueId, String period, String hours) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            Queue queue = checkAndGetQueueByAdmin(conn, queueId, adminId);
            if (period.equals("same")) {
                queueDao.updateWorkHours(conn, queueId, queue.getStartTime(), queue.getEndTime(), hours);
                LOGGER.info("Admin {} change queue {} period {} {}  and work hours {}", adminId, queueId,
                        queue.getStartTime(), queue.getEndTime(), hours);
            } else {
                Timestamp startTime = PeriodAndWorkHours.parseStartTime(period);
                Timestamp endTime = PeriodAndWorkHours.parseEndTime(period);
                queueDao.updateWorkHours(conn, queueId, startTime, endTime ,hours);
                LOGGER.info("Admin {} change queue {} period {} {}  and work hours {}", adminId, queueId, startTime,
                        endTime, hours);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to change workHours and period of queue " + queueId, e);
        }
    }

    @Override
    public void addExecutor(long execId, long adminId, long queueId, String name) throws AccessException,
            OverlimitException, NameCollisionException, QueueOrganizerException, NonexistentQueueIdException,
            NameCharsLimitException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            if (name.length()>511){
                throw new NameCharsLimitException("Executor name " + name + " is too long");
            }
            checkAndGetQueueByAdmin(conn, queueId, adminId);
            if (executorDao.getCountByQueue(conn, queueId)>=100) {
                throw new OverlimitException("Executor's limit in queue with" + queueId + " id reached");
            }
            Executor getByNameExec = executorDao.getByName(conn, name, queueId);
            if (getByNameExec != null && !getByNameExec.isInvalid()){
                throw new NameCollisionException("Executor with " + name + " name already exist");
            }
            Executor executor = executorDao.get(conn, queueId, execId);
            conn.setAutoCommit(false);
            try {
                if (executor == null){
                    Executor newExecutor = new Executor();
                    newExecutor.setName(name);
                    newExecutor.setClientId(execId);
                    newExecutor.setQueueId(queueId);
                    executorDao.add(conn, newExecutor);
                } else {
                    executorDao.updateInvalidStatus(conn, execId, queueId, false);
                    if (!executor.getName().equals(name)) {
                        executorDao.updateName(conn, execId, queueId, name);
                    }
                }
                LOGGER.info("Admin {} successfully added executor {} with name {}", adminId, execId, name);
            } catch (Exception e){
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to add executor with " + adminId
                    +" id to " + queueId + " queue", e);
        }
    }

    @Override
    public void fireExecutor(long execId, long adminId, long queueId) throws AccessException, QueueOrganizerException,
            NonexistentQueueIdException, NonexistentExecutorIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkAndGetQueueByAdmin(conn, queueId, adminId);
            Executor executor = executorDao.get(conn, queueId, execId);
            if ( executor == null){
                throw new NonexistentExecutorIdException("Wrong executor id " + execId);
            }
            if (executor.isInvalid()){
                return;
            }
            conn.setAutoCommit(false);
            try {
                executorDao.setInvalidAndActiveStatus(conn, execId,queueId);
                if (executor.isServeClient()){
                    clientInQueueDao.completeByExecutor(conn, executor.getServingClient(), queueId,
                            new Timestamp(System.currentTimeMillis()));
                }
                conn.commit();
                LOGGER.info("Executor {} fired by {} admin from {} queue", execId, adminId, queueId);
            } catch (Exception e){
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to fire executor with " + adminId +" id from "
                    + queueId+ " queue", e);
        }
    }

    @Override
    public List<Executor> getExecList (long adminId, long queueId) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkAndGetQueueByAdmin(conn, queueId, adminId);
            List<Executor> executors = executorDao.getAllByQueue(conn, queueId);
            LOGGER.info("Admin {} got list of executors of {} queue", adminId, queueId);
            return executors;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get executors list from"  + queueId+ " queue", e);
        }
    }

    @Override
    public void switchQueueActiveStatus(long adminId, long queueId, boolean status) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            Queue queue = checkAndGetQueueByAdmin(conn, queueId, adminId);
            if (queue.isActive() == status){
                return;
            }
            queueDao.updateActivity(conn, queueId, status);
            LOGGER.info("Queue {} successfully change active status to {}", queueId, status);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to change queue serve status to " + status + " in queue id " +
                    queueId + " by client " + adminId, e);
        }
    }

    @Override
    public void changeExecName(long execId, long queueId, long adminId, String newName) throws AccessException,
            QueueOrganizerException, NameCollisionException, NonexistentQueueIdException, ClosedQueueException,
            NonexistentExecutorIdException, NameCharsLimitException {
        try (Connection conn = ds.getConnection()) {
            if (newName.length()>511){
                throw new NameCharsLimitException("Executor's name " + newName + " is too long");
            }
            checkAndGetQueueByAdmin(conn, queueId, adminId);
            Executor executor = executorDao.get(conn,queueId,execId);
            if ( executor == null){
                throw new NonexistentExecutorIdException("wrong executor id " + execId);
            }
            if (executorDao.getByName(conn, newName, queueId) != null){
                throw new NameCollisionException("Executor with" + newName + "name already exist");
            }
            executorDao.updateName(conn, execId, queueId, newName);
            LOGGER.info("Admin {} of queue {} change executor {} name to {}", adminId, queueId, execId, newName);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to change executor name", e);
        }
    }

    @Override
    public List<Queue> getQueuesAround(float longitude, float latitude) throws QueueOrganizerException {
        try (Connection conn = ds.getConnection()) {
            return queueDao.getByDistance(conn, longitude, latitude);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get queues around", e);
        }
    }

    @Override
    public Queue getQueueInfo(long queueId) throws QueueOrganizerException, NonexistentQueueIdException,
            ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            return getQueue(conn, queueId);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get information about queue with " + queueId + " id", e);
        }
    }

    @Override
    public Notify<ClientInQueue> nextClient(long queueId, long execId) throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException, WrongTimeException, NonexistentExecutorIdException, WrongWorkingTimeException,
            InactiveExecutorException, WaitingException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            Queue queue = getQueue(conn, queueId);
            Executor executor = executorDao.get(conn, queueId, execId);
            if ( executor == null){
                throw new NonexistentExecutorIdException("Wrong executor id " + execId);
            }
            if (executor.isInvalid()){
                throw new AccessException("Client with id " + execId + " does not have executor's rights");
            }
            if (executor.isWaiting()){
                throw new WaitingException("Executor " + execId + " already waiting for client");
            }
            if(!executor.isActiveNow()){
                throw new InactiveExecutorException("Executor " + execId + " is not in executor mode");
            }
            conn.setAutoCommit(false);
            try {
                if (executor.isServeClient()){
                    clientInQueueDao.completeByExecutor(conn, executor.getServingClient(), queueId,
                            new Timestamp(System.currentTimeMillis()));
                    executorDao.updateServeStatus(conn,execId,queueId, false);
                    LOGGER.info("Executor {} served client {} in queue {}", execId, executor.getServingClient(), queueId);
                }
                if (!PeriodAndWorkHours.checkWorkHours(currentTime, queue.getWorkHours())){
                    throw new WrongWorkingTimeException("Executor " + execId +  "try to work in queue" + queue +
                      " at wrong time " + currentTime + ", while work hours " + queue.getWorkHours());
                }
                if (currentTime.before(queue.getStartTime()) || currentTime.after(queue.getEndTime())) {
                    throw new WrongTimeException("Executor " + execId +  " try to work in queue " + queueId +
                        " at wrong time " + currentTime + ", while working time between " + queue.getStartTime() +
                        " and " + queue.getEndTime());
                }
                List<ClientInQueue> nextClients = clientInQueueDao.getNextClients(conn, queueId);
                Notify<ClientInQueue> returnedNotify = null;
                // если больше нет свободных клиентов, то выставляет статус ожидания у исполнителя
                if(nextClients.isEmpty()){
                    executorDao.setWaitingStatus(conn, execId, queueId);
                } else {
                    //первый клиент из списка является текущим клиентом
                    ClientInQueue currentClient = nextClients.get(0);
                    executorDao.updateServeAndServingClientStatus(conn, execId, queueId, currentClient.getClientId());
                    clientInQueueDao.updateStartTimeAndServedId(conn, currentClient.getClientId(), currentClient.getQueueId(),
                            execId, new Timestamp(System.currentTimeMillis()));
                    returnedNotify = new Notify<>(executor, currentClient);
                    // чтобы записать в структуру данных имя текущего клиента
                    Client client = clientDao.get(conn, nextClients.get(0).getClientId());
                    String clientName = client.getFirstName();
                    if (client.getLastName() != null) {
                        clientName = clientName.concat(" " + client.getLastName());
                    }
                    if (nextClients.size()>1) {
                        returnedNotify.setSecondClient(nextClients.get(1));
                    }
                    returnedNotify.setCurrentClientName(clientName);
                    LOGGER.info("Executor {} started to serve new client {} in queue {}", execId, currentClient.getClientId(),
                            queueId);
                }
                return returnedNotify;
            }
            catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to serve new client in queue " + queueId + " by " + execId, e);
        }
    }

    @Override
    public void enterExecutorMode(long queueId, long execId) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, NonexistentExecutorIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkQueue(conn, queueId);
            Executor executor = executorDao.get(conn, queueId, execId);
            if (executor == null) {
                throw new NonexistentExecutorIdException("Wrong executor id" + execId);
            }
            if (executor.isInvalid()){
                throw new AccessException("client "+ execId +" does not have executor rights");
            }
            if (executor.isActiveNow()){
                return;
            }
            executorDao.updateActiveStatus(conn, execId, queueId, true);
            LOGGER.info("Executor {} entered queue {}", execId, queueId);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to change executor name", e);
        }
    }

    @Override
    public void quitExecutorMode(long queueId, long execId) throws QueueOrganizerException, AccessException,
            NonexistentExecutorIdException, NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            Executor executor = checkAndGetExecutor(conn, queueId, execId);
            if(executor.isServeClient() ){
                clientInQueueDao.completeByExecutor(conn, executor.getServingClient(), queueId,
                        new Timestamp(System.currentTimeMillis()));
                LOGGER.info("Executor {} served client {} in queue {}", execId, executor.getServingClient(), queueId);
            }
            if (!executor.isActiveNow()){
                return;
            }
            executorDao.updateQuitStatuses(conn, execId, queueId);
            LOGGER.info("Executor {} quit queue {}", execId, queueId);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to leave executor mode", e);
        }
    }

    @Override
    public List<ServedClient> getClientsList(long queueId, long execId) throws
            QueueOrganizerException, NonexistentExecutorIdException, AccessException, NonexistentQueueIdException,
            ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkAndGetExecutor(conn, queueId, execId);
            List<ServedClient> clientsList = clientInQueueDao.getServedClients(conn, queueId, execId);
            LOGGER.info("Executor {} gets list of clients who served in queue {}", execId, queueId);
            return clientsList;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get client list", e);
        }
    }

    @Override
    public Notify<Client> getInQueue (long clientId, long queueId) throws OverlimitException, ClosedQueueException,
            QueueOrganizerException, NonexistentQueueIdException, WrongTimeException, RepeatedGetInTheQueueException,
            InactiveQueueException, WrongWorkingTimeException {
        try (Connection conn = ds.getConnection()) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            Queue queue = getQueue(conn, queueId);
            if (!queue.isActive()){
                throw new InactiveQueueException("Client " + clientId + " tried to enter inactive queue " + queueId);
            }
            if (currentTime.before(queue.getStartTime()) || currentTime.after(queue.getEndTime())){
                throw new WrongTimeException("Client " + clientId +  " try to get in queue " + queueId +
                        " at wrong time " + currentTime + ", when working time between " + queue.getStartTime() + " and "
                        + queue.getEndTime());
            }
            if (!PeriodAndWorkHours.checkWorkHours(currentTime, queue.getWorkHours())){
                throw new WrongWorkingTimeException("Client " + clientId +  " try get in queue " + queue +
                        " at wrong time " + currentTime + ", while work hours " + queue.getWorkHours());
            }
            if (clientInQueueDao.getCountByClient(conn, clientId)!=0){
                throw new RepeatedGetInTheQueueException("Client " + clientId +  " already stayed at queue");
            }
            if (clientInQueueDao.getCountByQueue(conn, queueId)>=10000){
                throw new OverlimitException("Active clients in queues with " + queueId +  " id limit reached");
            }
            ClientInQueue newClientInQueue = new ClientInQueue(clientId, queueId);
            conn.setAutoCommit(false);
            try {
                long place = queueDao.get(conn, queueId).getLastPlace()+1;
                newClientInQueue.setPlace(place);
                clientInQueueDao.add(conn, newClientInQueue);
                queueDao.updateLastPlace(conn, queueId, place);
                LOGGER.info("Client {} take a {} place in {} queue", clientId, place, queueId);
                // проверка на исполнителей в режиме ожидания
                Executor waitingExecutor = surfExecutors(conn, queueId, clientId);
                Notify<Client> notify = null;
                // если есть исполнитель, то обновляет информацию у клиента и заполняет структуру данных для исполнителя
                if (waitingExecutor != null) {
                    clientInQueueDao.updateStartTimeAndServedId(conn, clientId, queueId, waitingExecutor.getClientId(),
                            new Timestamp(System.currentTimeMillis()));
                    notify = new Notify<>(waitingExecutor, clientDao.get(conn, clientId));
                    LOGGER.info("Client {} serving by {} executor in {} queue", clientId, waitingExecutor.getClientId(),
                            queueId);
                }
                conn.commit();
                return notify;
            } catch (Exception e){
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new QueueOrganizerException("Client " + clientId + " failed to get in the queue "+ queueId, e);
        }
    }

    @Override
    public ClientInQueue getClientInQueueInfo(long clientId, long queueId) throws QueueOrganizerException,
            NonexistentClientException {
        try (Connection conn = ds.getConnection()){
            ClientInQueue clientInQueue = clientInQueueDao.get(conn, clientId, queueId);
            // null если нет записи о клиенте с незавершенным обслуживанием
            if(clientInQueue == null){
                throw new NonexistentClientException("Client " + clientId + " does not stay in queue "+ queueId);
            }
            return clientInQueue;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get info about client " + clientId + "in queue " + queueId, e);
        }
    }

    @Override
    public void leaveQueue(long queueId,long clientId) throws QueueOrganizerException, NonexistentQueueIdException,
            NonexistentClientException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkQueue(conn, queueId);
            ClientInQueue thisClientInQueue = clientInQueueDao.get(conn, clientId, queueId);
            if (thisClientInQueue == null) {
                throw new NonexistentClientException("Client " + clientId + " does not stay in queue "+ queueId);
            }
            if (thisClientInQueue.getServedById() != 0){
                clientInQueueDao.completeByExecutor(conn, clientId, queueId, new Timestamp(System.currentTimeMillis()));
            } else {
                clientInQueueDao.completeByClient(conn, clientId, queueId);
            }
            LOGGER.info("Client {} left queue {}", clientId, queueId);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to leave queue with " + queueId + " id", e);
        }
    }

    @Override
    public void switchNotification(long queueId, long clientId, boolean status) throws QueueOrganizerException,
            NonexistentClientException, NonexistentQueueIdException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            checkQueue(conn, queueId);
            ClientInQueue thisClientInQueue = clientInQueueDao.get(conn, clientId, queueId);
            if (thisClientInQueue == null) {
                throw new NonexistentClientException("Client " + clientId + " does not stay in queue "+ queueId);
            }
            if (thisClientInQueue.getNotificationStatus() == status){
                return;
            }
            clientInQueueDao.updateNotificationStatus(conn, clientId,queueId, status);
            LOGGER.info("Client {} in queue {} successfully change notification status to {}", clientId, queueId,
                    status);
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to switch notification in queue "+ queueId , e);
        }
    }

    @Override
    public List<Queue> getQueuesListByExecutor(long execId, float longitude, float latitude) throws
            QueueOrganizerException {
        try (Connection conn = ds.getConnection()) {
            List<Queue> queues = queueDao.getQueuesByExecutor(conn, execId, longitude, latitude);
            LOGGER.info("Executor {} requested his queues at point {} {}", execId, longitude, latitude);
            return queues;
        } catch (SQLException e) {
            throw new QueueOrganizerException("Failed to get list of queues by executor " + execId, e);
        }
    }

    private Executor surfExecutors(Connection conn, long queueId, long servingClientId) throws SQLException {
        Executor executor = executorDao.getFreeExecutor(conn, queueId);
        if (executor != null){
            executorDao.updateServeAndServingIdWhileWaiting(conn, executor.getClientId(), queueId,
                    servingClientId);
        }
        return executor;
    }

    private Queue getQueue(Connection conn, long queueId) throws SQLException, NonexistentQueueIdException,
            ClosedQueueException {
        Queue queue = queueDao.get(conn, queueId);
        if (queue == null) {
            throw new NonexistentQueueIdException("Wrong queue id "+queueId);
        }
        if (queue.isInvalid()){
            throw new ClosedQueueException("Queue " + queueId + " already closed");
        }
        return queue;
    }

    private void checkQueue(Connection conn, long queueId) throws NonexistentQueueIdException, ClosedQueueException,
            SQLException {
        Queue queue = queueDao.get(conn, queueId);
        if (queue == null) {
            throw new NonexistentQueueIdException("Wrong queue id "+queueId);
        }
        if (queue.isInvalid()){
            throw new ClosedQueueException("Queue " + queueId + " already closed");
        }
    }

    private Executor checkAndGetExecutor(Connection conn, long queueId, long execId) throws ClosedQueueException,
            SQLException, NonexistentQueueIdException, NonexistentExecutorIdException, AccessException {
        checkQueue(conn, queueId);
        Executor executor = executorDao.get(conn, queueId, execId);
        if (executor == null) {
            throw new NonexistentExecutorIdException("Wrong executor id "+execId);
        }
        if (executor.isInvalid()){
            throw new AccessException("client " + execId + " does not have executor rights");
        }
        return executor;
    }

    private Queue checkAndGetQueueByAdmin(Connection conn, long queueId, long adminId) throws SQLException,
            NonexistentQueueIdException, ClosedQueueException, AccessException {
        Queue queue = queueDao.get(conn, queueId);
        if (queue == null) {
            throw new NonexistentQueueIdException("Wrong queue id "+queueId);
        }
        if (queue.isInvalid()){
            throw new ClosedQueueException("Queue " + queueId + " already closed");
        }
        if (adminId != queue.getAdmin()) {
            throw new AccessException ("client "+ adminId +" does not have admin rights");
        }
        return queue;
    }
}
