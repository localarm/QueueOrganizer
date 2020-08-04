import com.pavel.queueorganizer.*;
import com.pavel.queueorganizer.Queue;
import com.pavel.queueorganizer.dao.ClientDAO;
import com.pavel.queueorganizer.dao.ExecutorDAO;
import com.pavel.queueorganizer.dao.QueueDAO;
import com.pavel.queueorganizer.dao.postgres.PostgresClientDAO;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.*;

public class QueueOrganizerApiTest {
    private QueueOrganizerApi queueOrganizerApi;
    private ClientDAO clientDAO;
    private DerbyClientInQueueDAO clientInQueueDAO;
    private ExecutorDAO executorDAO;
    private QueueDAO queueDAO;
    private EmbeddedDataSource ds;
    private static final long TESTADMINID = 1234;
    private static final long TESTADMINID2 = 2345;
    private static final long CLOSEDQUEUEADMINID = 1233;
    private static final long WRONGADMINID = 99999;
    private static final long QUITEXECUTORMODEEXECUTORID = 80005;
    private static final String TESTADMINFIRSTNAME2 = "anotherTest";
    private static final String TESTADMINLASTNAME2 = "admin";
    private static final long TESTCLIENTID1 = 1235;
    private static final long TESTCLIENTID2 = 1236;
    private static final long TESTCLIENTID3 = 1237;
    private static final long TESTQUEUEID = 1;
    private static final String TESTQUEUENAME = "testQueue";
    private static final long WRONGQUEUEID = 99999;
    private static final long TESTEXECUTORID = 4321;
    private static final String TESTEXECUTORNAME = "Test Executor";
    private static final long TESTEXECUTORID2 = 4322;
    private static final String TESTEXECUTORNAME2 = "Another Executor";
    private static final String NEWTESTEXECUTORNAME2 = "NewName Executor";
    private static final long WRONGEXECUTORID = 999999999;
    private static final float TESTLONGITUDEQUEUE1= 4321;//
    private static final float TESTLATITUDEQUEUE1= 4321;//
    private static final float TESTLATITUDE1= 4321;// для проверки, если человек близко
    private static final float TESTLONGITUDE1= 4321;//
    private long closedQueueId;

