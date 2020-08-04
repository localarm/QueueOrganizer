package com.pavel.queueorganizer.dao;

import com.pavel.queueorganizer.ClientInQueue;
import com.pavel.queueorganizer.ServedClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface ClientInQueueDAO {

    /**добавляет запись о клиенте, находящимся в очереди в таблицу client_queue
     * @param clientInQueue клиент, находящийся в очереди
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void add(Connection conn, ClientInQueue clientInQueue) throws SQLException;

    /**Возвращает запись о клиенте, находящемся в очереди, из таблицы client_queue по id клиента и id очереди
     * @param clientId  id клиента
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return клиента в очереди, либо null, если нет записи, удовлетворяющей условию
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    ClientInQueue get(Connection conn, long clientId, long queueId) throws SQLException;

    /**Вовзращает количество строк в таблице client_queue в которых столбец queue_id равен передаваемому queueId
     * и complete = false
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return количество совпадений
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    int getCountByQueue(Connection conn, long queueId) throws SQLException;

    /**Вовзращает количество строк в таблице client_queue в которых столбец client_id равен передаваемому clientId
     * и complete = false
     * @param clientId id клиента
     * @param conn Соединение с базой данных
     * @return количество совпадений
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    int getCountByClient(Connection conn, long clientId) throws SQLException;

    /**выставляет complete = true в таблице client_queue по заданным айди
     * @param clientId  id клиента
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void completeByClient(Connection conn, long clientId, long queueId) throws SQLException;

    /***выставляет complete = true и end_time = time в таблице client_queue по заданному ключу
     * @param clientId id клиента
     * @param queueId id очереди
     * @param time время конца осблуживания клиента
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void completeByExecutor(Connection conn, long clientId, long queueId, Timestamp time) throws SQLException;

    /**Возвращает список записей, которые удовлетворяют условиям complete = false и queue_id = queueId и start_time is
     * null
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return список оставшихся в очереди клиентов, может быть пустым
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<ClientInQueue> getRemainingClients(Connection conn, long queueId) throws SQLException;

    /**Возвращает список записей, которые удовлетворяют условиям complete = false и queue_id = queueId и start_time is
     * null
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return список клиентовв очереди, максимум двух, в порядке возрастания их места в очереди. Может быть пустым
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<ClientInQueue> getNextClients(Connection conn, long queueId) throws SQLException;

    /**Возвращает список записей клиентов, осблуженных исполнителем. Отображаются последние 20 клиентов
     * @param queueId id очереди
     * @param execId id исполнителя, который обслужил клиента
     * @param conn Соединение с базой данных
     * @return список обслуженных клиентов, или пустой список, если нет совпадений
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<ServedClient> getServedClients(Connection conn, long queueId, long execId) throws
            SQLException;

    /**выставляет notification = status в таблице client_queue по заданным айди и complete = false
     * @param clientId id клиента
     * @param queueId id очереди
     * @param status статус оповещений
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateNotificationStatus(Connection conn, long clientId, long queueId, boolean status) throws SQLException;

    /**Обновляет значение start_time и served_by_id у записи из таблицы client_queue по заданному айди
     * @param clientId id клиента
     * @param queueId id очереди
     * @param execId id исполнителя, осблуживающего клиента
     * @param time время начала осблуживания клиента
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateStartTimeAndServedId(Connection conn, long clientId, long queueId, long execId, Timestamp time)
            throws SQLException;

    /**Выставляет complete = true у всех записей по заданному айди очереди
     * @param conn Соединение с базой данных
     * @param queueId id очереди
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void completeAllClientsInQueue(Connection conn, long queueId) throws SQLException;

}
