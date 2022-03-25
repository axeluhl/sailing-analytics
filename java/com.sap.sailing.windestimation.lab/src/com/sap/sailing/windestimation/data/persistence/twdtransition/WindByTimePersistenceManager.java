package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.FieldNames;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.PersistenceFactory;
import com.sap.sailing.server.gateway.deserialization.impl.MongoDbFriendlyPositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.serialization.WindSourceJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.WindSourceMetadataJsonDeserializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.shared.json.JsonDeserializationException;

public class WindByTimePersistenceManager {
    static final String COLLECTION_NAME = "windByTime";
    private final MongoDatabase database;
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;

    public WindByTimePersistenceManager() throws UnknownHostException {
        MongoDBService mongoDbService = MongoDBConfiguration.getDefaultConfiguration().getService();
        database = mongoDbService.getDB();
        final Document indexeSortedByTime = new Document(FieldNames.TIME_AS_MILLIS.name(), 1);
        getCollection().createIndex(indexeSortedByTime);
        mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        domainObjectFactory = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
    }

    private MongoCollection<Document> getCollection() {
        return database.getCollection(COLLECTION_NAME);
    }

    public void dropCollection() {
        getCollection().drop();
    }

    public void add(JSONArray windSourcesJson) throws JsonDeserializationException {
        final MongoDbFriendlyPositionJsonDeserializer positionDeserializer = new MongoDbFriendlyPositionJsonDeserializer();
        final WindSourceJsonDeserializer deserializer = new WindSourceJsonDeserializer(new WindJsonDeserializer(positionDeserializer), new WindSourceMetadataJsonDeserializer(positionDeserializer));
        final List<Document> windDocuments = new ArrayList<>();
        for (final Object o : windSourcesJson) {
            final WindSourceWithFixes windSource = deserializer.deserialize((JSONObject) o);
            for (final Wind windFix : windSource.getWindFixes()) {
                windDocuments.add(mongoObjectFactory.storeWind(windFix));
            }
        }
        getCollection().insertMany(windDocuments);
    }
    
    public Iterable<Wind> getWindNewerThan(TimePoint timePoint) {
        final long millis = timePoint.asMillis();
        final Document queryByTime = new Document(FieldNames.TIME_AS_MILLIS.name(), new Document("$gt", millis));
        final Document sorting = new Document(FieldNames.TIME_AS_MILLIS.name(), 1);
        return Util.map(getCollection().find(queryByTime, Document.class).sort(sorting), windDocument->domainObjectFactory.loadWind(windDocument));
    }

    public long countElements() {
        return getCollection().estimatedDocumentCount();
    }
}
