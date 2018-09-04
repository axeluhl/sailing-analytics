package com.sap.sailing.windestimation.data.persistence;

import java.util.List;

import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sse.common.Util.Pair;

public interface PersistenceManager<T> {

    String getCollectionName();

    void dropCollection();

    boolean collectionExists();

    long countElements();

    Pair<String, T> getNextElement(String lastId) throws JsonDeserializationException, ParseException;

    PersistedElementsIterator<T> getIterator();

    List<T> getAllElements(String query) throws JsonDeserializationException, ParseException;

    List<T> getAllElements() throws JsonDeserializationException, ParseException;

}
