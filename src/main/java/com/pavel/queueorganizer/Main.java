package com.pavel.queueorganizer;
import com.pavel.queueorganizer.dao.ClientDAO;
import com.pavel.queueorganizer.dao.ClientInQueueDAO;
import com.pavel.queueorganizer.dao.ExecutorDAO;
import com.pavel.queueorganizer.dao.QueueDAO;
import com.pavel.queueorganizer.dao.postgres.PostgresClientDAO;
import com.pavel.queueorganizer.dao.postgres.PostgresClientInQueueDAO;
import com.pavel.queueorganizer.dao.postgres.PostgresExecutorDAO;
import com.pavel.queueorganizer.dao.postgres.PostgresQueueDAO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;

public class Main {
    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    public static void main(String[] args) {
        try {
            Properties appProperties = new Properties();
            appProperties.load(new BufferedInputStream(new FileInputStream("application.properties")));
            String botToken = checkIsEmpty(appProperties.getProperty("botToken"));
            String botUsername = checkIsEmpty(appProperties.getProperty("botUsername"));
            String jdbcUrl = checkIsEmpty(appProperties.getProperty("jdbcUrl"));
            String username = checkIsEmpty(appProperties.getProperty("username"));
            String password = checkIsEmpty(appProperties.getProperty("password"));
            boolean ignoreDistance = Boolean.parseBoolean(checkIsEmpty(appProperties.getProperty("ignoreDistance")));
            int maxPoolSize = Integer.parseInt(checkIsEmpty(appProperties.getProperty("maxPoolSize")));
            long timeOutMillis = Long.parseLong(checkIsEmpty(appProperties.getProperty("timeOutMillis")));
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(maxPoolSize);
            HikariDataSource dataSource = new HikariDataSource(config);
            ClientDAO clientDAO = new PostgresClientDAO();
            ClientInQueueDAO clientInQueueDAO = new PostgresClientInQueueDAO();
            QueueDAO queueDAO = new PostgresQueueDAO(ignoreDistance);
            ExecutorDAO executorDAO = new PostgresExecutorDAO();
            QueueOrganizerApi queueOrganizerApi = new QueueOrganizerApiImpl(dataSource, queueDAO, clientDAO,
                    executorDAO, clientInQueueDAO);
            ApiContextInitializer.init();
            TelegramBotsApi telegram = new TelegramBotsApi();
            Bot bot = new Bot(queueOrganizerApi, botToken, botUsername);
            try {
                BotSession botSession = telegram.registerBot(bot);
                Runtime.getRuntime().addShutdownHook(new Thread(()-> {
                    LOGGER.info("ShutdownHook activated");
                    botSession.stop();
                    for (int loopTime= 0; botSession.isRunning() && timeOutMillis-loopTime > 0; loopTime=+100) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }));
            } catch (TelegramApiRequestException e) {
                LOGGER.error(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Properties file not found in main directory", e);
            System.exit(2);
        } catch (InvalidParameterException | NumberFormatException e){
            LOGGER.error("Missing or wrong values in properties file", e);
            System.exit(3);
        }
    }

    static String checkIsEmpty(String value) {
        if (value == null || value.isEmpty() || value.trim().isEmpty()) {
            throw new InvalidParameterException("Missing value");
        } else {
            return value;
        }
    }

}
