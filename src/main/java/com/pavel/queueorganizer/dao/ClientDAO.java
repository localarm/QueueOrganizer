package com.pavel.queueorganizer.dao;

import com.pavel.queueorganizer.Client;

import java.sql.Connection;
import java.sql.SQLException;

public interface ClientDAO  {
    /** Добавляет запись о клиенте в таблицу client
     * @param client добавляемый клиент
     * @param conn Соединение с базой данных
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    void add(Connection conn, Client client) throws SQLException;

    /**Вовзращает модель клиента по указанному айди из таблицы client
     * @param clientId id клиента
     * @param conn соединение с базой данных
     * @return клиента, либо null, если нет записи, удовлетворяющей условию
     * @throws SQLException если возникает ошибка при работе с базой данных
     */
    Client get(Connection conn, long clientId) throws SQLException;
}
