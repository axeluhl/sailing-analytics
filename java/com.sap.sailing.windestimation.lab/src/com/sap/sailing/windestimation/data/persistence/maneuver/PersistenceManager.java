package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.util.List;

import org.bson.Document;
import org.json.simple.parser.ParseException;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

    PersistedElementsIterator<T> getIterator();

    PersistedElementsIterator<T> getIterator(String query);

    PersistedElementsIterator<T> getIterator(Document query);

    List<T> getAllElements(String query) throws JsonDeserializationException, ParseException;

    List<T> getAllElements() throws JsonDeserializationException, ParseException;

    Pair<String, T> getNextElement(String lastId, String query) throws JsonDeserializationException, ParseException;

    String getFilterQueryForYear(int year, boolean exclude);

    MongoDatabase getDb();

    MongoCollection<Document> getCollection();

    PersistedElementsIterator<T> getIterator(String query, String sort);

}
