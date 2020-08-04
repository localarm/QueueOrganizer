package com.pavel.queueorganizer.dao;

import com.pavel.queueorganizer.Queue;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface QueueDAO {

    /**добавляет очередь в таблицу queue
     * @param queue добавляемая очередь
     * @param conn Соединение с базой данных
     * @return id созданной записи
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    long create(Connection conn, Queue queue) throws SQLException;

    /**Возвращает запись из таблицы queue с указанным id
     * @param id очереди
     * @param conn Соединение с базой данных
     * @return очередь, либо null, если нет записи, удовлетворяющей условию
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    Queue get(Connection conn, long id) throws SQLException;

    /**Возвращает список очередей, которые находятся в радиусе 60 метров от передаваемх координат, сортируются по
     * количеству уникальных клиентов
     * @param conn Соединение с базой данных
     * @param longitude долгота
     * @param latitude широта
     * @return список подходящих очередей, пустой список, если очередей не было найдено
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<Queue> getByDistance(Connection conn, float longitude, float latitude) throws SQLException;

    /**Обновляет значения start_time, end_time и work_hours в таблице queue у записи с указанным id
     * @param id id очереди
     * @param startTime время открытия очереди
     * @param endTime время заверешения очереди
     * @param workHours строка с рабочими часами
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateWorkHours(Connection conn, long id, Timestamp startTime, Timestamp endTime, String workHours) throws
            SQLException;

    /**Выставляет передаваемый статус в столбце active в таблице queue у записи с указанным id
     * @param id очереди
     * @param status устанавливаемый статус столбца active
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateActivity(Connection conn, long id, boolean status) throws SQLException;

    /**Выставляет статус invalid = true и end_time = current_timestamp у очереди с указанным id в таблице queue
     * @param id очереди
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void close(Connection conn, long id) throws SQLException;

    /**Возвращает количество записей, в которых invalid = false и admin равен указанному id
     * @param id id амдинистратора очередей
     * @param conn Соединение с базой данных
     * @return количество совпадений в таблице queue, 0 если совпадений нет
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    int getCountByAdmin(Connection conn, long id) throws SQLException;

    /**Выставляет last_place = place таблице queue у записи с указанным id
     * @param Id очереди
     * @param place новое значение места в очереди
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void updateLastPlace(Connection conn, long Id, long place) throws SQLException;

    /**Возвращает записи обо всех очередях из таблицы queue с указанным id администратора
     * @param adminId id администратора
     * @param conn Соединение с базой данных
     * @return список очередей, может вернуть пустой список, если нет подходящих записей
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<Queue> getAllQueuesByAdmin(Connection conn, long adminId) throws SQLException;

    /**Проверяет уникальность названия очереди среди всех не удаленных очередей
     * @param conn Соединение с базой данных
     * @param queueName имя очереди
     * @return true если нет совпадений с именем, false в остальных случаях
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    boolean checkQueueName(Connection conn, String queueName) throws SQLException;

    /**Выбирает строки с очередями, в которых числится данный исполнитель
     * @param conn Соединение с базой данных
     * @param execId айди исполнителя
     * @param longitude долгота
     * @param latitude широта
     * @return Список с очередями, если совпадений нет, то вовращается пустой список
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    List<Queue> getQueuesByExecutor(Connection conn, long execId, float longitude, float latitude) throws SQLException;


}
