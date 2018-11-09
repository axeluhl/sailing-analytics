package com.sap.sailing.windestimation.data.persistence;

import java.util.List;

import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sse.common.Util.Pair;

public interface PersistenceManager<T> {

    String getCollectionName();

    void dropCollection();

    boolean collectionExists();

    long countElements(String query);

    default long countElements() {
        return countElements(null);
    }

    PersistedElementsIterator<T> getIterator(String query);

    List<T> getAllElements(String query) throws JsonDeserializationException, ParseException;

    List<T> getAllElements() throws JsonDeserializationException, ParseException;

    Pair<String, T> getNextElement(String lastId, String query) throws JsonDeserializationException, ParseException;

    String getFilterQueryForYear(int year, boolean exclude);

}
