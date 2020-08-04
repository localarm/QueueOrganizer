package com.pavel.queueorganizer.dao;
import com.pavel.queueorganizer.Executor;
import com.pavel.queueorganizer.Queue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ExecutorDAO {

    /**Добавляет исполнителя в таблицу executor и возвращает полученный при этой операции id
     * @param executor добавляемый исполнитель
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void add(Connection conn, Executor executor) throws SQLException;

    /**Выставляет статус serve_client = true и serving_client_id = servingClientId у исполнителя с указанными id в
     * таблице executor
     * @param queueId id очереди
     * @param servingClientId id обслуживаемого клиента               
     * @param execId id исполнителя
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateServeAndServingClientStatus(Connection conn, long execId, long queueId, long servingClientId) throws
            SQLException;

    /**Выставляет статус serve_client = true, serving_client_id = servingClientId и waiting = false у исполнителя с
     * указанными id в таблице executor
     * @param queueId id очереди
     * @param servingClientId id обслуживаемого клиента
     * @param execId id исполнителя
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateServeAndServingIdWhileWaiting(Connection conn, long execId, long queueId, long servingClientId)
            throws SQLException;

    /**Вовзращает заполненную модель исполнителя, соответсвующую записи в таблице executor с указанными id
     * @param queueId id очереди
     * @param execId id исполнителя
     * @return  модель исполнителя, либо null, если нет записи, удовлетворяющей условию
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    Executor get(Connection conn, long queueId, long execId) throws SQLException;

    /**Вовзращает список исполнителей, соответсвующую записям в таблице executor, у которых queue_id совпадает с указанным
     * queueId и invalid = false
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return список исполнтелей или пустой список, если нет совпадений
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<Executor> getAllByQueue(Connection conn, long queueId) throws SQLException;

    /**Изменяет статус active на переданный у исполнителя в таблице executor
     * @param execId id исполнителя
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @param status статус активности исполнителя
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateActiveStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException;

    /**Изменяет  столбец name на переданную строку в таблице executor у записи, определенной по полученным id
     * @param execId id исполнителя
     * @param queueId id очереди
     * @param newName новое имя исполнителя
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateName(Connection conn, long execId, long queueId, String newName) throws SQLException;

    /**Возвращает количество записей, в которых invalid = false и queueId равен указанному id
     * @param queueId id амдинистратора очередей
     * @param conn Соединение с базой данных
     * @return количество совпадений поля queueId в таблице executor
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    int getCountByQueue(Connection conn, long queueId) throws SQLException;

    /**Возвращает запись из таблицы executor, у которой name совпадает с переданной строкой и queueId равен указанному id
     * @param name строка с имененм, не более 511 символов
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @return модель исполнителя, либо null, если нет записи, удовлетворяющей условию
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    Executor getByName(Connection conn, String name, long queueId) throws SQLException;

    /**Выставляет статус invalid = true у исполнителя с полученным id в таблице executor
     * @param execId id исполнителя
     * @param queueId id очереди
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateInvalidStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException;

    /**Выставляет статус invalid = true и active = false в таблице executor по заданным айди
     * @param conn Соединение с базой данных
     * @param execId id исполнителя
     * @param queueId id очереди
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void setInvalidAndActiveStatus(Connection conn, long execId, long queueId) throws SQLException;

    /**Изменяет статус serve_client, waiting и active у записи с указанным id в таблице executor на false
     * @param queueId id очереди
     * @param execId id исполнителя
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateQuitStatuses(Connection conn, long execId, long queueId)
            throws SQLException;

    /**Выставляет статус invalid = true у всех записей в заданной очереди
     * @param conn Соединение с базой данных
     * @param queueId id очереди
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void setInvalidToQueue(Connection conn, long queueId) throws SQLException;

    /**Выставляет статус serve_client = status  у исполнителя с указанными id втаблице executor
     * @param queueId id очереди
     * @param status статус, отображающий занятость исполнителя
     * @param execId id исполнителя
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateServeStatus(Connection conn, long execId, long queueId, boolean status) throws SQLException;

    /**Выставляет статус waiting = status у исполнителя с указанными id втаблице executor
     * @param queueId id очереди
     * @param execId id исполнителя
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void setWaitingStatus(Connection conn, long execId, long queueId) throws SQLException;

    /**Выбирает одну запись из таблицы executor, где waiting = true
     * @param conn Соединение с базой данных
     * @param queueId id очереди
     * @return запись свободного исполнителя, если свободного исполнителя нет - возвращает null
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    Executor getFreeExecutor(Connection conn, long queueId) throws SQLException;


}
