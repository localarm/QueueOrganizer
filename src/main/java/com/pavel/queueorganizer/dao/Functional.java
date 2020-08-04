package com.pavel.queueorganizer.dao;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Functional<T> {
    T setup(PreparedStatement statement) throws SQLException;
}
