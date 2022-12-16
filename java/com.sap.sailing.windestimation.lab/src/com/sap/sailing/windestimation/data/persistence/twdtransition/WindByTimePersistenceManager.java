package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.client.ClientSession;
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
    final static String COLLECTION_NAME = "windByTime";
    private final static Logger logger = Logger.getLogger(WindByTimePersistenceManager.class.getName());
    private final MongoDatabase database;
    private final MongoObjectFactory mongoObjectFactory;
    private final DomainObjectFactory domainObjectFactory;
    private final MongoDBService mongoDbService;

    public WindByTimePersistenceManager() throws UnknownHostException {
        mongoDbService = MongoDBConfiguration.getDefaultConfiguration().getService();
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
        final ClientSession session = mongoDbService.startAutoRefreshingSession();
        final Iterable<Wind> resultIterable = Util.map(getCollection().find(session, queryByTime, Document.class).sort(sorting),
                windDocument->domainObjectFactory.loadWind(windDocument));
        // construct an Iterable whose Iterator references the ClientSession strongly until hasNext returns false;
        // this way, the session will auto-refresh because it cannot be garbage collected as long as the iterator
        // is strongly referenced and hasn't consumed all elements.
        final Iterable<Wind> resultWithSessionAttached = new Iterable<Wind>() {
            @Override
            public Iterator<Wind> iterator() {
                return new Iterator<Wind>() {
                    private ClientSession strongSessionReference = session;
                    final private Iterator<Wind> iterator = resultIterable.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        if (!iterator.hasNext() && strongSessionReference != null) {
                            logger.fine("Clearing strong reference to client session "+
                                    strongSessionReference.getServerSession().getIdentifier());
                            strongSessionReference = null; // can be garbage-collected now
                        }
                        return iterator.hasNext();
                    }

                    @Override
                    public Wind next() {
                        return iterator.next();
                    }
                };
            }
        };
        return resultWithSessionAttached;
    }

    public long countElements() {
        return getCollection().estimatedDocumentCount();
    }
}