    @Before
    public void init() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Reader sqlReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass()
                .getClassLoader().getResourceAsStream("initTest.sql"))));
        ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:myDB");
        ds.setCreateDatabase("create");
        ScriptRunner sr = new ScriptRunner(ds.getConnection());
        sr.runScript(sqlReader);
        sr.closeConnection();
        clientDAO = new PostgresClientDAO();
        clientInQueueDAO = new DerbyClientInQueueDAO();
        executorDAO = new DerbyExecutorDAO();
        queueDAO = new DerbyQueueDAO(true);
        queueOrganizerApi = new QueueOrganizerApiImpl(ds, queueDAO, clientDAO, executorDAO, clientInQueueDAO);
    }

    @After
    public void closeDB() {
        ds.setShutdownDatabase("shutdown");
        try {
            ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test() throws SQLException, OverlimitException, QueueOrganizerException, AccessException,
            NameCollisionException, NonexistentQueueIdException, NonexistentExecutorIdException, WrongTimeException,
            WrongLocationException, WrongWorkingTimeException, RepeatedGetInTheQueueException, InactiveQueueException,
            InactiveExecutorException, NonexistentClientException, NameCharsLimitException, WaitingException,
            ClosedQueueException {
        addClientTest();
        createQueueTest();
        getQueuesByAdminTest();
        createQueueNameCollisionExceptionTest();
        createQueueOverLimitExceptionTest();
        createQueueNameCharsLimitException();
        closeClosedQueueTest();
        getInQueueTest();
        getInQueueRepeatedGetInTheQueueExceptionTest();
        getInQueueNonexistentQueueIdExceptionTest();
        getInClosedQueueTest();
        closeQueueTest();
        closeQueueAccessExceptionTest();
        closeQueueWithWrongQueueIdTest();
        addExecutorTest();
        getExecListTest();
        getExecListAccessExceptionTest();
        getExecListNonexistentQueueIdExceptionTest();
        getExecListInClosedQueueTest();
        addExecutorWithWrongAdminIdTest();
        addExecutorWithWrongQueueIdTest();
        addExecutorWithSameNameTest();
        addExecutorOverlimitExceptionTest();
        addExecutorNameCharsLimitException();
        addExecutorInClosedQueueText();
        fireExecutorTest();
        fireExecutorAccessExceptionTest();
        fireExecutorNonexistentExecutorIdExceptionTest();
        fireExecutorNonexistentQueueIdExceptionTest();
        readdExecutorTest();
        fireExecutorInClosedQueueTest();
        switchQueueActiveStatusTest();
        getInQueueInactiveQueueExceptionTest();
        switchQueueActiveStatusNonexistentQueueIdExceptionTest();
        switchQueueActiveStatusAccessExceptionTest();
        switchQueueActiveStatusInClosedQueueTest();
        changeExecNameTest();
        changeExecNameNameCharsLimitExceptionTest();
        changeExecNameAccessExceptionTest();
        changeExecNameWithCollisionsTest();
        changeExecNameNonexistentExecutorIdExceptionTest();
        changeExecNameNonexistentQueueExceptionTest();
        changeExecNameInClosedQueueTest();
        getQueueInfoTest();
        getQueueInfoNonexistentQueueIdExceptionTest();
        getClosedQueueInfoTest();
        enterExecutorModeTest();
        enterExecutorModeAccessExceptionTest();
        enterExecutorNonexistentExecutorIdExceptionTest();
        enterExecutorNonexistentQueueIdExceptionTest();
        enterExecutorModeInClosedQueueTest();
        quitExecutorModeTest();
        quitExecutorModeWithClientTest();
        quitExecutorModeNonexistentQueueIdExceptionTest();
        quitExecutorModeNonexistentExecutorIdExceptionTest();
        quitExecutorModeAccessExceptionTest();
        quitExecutorModeInClosedQueue();
        nextClientTest();
        nextClientInactiveExecutorExceptionTest();
        nextClientAccessExceptionTest();
        nextClientNonexistentExecutorIdExceptionTest();
        nextClientNonexistentQueueIdExceptionTest();
        nextClientWaitingExceptionTest();
        nextClientInClosedQueueTest();
        getClientsListTest();
        getInQueueNonexistentQueueIdExceptionTest();
        getClientsListAccessExceptionTest();
        getClientsListNonexistentExecutorIdExceptionTest();
        getClientsListNonexistentQueueIdExceptionTest();
        getClientsListInClosedQueueTest();
        leaveQueueBeforeServingTest();
        leaveQueueWithServingTest();
        leaveQueueNonexistentClientIdExceptionTest();
        leaveQueueNonexistentQueueIdExceptionTest();
        leaveClosedQueueTest();
        getQueueAroundTest();
        switchNotificationTest();
        switchNotificationNonexistentClientIdExceptionTest();
        switchNotificationNonexistentQueueIdExceptionTest();
        switchNotificationStatusInClosedQueueTest();
        fireExecutorWithClientTest();
        nextClientNotificationTest();
        closeQueueNotificationsTest();
        getInQueueOverlimitExceptionTest();
        getQueuesListByExecutorTest();
        getInQueueMultipleTimeTest();
    }

    private void addClientTest() throws SQLException, QueueOrganizerException {
        queueOrganizerApi.addClient(TESTADMINID, "test", "admin");
        try (Connection conn = ds.getConnection()) {
            Client returnedClient = clientDAO.get(conn, TESTADMINID);
        Assert.assertEquals("test", returnedClient.getFirstName());
        }
    }

    private void createQueueTest() throws SQLException, OverlimitException, QueueOrganizerException,
            NameCollisionException, NameCharsLimitException {
        queueOrganizerApi.createQueue(TESTQUEUENAME,"test","admin",
                TESTLATITUDE1,TESTLONGITUDE1, TESTADMINID);
        try (Connection conn = ds.getConnection()) {
            Queue testQueue = queueDAO.get(conn,TESTQUEUEID);
            Assert.assertEquals("testQueue", testQueue.getName());
        }
    }

    private void getQueuesByAdminTest() throws QueueOrganizerException {
        List<Queue> testList = queueOrganizerApi.getQueuesByAdmin(TESTADMINID);
        Assert.assertEquals(TESTQUEUEID, testList.get(0).getId());
        Assert.assertEquals(1, testList.size());
    }

    private void createQueueNameCharsLimitException() throws NameCollisionException, OverlimitException,
            QueueOrganizerException {
        StringBuilder s = new StringBuilder();
        for (int i = 0;i < 9; i++){
            s.append("testtest");
        }
        try {
            queueOrganizerApi.createQueue(s.toString(), TESTADMINFIRSTNAME2, TESTADMINLASTNAME2, TESTLONGITUDE1,
                    TESTLATITUDE1, TESTADMINID);
        } catch (NameCharsLimitException ignore) {}
    }

    private void createQueueNameCollisionExceptionTest() throws OverlimitException, QueueOrganizerException,
            NameCharsLimitException {
        try {
            queueOrganizerApi.createQueue(TESTQUEUENAME, "test", "admin", TESTLONGITUDE1,
                    TESTLATITUDE1, TESTADMINID);
        } catch (NameCollisionException ignore){}
    }

    private void createQueueOverLimitExceptionTest() throws QueueOrganizerException, NameCollisionException,
            NameCharsLimitException {
        try {
            for (int i=1;i<12;i++) {
                queueOrganizerApi.createQueue("testQueue"+i,"test","admin",
                        TESTLONGITUDEQUEUE1,TESTLATITUDEQUEUE1, TESTADMINID);
            }
            Assert.fail("OverLimitException expected to be thrown");
        } catch (OverlimitException ignore){
        }
    }

    private void closeQueueTest() throws OverlimitException, QueueOrganizerException, AccessException, SQLException,
            NonexistentQueueIdException, NameCollisionException, NameCharsLimitException, ClosedQueueException {
        queueOrganizerApi.addClient(TESTADMINID2,TESTADMINFIRSTNAME2,TESTADMINLASTNAME2);
        queueOrganizerApi.createQueue("closingTestQueue",TESTADMINFIRSTNAME2,TESTADMINLASTNAME2,
                1,2, TESTADMINID2);
        try (Connection conn = ds.getConnection()) {
            Queue testQueue = queueOrganizerApi.getQueuesByAdmin(TESTADMINID2).get(0);
            Timestamp beforeTimeCheck = new Timestamp(System.currentTimeMillis());
            queueOrganizerApi.closeQueue(TESTADMINID2, testQueue.getId());
            Assert.assertEquals(queueDAO.getCountByAdmin(conn, TESTADMINID2), 0);
            Queue closedQueue = queueDAO.get(conn, testQueue.getId());
            Timestamp time = closedQueue.getEndTime();
            Timestamp afterTimeCheck = new Timestamp(System.currentTimeMillis());
            if (time.before(beforeTimeCheck) || time.after(afterTimeCheck)) {
                Assert.fail("Time check failed, closing time "+ time + " is not between " + beforeTimeCheck + " and "
                        + afterTimeCheck);
            }
            try {
                queueOrganizerApi.getQueueInfo(testQueue.getId());
                Assert.fail("ClosedQueueException expected to be thrown");
            } catch (ClosedQueueException ignore){}
        }

    }

    private void closeQueueAccessExceptionTest() throws QueueOrganizerException, NonexistentQueueIdException,
            ClosedQueueException {
        try {
            queueOrganizerApi.closeQueue(WRONGADMINID,TESTQUEUEID);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){ }
    }


    private void closeQueueWithWrongQueueIdTest() throws AccessException, QueueOrganizerException,
            ClosedQueueException {
        try {
            queueOrganizerApi.closeQueue(TESTADMINID, WRONGQUEUEID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){ }
    }

    private void closeClosedQueueTest() throws QueueOrganizerException, NameCollisionException, OverlimitException,
            NameCharsLimitException,  AccessException, NonexistentQueueIdException {
        queueOrganizerApi.addClient(CLOSEDQUEUEADMINID, "closedQueue", "Admin");
        queueOrganizerApi.createQueue("ClosedQueue","closedQueue", "Admin",
                TESTLONGITUDE1, TESTLATITUDE1,CLOSEDQUEUEADMINID);
        closedQueueId = queueOrganizerApi.getQueuesByAdmin(CLOSEDQUEUEADMINID).get(0).getId();
        try {
            queueOrganizerApi.closeQueue(CLOSEDQUEUEADMINID, closedQueueId);
            queueOrganizerApi.closeQueue(CLOSEDQUEUEADMINID, closedQueueId);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }


    private void addExecutorTest() throws AccessException, NameCollisionException,
            QueueOrganizerException, OverlimitException, SQLException, NonexistentQueueIdException,
            NameCharsLimitException, ClosedQueueException {
        queueOrganizerApi.addClient(TESTEXECUTORID,"test", "exec");
        queueOrganizerApi.addExecutor(TESTEXECUTORID, TESTADMINID, TESTQUEUEID, TESTEXECUTORNAME);
        try (Connection conn = ds.getConnection()) {
            Assert.assertEquals(TESTEXECUTORNAME, executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID).getName());
        }
    }

    private void addExecutorNameCharsLimitException() throws NameCollisionException, OverlimitException,
            QueueOrganizerException, NonexistentQueueIdException, AccessException, ClosedQueueException {
        StringBuilder s = new StringBuilder();
        for (int i = 0;i < 50; i++){
            s.append("testtesttesttest");
        }
        try {
            queueOrganizerApi.addExecutor(3333, TESTADMINID, TESTQUEUEID, s.toString());
        } catch (NameCharsLimitException ignore) {}
    }

    private void addExecutorWithWrongAdminIdTest() throws QueueOrganizerException, NameCollisionException,
            OverlimitException, NonexistentQueueIdException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.addExecutor(11,WRONGADMINID,TESTQUEUEID,"testExecutor");
            Assert.fail("expected AccessException to be thrown");
        } catch (AccessException ignore) {}
    }

    private void addExecutorWithWrongQueueIdTest() throws QueueOrganizerException, NameCollisionException,
            OverlimitException, AccessException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.addExecutor(11,TESTADMINID,WRONGQUEUEID,"testExecutor");
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore) { }
    }

    private void addExecutorWithSameNameTest() throws AccessException, QueueOrganizerException, OverlimitException,
            NonexistentQueueIdException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.addExecutor(22,TESTADMINID,TESTQUEUEID,TESTEXECUTORNAME);
            Assert.fail("NameCollisionException expected to be thrown");
        } catch (NameCollisionException ignore) {
        }
    }

    private void addExecutorOverlimitExceptionTest() throws AccessException, QueueOrganizerException,
            NonexistentQueueIdException, NameCollisionException, SQLException, NameCharsLimitException,
            ClosedQueueException {
        try {
            try (Connection conn = ds.getConnection()) {
                long QueueId = queueDAO.getAllQueuesByAdmin(conn, TESTADMINID).get(1).getId();
                for (int i = 1; i < 102; i++) {
                    queueOrganizerApi.addExecutor(i, TESTADMINID,  QueueId,TESTEXECUTORNAME + i);
                }
                queueOrganizerApi.addExecutor(11, TESTADMINID, TESTQUEUEID, TESTEXECUTORNAME);
                Assert.fail(" OverlimitException expected to be thrown");
            }
        } catch (OverlimitException ignore) {
        }
    }

    private void addExecutorInClosedQueueText() throws NonexistentQueueIdException, QueueOrganizerException,
            OverlimitException, NameCollisionException, AccessException, NameCharsLimitException {
        try {
            queueOrganizerApi.addExecutor(TESTEXECUTORID, CLOSEDQUEUEADMINID,closedQueueId, "ExceptionTest");
            Assert.fail(" ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void fireExecutorTest() throws QueueOrganizerException, AccessException, SQLException,
            NonexistentQueueIdException, NonexistentExecutorIdException, ClosedQueueException {
        queueOrganizerApi.fireExecutor(TESTEXECUTORID,TESTADMINID,TESTQUEUEID);
        try (Connection conn = ds.getConnection()) {
            Assert.assertTrue(executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID).isInvalid());
        }
    }

    private void fireExecutorAccessExceptionTest() throws NonexistentExecutorIdException,
            NonexistentQueueIdException, QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID, WRONGADMINID, TESTQUEUEID);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){ }
    }

    private void fireExecutorNonexistentQueueIdExceptionTest() throws QueueOrganizerException,
            NonexistentExecutorIdException, AccessException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID, TESTADMINID, WRONGQUEUEID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){ }
    }

    private void fireExecutorNonexistentExecutorIdExceptionTest() throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(WRONGEXECUTORID, TESTADMINID, TESTQUEUEID);
            Assert.fail("NonexistentExecutorIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore){ }
    }

    private void readdExecutorTest() throws AccessException, NameCollisionException, QueueOrganizerException,
            OverlimitException, SQLException, NonexistentQueueIdException, NameCharsLimitException,
            ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.addExecutor(TESTEXECUTORID, TESTADMINID, TESTQUEUEID, TESTEXECUTORNAME);
            Assert.assertFalse(executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID).isInvalid());
        }
    }

    private void fireExecutorInClosedQueueTest() throws NonexistentExecutorIdException, AccessException,
            NonexistentQueueIdException, QueueOrganizerException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID, CLOSEDQUEUEADMINID,closedQueueId);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void fireExecutorWithClientTest() throws SQLException, RepeatedGetInTheQueueException,
            NonexistentQueueIdException, InactiveQueueException, QueueOrganizerException, OverlimitException,
            WrongWorkingTimeException, WrongLocationException, WrongTimeException, NonexistentExecutorIdException,
            AccessException, InactiveExecutorException, NameCollisionException, WaitingException,
            NameCharsLimitException, ClosedQueueException {
        try(Connection conn = ds.getConnection()){
            queueOrganizerApi.addClient(14000,"test", "14000");
            queueOrganizerApi.createQueue("TestTestTest","Admin", "Test",
                    TESTLONGITUDE1, TESTLATITUDE1, TESTADMINID2);
            long queueId = queueOrganizerApi.getQueuesByAdmin(TESTADMINID2).get(0).getId();
            queueOrganizerApi.getInQueue(14000,queueId);
            queueOrganizerApi.addExecutor(TESTEXECUTORID, TESTADMINID2, queueId,  "FiredTest");
            queueOrganizerApi.enterExecutorMode(queueId, TESTEXECUTORID);
            queueOrganizerApi.nextClient(queueId, TESTEXECUTORID);
            queueOrganizerApi.fireExecutor(TESTEXECUTORID, TESTADMINID2, queueId);
            ClientInQueue testClient =  clientInQueueDAO.getCompleted(conn, 14000, queueId);
            Assert.assertTrue(testClient.isComplete());
            Assert.assertEquals(TESTEXECUTORID, testClient.getServedById());
        }
    }

    private void getExecListTest() throws QueueOrganizerException, AccessException, NonexistentQueueIdException,
            ClosedQueueException {
        long[] expectedExecutorsId =new long[] {TESTADMINID,TESTEXECUTORID};
        List<Executor> testExecutors = queueOrganizerApi.getExecList(TESTADMINID,TESTQUEUEID);
        long[] actualExecutorsId = testExecutors.stream().mapToLong(Executor::getClientId).sorted().toArray();
        Assert.assertArrayEquals(expectedExecutorsId,actualExecutorsId);
    }

    private void getExecListAccessExceptionTest() throws NonexistentQueueIdException, QueueOrganizerException,
            ClosedQueueException {
        try {
            queueOrganizerApi.getExecList(WRONGADMINID, TESTQUEUEID);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore) { }
    }

    private void getExecListNonexistentQueueIdExceptionTest() throws QueueOrganizerException, AccessException,
            ClosedQueueException {
        try {
            queueOrganizerApi.getExecList(TESTADMINID,WRONGQUEUEID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore) {
        }
    }

    private void getExecListInClosedQueueTest() throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException {
        try {
            queueOrganizerApi.getExecList(CLOSEDQUEUEADMINID, closedQueueId);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void switchQueueActiveStatusTest() throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException, SQLException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.switchQueueActiveStatus(TESTADMINID, TESTQUEUEID, false);
            boolean actual = queueDAO.get(conn, TESTQUEUEID).isActive();
            Assert.assertFalse(actual);
            queueOrganizerApi.switchQueueActiveStatus(TESTADMINID, TESTQUEUEID, true);
        }
    }

    private void switchQueueActiveStatusAccessExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.switchQueueActiveStatus(WRONGADMINID, TESTQUEUEID, false);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){}
    }

    private void switchQueueActiveStatusNonexistentQueueIdExceptionTest() throws QueueOrganizerException,
            AccessException, ClosedQueueException {
        try {
            queueOrganizerApi.switchQueueActiveStatus(TESTADMINID, WRONGQUEUEID, false);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void switchQueueActiveStatusInClosedQueueTest() throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException {
        try {
            queueOrganizerApi.switchQueueActiveStatus(CLOSEDQUEUEADMINID,closedQueueId, true);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void changeExecNameTest() throws SQLException, AccessException, NameCollisionException,
            QueueOrganizerException, OverlimitException, NonexistentQueueIdException, NonexistentExecutorIdException,
            NameCharsLimitException, ClosedQueueException {
        queueOrganizerApi.addClient(TESTEXECUTORID2,"Another", "Executor");
        queueOrganizerApi.addExecutor(TESTEXECUTORID2,TESTADMINID,TESTQUEUEID,TESTADMINFIRSTNAME2);
        queueOrganizerApi.changeExecName(TESTEXECUTORID2, TESTQUEUEID, TESTADMINID,NEWTESTEXECUTORNAME2);
        try(Connection conn = ds.getConnection()){
            Executor executor = executorDAO.get(conn,TESTQUEUEID,TESTEXECUTORID2);
            Assert.assertEquals(NEWTESTEXECUTORNAME2, executor.getName());
        }
    }

    private  void changeExecNameNameCharsLimitExceptionTest() throws NonexistentExecutorIdException, NonexistentQueueIdException,
            NameCollisionException, QueueOrganizerException, AccessException, ClosedQueueException {
        try {
            StringBuilder s = new StringBuilder();
            for (int i = 0;i < 50; i++){
                s.append("testtesttesttest");
            }
            queueOrganizerApi.changeExecName(TESTEXECUTORID2,TESTQUEUEID,WRONGADMINID,s.toString());
            Assert.fail("NameCharsLimitException expected to be thrown");
        } catch (NameCharsLimitException ignore){}
    }

    private  void changeExecNameAccessExceptionTest() throws NonexistentExecutorIdException, NonexistentQueueIdException,
            NameCollisionException, QueueOrganizerException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.changeExecName(TESTEXECUTORID2,TESTQUEUEID,WRONGADMINID,TESTEXECUTORNAME2);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){}
    }

    private void changeExecNameNonexistentQueueExceptionTest() throws NonexistentExecutorIdException, AccessException,
            NameCollisionException, QueueOrganizerException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.changeExecName(TESTEXECUTORID2,WRONGQUEUEID,TESTADMINID,TESTEXECUTORNAME2);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore) {}
    }

    private void changeExecNameNonexistentExecutorIdExceptionTest() throws AccessException, NameCollisionException,
            QueueOrganizerException, NonexistentQueueIdException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.changeExecName(WRONGEXECUTORID,TESTQUEUEID,TESTADMINID,TESTEXECUTORNAME2);
            Assert.fail("NonexistentExecutorIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore){}
    }

    private void changeExecNameWithCollisionsTest() throws NonexistentExecutorIdException, AccessException,
            NonexistentQueueIdException, QueueOrganizerException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.changeExecName(TESTEXECUTORID2,TESTQUEUEID,TESTADMINID,TESTEXECUTORNAME);
            Assert.fail("NameCollisionException expected to be thrown");
        } catch (NameCollisionException ignore){}
    }

    private void changeExecNameInClosedQueueTest() throws NonexistentQueueIdException, QueueOrganizerException,
            NameCollisionException, AccessException, NonexistentExecutorIdException, NameCharsLimitException {
        try {
            queueOrganizerApi.changeExecName(TESTEXECUTORID,closedQueueId,CLOSEDQUEUEADMINID,"ne vajno");
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void getQueueInfoTest() throws NonexistentQueueIdException, QueueOrganizerException, ClosedQueueException {
        String actual = queueOrganizerApi.getQueueInfo(TESTQUEUEID).getName();
        Assert.assertEquals(TESTQUEUENAME,actual);
    }

    private void getQueueInfoNonexistentQueueIdExceptionTest() throws QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.getQueueInfo(WRONGQUEUEID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void getClosedQueueInfoTest() throws NonexistentQueueIdException, QueueOrganizerException {
        try {
            queueOrganizerApi.getQueueInfo(closedQueueId);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void enterExecutorModeTest() throws AccessException, NonexistentQueueIdException,
            QueueOrganizerException, SQLException, WrongLocationException, NonexistentExecutorIdException,
            ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID, TESTEXECUTORID);
            boolean actual =  executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID).isActiveNow();
            Assert.assertTrue(actual);
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID, TESTEXECUTORID2);
            actual =  executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID2).isActiveNow();
            Assert.assertTrue(actual);
        }
    }

    private void enterExecutorModeAccessExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, WrongLocationException, NonexistentExecutorIdException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID2, TESTADMINID, TESTQUEUEID);
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID, TESTEXECUTORID2);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){}
    }

    private void enterExecutorNonexistentQueueIdExceptionTest() throws AccessException,
            QueueOrganizerException, WrongLocationException, NonexistentExecutorIdException, ClosedQueueException {
        try {
            queueOrganizerApi.enterExecutorMode(WRONGQUEUEID, TESTEXECUTORID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void enterExecutorNonexistentExecutorIdExceptionTest() throws AccessException,
            QueueOrganizerException, WrongLocationException, NonexistentQueueIdException, ClosedQueueException {
        try {
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID, WRONGEXECUTORID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore){}
    }

    private void enterExecutorModeInClosedQueueTest() throws NonexistentExecutorIdException, AccessException,
            NonexistentQueueIdException, QueueOrganizerException, WrongLocationException {
        try {
            queueOrganizerApi.enterExecutorMode(closedQueueId, TESTEXECUTORID);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void quitExecutorModeTest() throws QueueOrganizerException, NameCollisionException, OverlimitException,
            NameCharsLimitException, NonexistentExecutorIdException, AccessException, NonexistentQueueIdException,
            WrongLocationException, SQLException, ClosedQueueException {
        queueOrganizerApi.addClient(QUITEXECUTORMODEEXECUTORID, "quit", " test");
        queueOrganizerApi.createQueue("quitExecutorModeTest", " quit", "test",
                TESTLONGITUDE1, TESTLATITUDE1,QUITEXECUTORMODEEXECUTORID);
        long queueId = queueOrganizerApi.getQueuesByAdmin(QUITEXECUTORMODEEXECUTORID).get(0).getId();
        queueOrganizerApi.enterExecutorMode(queueId, QUITEXECUTORMODEEXECUTORID);
        queueOrganizerApi.quitExecutorMode(queueId, QUITEXECUTORMODEEXECUTORID);
        try (Connection conn = ds.getConnection()){
            Assert.assertFalse(executorDAO.get(conn, queueId, QUITEXECUTORMODEEXECUTORID).isActiveNow());
        }
    }

    private void quitExecutorModeWithClientTest() throws QueueOrganizerException, NonexistentExecutorIdException,
            AccessException, NonexistentQueueIdException, WrongLocationException, WrongTimeException,
            InactiveQueueException, RepeatedGetInTheQueueException, WrongWorkingTimeException, OverlimitException,
            InactiveExecutorException, WaitingException, SQLException, ClosedQueueException {
        long queueId = queueOrganizerApi.getQueuesByAdmin(QUITEXECUTORMODEEXECUTORID).get(0).getId();
        queueOrganizerApi.enterExecutorMode(queueId,QUITEXECUTORMODEEXECUTORID);
        queueOrganizerApi.addClient(80006, "quit", "test");
        queueOrganizerApi.getInQueue(80006, queueId);
        queueOrganizerApi.nextClient(queueId, QUITEXECUTORMODEEXECUTORID);
        queueOrganizerApi.quitExecutorMode(queueId, QUITEXECUTORMODEEXECUTORID);
        try (Connection conn = ds.getConnection()){
            Assert.assertNull( clientInQueueDAO.get(conn, 80006, queueId));
        }
    }

    private void quitExecutorModeNonexistentQueueIdExceptionTest() throws NonexistentExecutorIdException,
            QueueOrganizerException, AccessException, ClosedQueueException {
        try {
            queueOrganizerApi.quitExecutorMode(WRONGQUEUEID, TESTEXECUTORID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore) {
        }
    }

    private void quitExecutorModeNonexistentExecutorIdExceptionTest() throws AccessException,
            NonexistentQueueIdException, QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.quitExecutorMode(TESTQUEUEID, WRONGEXECUTORID);
            Assert.fail("NonexistentExecutorIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore) { }
    }

    private void quitExecutorModeAccessExceptionTest() throws QueueOrganizerException, NonexistentExecutorIdException,
            AccessException, NonexistentQueueIdException, ClosedQueueException {
        long queueId = queueOrganizerApi.getQueuesByAdmin(QUITEXECUTORMODEEXECUTORID).get(0).getId();
        queueOrganizerApi.fireExecutor(QUITEXECUTORMODEEXECUTORID, QUITEXECUTORMODEEXECUTORID, queueId);
        try {
            queueOrganizerApi.quitExecutorMode(queueId, QUITEXECUTORMODEEXECUTORID);
        } catch (AccessException ignore) { }
    }

    private void quitExecutorModeInClosedQueue() throws NonexistentExecutorIdException, AccessException,
            NonexistentQueueIdException, QueueOrganizerException {
        try {
            queueOrganizerApi.quitExecutorMode(closedQueueId, TESTEXECUTORID);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void getInQueueTest() throws SQLException, RepeatedGetInTheQueueException, NonexistentQueueIdException,
            InactiveQueueException, QueueOrganizerException, OverlimitException, WrongWorkingTimeException,
            WrongTimeException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.addClient(TESTCLIENTID1, "test", "client1");
            queueOrganizerApi.addClient(TESTCLIENTID2, "test", "client2");
            queueOrganizerApi.addClient(TESTCLIENTID3, "test", "client3");
            queueOrganizerApi.getInQueue(TESTCLIENTID1, TESTQUEUEID);
            queueOrganizerApi.getInQueue(TESTCLIENTID2, TESTQUEUEID);
            queueOrganizerApi.getInQueue(TESTCLIENTID3, TESTQUEUEID);
            long actual = clientInQueueDAO.get(conn, TESTCLIENTID1, TESTQUEUEID).getPlace();
            Assert.assertEquals(1, actual);
            Assert.assertEquals(2, clientInQueueDAO.get(conn, TESTCLIENTID2, TESTQUEUEID).getPlace());
            Assert.assertEquals(3, clientInQueueDAO.get(conn, TESTCLIENTID3, TESTQUEUEID).getPlace());
        }
    }

    private void getInQueueRepeatedGetInTheQueueExceptionTest() throws InactiveQueueException,
            NonexistentQueueIdException, QueueOrganizerException, OverlimitException, WrongWorkingTimeException,
            WrongTimeException, ClosedQueueException {
        try {
            queueOrganizerApi.getInQueue(TESTCLIENTID1, TESTQUEUEID);
            Assert.fail("RepeatedGetInTheQueueException expected to be thrown");
        } catch (RepeatedGetInTheQueueException ignore){}
    }

    private void getInQueueNonexistentQueueIdExceptionTest() throws RepeatedGetInTheQueueException,
            InactiveQueueException, QueueOrganizerException, OverlimitException,
            WrongWorkingTimeException, WrongTimeException, ClosedQueueException {
        try {
            queueOrganizerApi.getInQueue(TESTCLIENTID1, WRONGQUEUEID);
            Assert.fail("RepeatedGetInTheQueueException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void getInQueueOverlimitExceptionTest() throws RepeatedGetInTheQueueException,
            NonexistentQueueIdException, InactiveQueueException, QueueOrganizerException,
            WrongWorkingTimeException, WrongTimeException, ClosedQueueException {
        try{
            for (int i = 100000;i<111111; i++) {
                queueOrganizerApi.addClient(i, "name", "client" + i);
                queueOrganizerApi.getInQueue(i, TESTQUEUEID);
            }
            Assert.fail("OverlimitException expected to be thrown");
        } catch (OverlimitException ignore) {}
    }

    private void getInQueueInactiveQueueExceptionTest() throws RepeatedGetInTheQueueException,
            NonexistentQueueIdException, QueueOrganizerException, OverlimitException, WrongWorkingTimeException,
            WrongTimeException, AccessException, ClosedQueueException {
        try{
            queueOrganizerApi.addClient(100000000,"Inactive","test");
            queueOrganizerApi.switchQueueActiveStatus(TESTADMINID, TESTQUEUEID, false);
            queueOrganizerApi.getInQueue(100000000, TESTQUEUEID);
            Assert.fail("InactiveQueueException expected to be thrown");
        } catch (InactiveQueueException ignore) {}
        finally {
            queueOrganizerApi.switchQueueActiveStatus(TESTADMINID, TESTQUEUEID, true);
        }
    }

    private void getInClosedQueueTest() throws RepeatedGetInTheQueueException, NonexistentQueueIdException,
            InactiveQueueException, QueueOrganizerException, OverlimitException, WrongWorkingTimeException,
            WrongTimeException {
        try {
            queueOrganizerApi.getInQueue(1234, closedQueueId);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void nextClientTest() throws NonexistentQueueIdException, QueueOrganizerException,
            InactiveExecutorException, WrongWorkingTimeException, AccessException, NonexistentExecutorIdException,
            WrongTimeException, SQLException, OverlimitException, NameCollisionException, WrongLocationException,
            NameCharsLimitException, WaitingException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.addExecutor(TESTEXECUTORID2,TESTADMINID, TESTQUEUEID, TESTEXECUTORNAME2);
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID,TESTEXECUTORID2);
            queueOrganizerApi.nextClient(TESTQUEUEID, TESTEXECUTORID);
            Executor actual = executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID);
            Assert.assertTrue(actual.isServeClient());
            Assert.assertEquals(TESTCLIENTID1,actual.getServingClient());
            queueOrganizerApi.nextClient(TESTQUEUEID, TESTEXECUTORID2);
            actual = executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID2);
            Assert.assertEquals(TESTCLIENTID2,actual.getServingClient());
            queueOrganizerApi.nextClient(TESTQUEUEID, TESTEXECUTORID);
            Assert.assertEquals(TESTCLIENTID3,executorDAO.get(conn, TESTQUEUEID, TESTEXECUTORID).getServingClient());
            Assert.assertNull(clientInQueueDAO.get(conn, TESTCLIENTID1,TESTQUEUEID));
            queueOrganizerApi.quitExecutorMode(TESTQUEUEID, TESTEXECUTORID);
        }
    }

    private void nextClientNonexistentQueueIdExceptionTest() throws InactiveExecutorException, QueueOrganizerException,
            WrongWorkingTimeException,
            AccessException, NonexistentExecutorIdException, WrongTimeException, WaitingException, ClosedQueueException{
        try {
            queueOrganizerApi.nextClient(WRONGQUEUEID, TESTEXECUTORID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void nextClientInactiveExecutorExceptionTest() throws NonexistentQueueIdException, QueueOrganizerException,
            WrongWorkingTimeException, AccessException, NonexistentExecutorIdException, WrongTimeException,
            WaitingException, ClosedQueueException {
        try {
            queueOrganizerApi.quitExecutorMode(TESTQUEUEID, TESTEXECUTORID2);
            queueOrganizerApi.nextClient(TESTQUEUEID, TESTEXECUTORID2);
            Assert.fail("InactiveExecutorException expected to be thrown");
        } catch (InactiveExecutorException ignore){}
    }

    private void nextClientAccessExceptionTest() throws NonexistentQueueIdException, QueueOrganizerException,
            InactiveExecutorException, WrongWorkingTimeException, NonexistentExecutorIdException, WrongTimeException,
            WaitingException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID2, TESTADMINID, TESTQUEUEID);
            queueOrganizerApi.nextClient(TESTQUEUEID, TESTEXECUTORID2);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){}
    }

    private void nextClientNonexistentExecutorIdExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, InactiveExecutorException, AccessException, WrongWorkingTimeException,
            WrongTimeException, WaitingException, ClosedQueueException {
        try {
            queueOrganizerApi.nextClient(TESTQUEUEID, WRONGEXECUTORID);
            Assert.fail("NonexistentExecutorIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore){}
    }

    private void nextClientWaitingExceptionTest() throws WrongWorkingTimeException, NonexistentQueueIdException,
            QueueOrganizerException, InactiveExecutorException, AccessException, NonexistentExecutorIdException,
            WrongTimeException, NameCollisionException, OverlimitException, NameCharsLimitException,
            WrongLocationException, ClosedQueueException {
        try {
            long adminId = 80000;
            queueOrganizerApi.addClient(adminId, "waiting", "test");
            queueOrganizerApi.createQueue("waitingTestQueue", "waiting", "test",
                    TESTLONGITUDE1, TESTLATITUDE1, adminId);
            long queueId = queueOrganizerApi.getQueuesByAdmin(adminId).get(0).getId();
            queueOrganizerApi.enterExecutorMode(queueId, adminId);
            queueOrganizerApi.nextClient(queueId, adminId);
            queueOrganizerApi.nextClient(queueId, adminId);
            Assert.fail("WaitingException expected to be thrown");
        } catch (WaitingException ignore){}
    }

    private void nextClientInClosedQueueTest() throws WrongWorkingTimeException, NonexistentQueueIdException,
            QueueOrganizerException, InactiveExecutorException, AccessException, WaitingException,
            NonexistentExecutorIdException, WrongTimeException {
        try {
            queueOrganizerApi.nextClient(closedQueueId,TESTEXECUTORID);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void getClientsListTest() throws NonexistentQueueIdException, QueueOrganizerException,
            InactiveExecutorException, WrongWorkingTimeException, AccessException, NonexistentExecutorIdException,
            WrongTimeException, InactiveQueueException, RepeatedGetInTheQueueException, OverlimitException,
            WaitingException, NameCollisionException, NameCharsLimitException, WrongLocationException,
            ClosedQueueException {
        HashSet<String> filterSet = new HashSet<>();
        long executorId = 60050;
        queueOrganizerApi.addClient(executorId,"getClientList","Admin");
        queueOrganizerApi.createQueue("getClientListQueueTest", "getClientList", "Admin",
                TESTLONGITUDE1, TESTLATITUDE1, executorId);
        long queueId = queueOrganizerApi.getQueuesByAdmin(executorId).get(0).getId();
        for (long i = 60000; i<60030; i++ ){
            queueOrganizerApi.addClient(i,"testClientList"+i,"name");
            filterSet.add("testClientList"+ i + " " + "name");
            queueOrganizerApi.getInQueue(i,queueId);
        }
        queueOrganizerApi.enterExecutorMode(queueId, executorId);
        for (int i = 0; i<30; i++ ){
            queueOrganizerApi.nextClient(queueId,executorId);
        }
        queueOrganizerApi.quitExecutorMode(queueId, executorId);
        Assert.assertTrue(queueOrganizerApi.getClientsList(queueId,executorId)
                .stream().map(ServedClient::getName).allMatch(filterSet::contains));
    }

//    private void getClientsListWithTimeTest() throws EmptyReturnedListException, NonexistentQueueIdException,
//            QueueOrganizerException, InactiveExecutorException, WrongWorkingTimeException, AccessException,
//            NonexistentExecutorIdException, WrongTimeException, SQLException, InactiveQueueException,
//            RepeatedGetInTheQueueException, OverlimitException, WrongLocationException {
//        Timestamp time = new Timestamp(System.currentTimeMillis());
//        long[] expected = new long[10];
//        for (long i = 50050; i<50060; i++ ) {
//            queueOrganizerApi.addClient(i,"test"+i,"name");
//            int j =(int) (i - 50050);
//            expected[j]=i;
//            queueOrganizerApi.getInQueue(i,TESTQUEUEID, TESTLONGITUDE1, TESTLATITUDE1);
//        }
//        for (int i = 0; i<10; i++ ){
//            queueOrganizerApi.nextClient(TESTQUEUEID,TESTEXECUTORID);
//        }
//        try {
//            queueOrganizerApi.nextClient(TESTQUEUEID,TESTEXECUTORID);
//        } catch (EmptyReturnedListException ignore){}
//        long[] actual;
//        actual = queueOrganizerApi.getClientsList(TESTQUEUEID,TESTEXECUTORID, time)
//                .stream().mapToLong(ClientInQueue::getClientId).sorted().toArray();
//        Assert.assertArrayEquals(expected, actual);
//    }


    private void getClientsListNonexistentExecutorIdExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, AccessException, ClosedQueueException {
        try {
            queueOrganizerApi.getClientsList(TESTQUEUEID, WRONGEXECUTORID);
            Assert.fail("NonexistentExecutorIdException expected to be thrown");
        } catch (NonexistentExecutorIdException ignore){}

    }

    private void getClientsListNonexistentQueueIdExceptionTest() throws NonexistentExecutorIdException, AccessException,
            QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.getClientsList(WRONGQUEUEID, TESTEXECUTORID);
            Assert.fail("NonexistentQueueIdException expected to be thrown");
        } catch (NonexistentQueueIdException ignore){}
    }

    private void getClientsListAccessExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, NonexistentExecutorIdException, NameCollisionException, OverlimitException,
            AccessException, WrongLocationException, NameCharsLimitException, ClosedQueueException {
        try {
            queueOrganizerApi.fireExecutor(TESTEXECUTORID,TESTADMINID,TESTQUEUEID);
            queueOrganizerApi.getClientsList(TESTQUEUEID, TESTEXECUTORID);
            Assert.fail("AccessException expected to be thrown");
        } catch (AccessException ignore){}
        finally {
            queueOrganizerApi.addExecutor(TESTEXECUTORID,TESTADMINID,TESTQUEUEID,TESTEXECUTORNAME);
            queueOrganizerApi.enterExecutorMode(TESTQUEUEID, TESTEXECUTORID);
        }
    }

    private void getClientsListInClosedQueueTest() throws NonexistentExecutorIdException, AccessException,
            NonexistentQueueIdException, QueueOrganizerException {
        try {
            queueOrganizerApi.getClientsList(closedQueueId, TESTEXECUTORID);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void leaveQueueBeforeServingTest() throws SQLException, RepeatedGetInTheQueueException,
            NonexistentQueueIdException, InactiveQueueException, QueueOrganizerException, OverlimitException,
            WrongWorkingTimeException, WrongTimeException, NonexistentClientException, ClosedQueueException {
        try (Connection conn = ds.getConnection()){
        queueOrganizerApi.addClient(20000, "test", "200000");
        queueOrganizerApi.getInQueue(20000, TESTQUEUEID);
        queueOrganizerApi.leaveQueue(TESTQUEUEID, 20000);
        ClientInQueue clientInQueue = clientInQueueDAO.getCompleted(conn, 20000, TESTQUEUEID);
        Assert.assertNull(clientInQueue.getStartTime());
        Assert.assertNull(clientInQueue.getEndTime());
        Assert.assertTrue(clientInQueue.isComplete());
        }
    }

    private void leaveQueueWithServingTest() throws SQLException, RepeatedGetInTheQueueException,
            NonexistentQueueIdException, InactiveQueueException, QueueOrganizerException, OverlimitException,
            WrongWorkingTimeException, WrongTimeException, NonexistentClientException,
            NonexistentExecutorIdException, AccessException, InactiveExecutorException,
            WaitingException, NameCollisionException, NameCharsLimitException, WrongLocationException, ClosedQueueException {
        try (Connection conn = ds.getConnection()){
            long adminId = 20002;
            long clientId =20001;
            queueOrganizerApi.addClient(clientId, "test", "client");
            queueOrganizerApi.addClient(adminId, "test", "admin");
            queueOrganizerApi.createQueue("leaveQueueTest","test", "admin",
                    TESTLONGITUDE1, TESTLATITUDE1, adminId);
            long queueId = queueOrganizerApi.getQueuesByAdmin(adminId).get(0).getId();
            queueOrganizerApi.enterExecutorMode(queueId,adminId);
            queueOrganizerApi.getInQueue(clientId, queueId);
            queueOrganizerApi.nextClient(queueId,adminId);
            queueOrganizerApi.leaveQueue(queueId, clientId);
            ClientInQueue clientInQueue = clientInQueueDAO.getCompleted(conn, clientId, queueId);
            Assert.assertNotNull(clientInQueue.getStartTime());
            Assert.assertNotNull(clientInQueue.getEndTime());
            Assert.assertTrue(clientInQueue.isComplete());
            Assert.assertEquals(adminId,clientInQueue.getServedById());
        }
    }

    private void leaveQueueNonexistentClientIdExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.leaveQueue(TESTQUEUEID,1000000);
        } catch (NonexistentClientException ignore){}
    }

    private void leaveQueueNonexistentQueueIdExceptionTest() throws QueueOrganizerException,
            NonexistentClientException, ClosedQueueException {
        try {
            queueOrganizerApi.leaveQueue(WRONGQUEUEID, 10000000);
        } catch (NonexistentQueueIdException ignore){}
    }

    private void leaveClosedQueueTest() throws NonexistentClientException, QueueOrganizerException,
            NonexistentQueueIdException {
        try {
            queueOrganizerApi.leaveQueue(closedQueueId, 1234);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void getQueueAroundTest() throws QueueOrganizerException {
        List<Queue> testList = queueOrganizerApi.getQueuesAround(TESTLATITUDE1,TESTLONGITUDE1);
        Assert.assertEquals(14,testList.size());
    }

    private void switchNotificationTest() throws SQLException, RepeatedGetInTheQueueException,
            NonexistentQueueIdException, InactiveQueueException, QueueOrganizerException, OverlimitException,
            WrongWorkingTimeException, WrongTimeException, NonexistentClientException, ClosedQueueException {
        try (Connection conn = ds.getConnection()) {
            queueOrganizerApi.addClient(15000, "test", "15000");
            queueOrganizerApi.getInQueue(15000, TESTQUEUEID);
            ClientInQueue clientInQueue = clientInQueueDAO.get(conn,15000, TESTQUEUEID);
            Assert.assertTrue(clientInQueue.getNotificationStatus());
            queueOrganizerApi.switchNotification(TESTQUEUEID, 15000, false);
            clientInQueue = clientInQueueDAO.get(conn,15000, TESTQUEUEID);
            Assert.assertFalse(clientInQueue.getNotificationStatus());
            queueOrganizerApi.switchNotification(TESTQUEUEID, 15000, true);
            clientInQueue = clientInQueueDAO.get(conn,15000, TESTQUEUEID);
            Assert.assertTrue(clientInQueue.getNotificationStatus());

        }
    }

    private void switchNotificationNonexistentClientIdExceptionTest() throws NonexistentQueueIdException,
            QueueOrganizerException, ClosedQueueException {
        try {
            queueOrganizerApi.switchNotification(TESTQUEUEID,1000000, true);
        } catch (NonexistentClientException ignore){}
    }

    private void switchNotificationNonexistentQueueIdExceptionTest() throws QueueOrganizerException,
            NonexistentClientException, ClosedQueueException {
        try {
            queueOrganizerApi.switchNotification(WRONGQUEUEID, 10000000, true);
        } catch (NonexistentQueueIdException ignore){}
    }

    private void switchNotificationStatusInClosedQueueTest() throws NonexistentQueueIdException,
            QueueOrganizerException, NonexistentClientException {
        try {
            queueOrganizerApi.switchNotification(closedQueueId, 1234, true);
            Assert.fail("ClosedQueueException expected to be thrown");
        } catch (ClosedQueueException ignore) {}
    }

    private void nextClientNotificationTest() throws NameCollisionException, OverlimitException,
            QueueOrganizerException, RepeatedGetInTheQueueException, NonexistentQueueIdException,
            InactiveQueueException, WrongWorkingTimeException, WrongLocationException, WrongTimeException,
            AccessException, NonexistentExecutorIdException, InactiveExecutorException, NameCharsLimitException,
            SQLException, WaitingException, ClosedQueueException {
        queueOrganizerApi.addClient(5000,"test0", "5000");
        queueOrganizerApi.addClient(5001,"test1", "5001");
        queueOrganizerApi.addClient(5002,"test2", "5002");
        queueOrganizerApi.addClient(5003,"test3", "5003");
        queueOrganizerApi.createQueue("closeQueueNotificationsTest", "Close",
                "Queue", TESTLONGITUDE1 ,TESTLATITUDE1, TESTADMINID2);
        long queueId = queueOrganizerApi.getQueuesByAdmin(TESTADMINID2).get(1).getId();
        queueOrganizerApi.getInQueue(5000,queueId);
        queueOrganizerApi.getInQueue(5001,queueId);
        queueOrganizerApi.getInQueue(5002,queueId);
        queueOrganizerApi.getInQueue(5003,queueId);
        queueOrganizerApi.addExecutor(TESTEXECUTORID, TESTADMINID2, queueId, "NotifyTest");
        queueOrganizerApi.enterExecutorMode(queueId, TESTEXECUTORID);
        Notify<ClientInQueue> firsNotify =  queueOrganizerApi.nextClient(queueId, TESTEXECUTORID);
        Assert.assertEquals("NotifyTest", firsNotify.getExecutor().getName());
        Assert.assertEquals(5000,firsNotify.getCurrentClient().getClientId());
        Assert.assertEquals("test0 5000", firsNotify.getCurrentClientName());
        Assert.assertEquals(5001, firsNotify.getSecondClient().getClientId());
        Notify<ClientInQueue> secondNotify =  queueOrganizerApi.nextClient(queueId, TESTEXECUTORID);
        Assert.assertEquals("NotifyTest", secondNotify.getExecutor().getName());
        Assert.assertEquals(5001,secondNotify.getCurrentClient().getClientId());
        Assert.assertEquals("test1 5001", secondNotify.getCurrentClientName());
        Assert.assertEquals(5002, secondNotify.getSecondClient().getClientId());
        queueOrganizerApi.quitExecutorMode(queueId, TESTEXECUTORID);
        try (Connection conn = ds.getConnection()){
            Assert.assertNull(clientInQueueDAO.get(conn, 5001, queueId));
        }

    }

    private void closeQueueNotificationsTest() throws QueueOrganizerException, NonexistentQueueIdException,
            NonexistentClientException, AccessException, ClosedQueueException {
        long queueId = queueOrganizerApi.getQueuesByAdmin(TESTADMINID2).get(1).getId();
        queueOrganizerApi.switchNotification(queueId, 5002, false);
        List<ClientInQueue> notificationTestList =  queueOrganizerApi.closeQueue(TESTADMINID2, queueId);
        Assert.assertFalse(notificationTestList.get(0).getNotificationStatus());
        Assert.assertTrue(notificationTestList.get(1).getNotificationStatus());
    }

    private void getQueuesListByExecutorTest() throws QueueOrganizerException{
        List<Queue> queues = queueOrganizerApi.getQueuesListByExecutor(TESTEXECUTORID, TESTLONGITUDE1, TESTLATITUDE1);
        Assert.assertEquals(1, queues.size());
        Assert.assertEquals(1, queues.get(0).getId());
    }

    private void getInQueueMultipleTimeTest() throws NameCharsLimitException, NameCollisionException,
            QueueOrganizerException, OverlimitException, WrongTimeException, NonexistentQueueIdException,
            RepeatedGetInTheQueueException, InactiveQueueException, WrongWorkingTimeException,
            NonexistentExecutorIdException, WrongLocationException, AccessException, InactiveExecutorException,
            WaitingException, SQLException, ClosedQueueException {
        long clientId = 80010;
        long adminId =  80011;
        queueOrganizerApi.addClient(clientId,"client", "MultipleGetInQueue");
        queueOrganizerApi.addClient(adminId,"test", "admin");
        queueOrganizerApi.createQueue("getInQueueMultipleTimeTest", "Close",
                "Queue", TESTLONGITUDE1 ,TESTLATITUDE1, adminId);
        long queueId = queueOrganizerApi.getQueuesByAdmin(adminId).get(0).getId();
        Notify<Client> firstNotify = queueOrganizerApi.getInQueue(clientId,queueId);
        Assert.assertNull(firstNotify);
        queueOrganizerApi.enterExecutorMode(queueId, adminId);
        queueOrganizerApi.nextClient(queueId, adminId);
        queueOrganizerApi.nextClient(queueId, adminId);
        try (Connection conn = ds.getConnection()) {
            Assert.assertNull(clientInQueueDAO.get(conn, clientId, queueId));
            Notify<Client> secondNotify = queueOrganizerApi.getInQueue(clientId, queueId);
            Assert.assertEquals(clientId,clientInQueueDAO.get(conn, clientId, queueId).getClientId());
            Assert.assertEquals(adminId, secondNotify.getExecutor().getClientId());
            Assert.assertEquals(clientId, secondNotify.getCurrentClient().getId());
            queueOrganizerApi.quitExecutorMode(queueId, adminId);
            Assert.assertNull(clientInQueueDAO.get(conn, clientId, queueId));
        }
    }
}
