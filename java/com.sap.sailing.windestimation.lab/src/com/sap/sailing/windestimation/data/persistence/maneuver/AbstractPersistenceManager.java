package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
    public static final String FIELD_DB_ID = "_id";
    private final MongoDatabase database;
    private final JSONParser jsonParser = new JSONParser();
    private final JsonDeserializer<T> deserializer;
    private final MongoClient mongoClient;
    private final DB db;

    @SuppressWarnings("deprecation")
    public AbstractPersistenceManager() throws UnknownHostException {
        mongoClient = new MongoClient(DB_HOST, DB_PORT);
        database = mongoClient.getDatabase(DB_NAME);
        db = mongoClient.getDB(DB_NAME);
        deserializer = getNewJsonDeserializer();
    }

    public MongoCollection<Document> getCollection() {
        return database.getCollection(getCollectionName());
    }

    protected abstract JsonDeserializer<T> getNewJsonDeserializer();

    public void dropCollection() {
        getCollection().drop();
    }

    public boolean collectionExists() {
        String targetCollectionName = getCollectionName();
        for (String collectionName : database.listCollectionNames()) {
            if (collectionName.equals(targetCollectionName)) {
                return true;
            }
        }
        return false;
    }

    public long countElements(Document query) {
        return getCollection().count(query);
    }

    public long countElements(String query) {
        Document dbQuery = query == null ? null : Document.parse(query);
        return countElements(dbQuery);
    }

    @Override
    public Pair<String, T> getNextElement(String lastId, String query)
            throws JsonDeserializationException, ParseException {
        Pair<String, T> result = null;
        Document gtQuery = null;
        if (lastId != null) {
            gtQuery = new Document();
            gtQuery.put(FIELD_DB_ID, new Document("$gt", new ObjectId(lastId)));
        }
        String finalQuery = null;
        if (gtQuery != null && query != null) {
            finalQuery = "{$and: [" + gtQuery.toString() + ", " + query + "]}";
        } else if (gtQuery != null) {
            finalQuery = gtQuery.toString();
        } else if (query != null) {
            finalQuery = query;
        }
        Document dbQuery = parseJsonString(finalQuery);
        Document dbObject = getCollection().find(dbQuery).first();
        if (dbObject != null) {
            ObjectId dbId = (ObjectId) dbObject.get(FIELD_DB_ID);
            T element = deserializer.deserialize(getJSONObject(dbObject));
            result = new Pair<>(dbId.toHexString(), element);
        }
        return result;
    }

    @Override
    public List<T> getAllElements() throws JsonDeserializationException, ParseException {
        return getAllElements(null);
    }

    @Override
    public String getFilterQueryForYear(int year, boolean exclude) {
        String query;
        if (exclude) {
            query = "{'regattaName': {$not: {$regex: '" + year + "'}}}";
        } else {
            query = "{'regattaName': {$regex: '" + year + "'}}";
        }
        return query;
    }

    @Override
    public List<T> getAllElements(String query) throws JsonDeserializationException, ParseException {
        Document dbQuery = parseJsonString(query);
        MongoCursor<Document> dbCursor = getCollection().find(dbQuery).iterator();
        List<T> result = new ArrayList<>();
        while (dbCursor.hasNext()) {
            Document dbObject = dbCursor.next();
            T element = deserializer.deserialize(getJSONObject(dbObject));
            result.add(element);
        }
        return result;
    }

    @Override
    public PersistedElementsIterator<T> getIterator(String query) {
        Document dbQuery = parseJsonString(query);
        return new PersistedElementsIteratorImpl(dbQuery);
    }

    @Override
    public PersistedElementsIterator<T> getIterator(Document query) {
        return new PersistedElementsIteratorImpl(query);
    }

    @Override
    public PersistedElementsIterator<T> getIterator() {
        return getIterator((Document) null);
    }

    @Override
    public PersistedElementsIterator<T> getIterator(String query, String sort) {
        Document dbQuery = parseJsonString(query);
        Document dbSort = parseJsonString(sort);
        return new PersistedElementsIteratorImpl(dbQuery, dbSort);
    }

    @Override
    public MongoDatabase getDb() {
        return database;
    }

    public DB getDbOld() {
        return db;
    }

    protected class PersistedElementsIteratorImpl implements PersistedElementsIterator<T> {

        private MongoCursor<Document> dbCursor;
        private T nextElement = null;
        private long numberOfElements;
        private long currentElementNumber = 0;
        private long limit = Long.MAX_VALUE;

        public PersistedElementsIteratorImpl(Document query) {
            this(query, null);
        }

        public PersistedElementsIteratorImpl(Document query, Document sort) {
            numberOfElements = countElements(query);
            FindIterable<Document> findIterable = query == null ? getCollection().find() : getCollection().find(query);
            if (sort != null) {
                findIterable = findIterable.sort(sort);
            }
            this.dbCursor = findIterable.iterator();
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
            if (nextElementNumber <= limit) {
                if (numberOfElements >= 100 && nextElementNumber % (numberOfElements / 100) == 0) {
                    LoggingUtil.logInfo("## Loading element " + nextElementNumber + "/" + numberOfElements + " ("
                            + (nextElementNumber * 100 / numberOfElements) + " %) from " + getCollectionName());
                }
                try {
                    if (dbCursor.hasNext()) {
                        Document nextDbObject = dbCursor.next();
                        this.nextElement = deserializer.deserialize(getJSONObject(nextDbObject));
                        this.currentElementNumber = nextElementNumber;
                    } else {
                        this.nextElement = null;
                    }
                } catch (JsonDeserializationException | ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.nextElement = null;
            }
        }

        public long getNumberOfElements() {
            return numberOfElements;
        }

        @Override
        public PersistedElementsIterator<T> limit(long limit) {
            this.limit = limit;
            if (numberOfElements > limit) {
                numberOfElements = limit;
            }
            return this;
        }

    }

    protected JSONObject getJSONObject(Document dbObject) throws ParseException {
        ObjectId dbId = (ObjectId) dbObject.get(AbstractPersistenceManager.FIELD_DB_ID);
        String jsonString = dbObject.toJson();
        jsonString = jsonString.replaceAll("\\{\\s*\\\"\\$numberLong\\\"\\s*\\:\\s*\\\"(\\d+)\\\"\\s*\\}", "$1");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);
        if (dbId != null) {
            jsonObject.put(AbstractPersistenceManager.FIELD_DB_ID, dbId.toHexString());
        }
        return jsonObject;
    }

    protected Document parseJsonString(String jsonString) {
        return jsonString == null ? new Document() : Document.parse(jsonString);
    }

}