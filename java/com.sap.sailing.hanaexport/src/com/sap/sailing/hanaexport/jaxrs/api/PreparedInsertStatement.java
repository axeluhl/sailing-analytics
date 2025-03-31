package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Can insert objects of type {@code T} using a prepared statement, in batch mode.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public interface PreparedInsertStatement<T> {
    PreparedStatement getPreparedStatement();
    
    void parameterizeStatement(T objectToInsert) throws SQLException;

    default void insert(T objectToInsert) throws SQLException {
        parameterizeStatement(objectToInsert);
        getPreparedStatement().execute();
    }
    
    default void insertBatch(Iterable<T> objectsToInsert) throws SQLException {
        for (final T objectToInsert : objectsToInsert) {
            insertBatch(objectToInsert);
        }
    }

    default void insertBatch(final T objectToInsert) throws SQLException {
        parameterizeStatement(objectToInsert);
        getPreparedStatement().addBatch();
    }
    
    default int[] executeBatch() throws SQLException {
        return getPreparedStatement().executeBatch();
    }
}
