package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractPersistenceManager<T> implements PersistenceManager<T> {

    public static final int DB_PORT = 27017;
    public static final String DB_HOST = "127.0.0.1";
    public static final String DB_NAME = "windEstimation";
    private static final String FIELD_DB_ID = "_id";
    private final DB db;
    private final JSONParser jsonParser = new JSONParser();
    private final JsonDeserializer<T> deserializer;

    public AbstractPersistenceManager() throws UnknownHostException {
        db = new MongoClient(DB_HOST, DB_PORT).getDB(DB_NAME);
        deserializer = getNewJsonDeserializer();
    }

    protected abstract JsonDeserializer<T> getNewJsonDeserializer();

    public void dropCollection() {
        db.getCollection(getCollectionName()).drop();
    }

    public boolean collectionExists() {
        return db.collectionExists(getCollectionName());
    }

    public long countElements() {
        return db.getCollection(getCollectionName()).count();
    }

    @Override
    public Pair<String, T> getNextElement(String lastId) throws JsonDeserializationException, ParseException {
        Pair<String, T> result = null;
        BasicDBObject gtQuery = null;
        if (lastId != null) {
            gtQuery = new BasicDBObject();
            gtQuery.put(FIELD_DB_ID, new BasicDBObject("$gt", new ObjectId(lastId)));
        }
        DBObject dbObject = db.getCollection(getCollectionName()).findOne(gtQuery);
        if (dbObject != null) {
            ObjectId dbId = (ObjectId) dbObject.get(FIELD_DB_ID);
            T element = deserializer.deserialize(getJSONObject(dbObject.toString()));
            result = new Pair<>(dbId.toHexString(), element);
        }
        return result;
    }

    @Override
    public List<T> getAllElements() throws JsonDeserializationException, ParseException {
        return getAllElements(null);
    }

    @Override
    public List<T> getAllElements(String query) throws JsonDeserializationException, ParseException {
        DBObject dbQuery = null;
        if (query != null) {
            dbQuery = (DBObject) JSON.parse(query.toString());
        }
        DBCursor dbCursor = db.getCollection(getCollectionName()).find(dbQuery);
        List<T> result = new ArrayList<>(dbCursor.count());
        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            T element = deserializer.deserialize(getJSONObject(dbObject.toString()));
            result.add(element);
        }
        return result;
    }

    @Override
    public PersistedElementsIterator<T> getIterator() {
        return new PersistedElementsIteratorImpl();
    }

    protected JSONObject getJSONObject(String json) throws ParseException {
        return (JSONObject) jsonParser.parse(json);
    }

    protected DB getDb() {
        return db;
    }

    protected class PersistedElementsIteratorImpl implements PersistedElementsIterator<T> {

        private String lastDbId = null;
        private T nextElement = null;
        private final long numberOfElements;
        private long currentElementNumber = 0;
        private int numberOfCharsDuringLastStatusLog = 0;

        public PersistedElementsIteratorImpl() {
            numberOfElements = countElements();
            LoggingUtil.logInfo(numberOfElements + " elements found in " + getCollectionName());
            prepareNext();
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @Override
        public T next() {
            T nextElementInDb = this.nextElement;
            prepareNext();
            return nextElementInDb;
        }

        private void prepareNext() {
            long nextElementNumber = currentElementNumber + 1;
            if (nextElementNumber % (numberOfElements / 100) == 0) {
                LoggingUtil.delete(numberOfCharsDuringLastStatusLog);
                numberOfCharsDuringLastStatusLog = LoggingUtil
                        .logInfo("Loading element " + nextElementNumber + "/" + numberOfElements + " ("
                                + (nextElementNumber * 100 / numberOfElements) + " %) from " + getCollectionName());
            }
            try {
                Pair<String, T> nextElementWithDbId = getNextElement(lastDbId);
                if (this.nextElement != null) {
                    this.lastDbId = nextElementWithDbId.getA();
                    this.nextElement = nextElementWithDbId.getB();
                    this.currentElementNumber = nextElementNumber;
                }
            } catch (JsonDeserializationException | ParseException e) {
                throw new RuntimeException(e);
            }
        }

        public long getNumberOfElements() {
            return numberOfElements;
        }

    }

}