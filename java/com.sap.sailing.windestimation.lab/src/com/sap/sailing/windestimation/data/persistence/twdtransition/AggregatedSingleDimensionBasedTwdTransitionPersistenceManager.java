package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.json.simple.JSONObject;

import com.mongodb.BasicDBObject;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.serialization.AggregatedSingleDimensionBasedTwdTransitionJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.AggregatedSingleDimensionBasedTwdTransitionJsonSerializer;
import com.sap.sailing.windestimation.data.serialization.SingleDimensionBasedTwdTransitionJsonSerializer;

public class AggregatedSingleDimensionBasedTwdTransitionPersistenceManager
        extends AbstractPersistenceManager<AggregatedSingleDimensionBasedTwdTransition> {

    private final JsonSerializer<AggregatedSingleDimensionBasedTwdTransition> serializer = new AggregatedSingleDimensionBasedTwdTransitionJsonSerializer();
    private final String collectionName;
    private final AggregatedSingleDimensionType dimensionType;

    public AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(AggregatedSingleDimensionType dimensionType)
            throws UnknownHostException {
        this.dimensionType = dimensionType;
        this.collectionName = dimensionType.getCollectioName();
        BasicDBObject indexes = new BasicDBObject(SingleDimensionBasedTwdTransitionJsonSerializer.DIMENSION_VALUE, 1);
        getCollection().createIndex(indexes);
    }

    public AggregatedSingleDimensionType getDimensionType() {
        return dimensionType;
    }

    public PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> getIteratorSorted() {
        return super.getIterator(null,
                "{'" + AggregatedSingleDimensionBasedTwdTransitionJsonSerializer.DIMENSION_VALUE + "': 1}");
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    protected JsonDeserializer<AggregatedSingleDimensionBasedTwdTransition> getNewJsonDeserializer() {
        return new AggregatedSingleDimensionBasedTwdTransitionJsonDeserializer();
    }

    public void add(AggregatedSingleDimensionBasedTwdTransition twdTransition) {
        JSONObject jsonObject = serializer.serialize(twdTransition);
        Document dbObject = parseJsonString(jsonObject.toString());
        getDb().getCollection(getCollectionName()).insertOne(dbObject);
    }

    public void add(List<AggregatedSingleDimensionBasedTwdTransition> twdTransitions) {
        List<Document> dbObjects = twdTransitions.stream()
                .map(twdTransition -> parseJsonString(serializer.serialize(twdTransition).toString()))
                .collect(Collectors.toList());
        getDb().getCollection(getCollectionName()).insertMany(dbObjects);
    }

    public enum AggregatedSingleDimensionType {
        DISTANCE("aggregatedDistanceTwdTransition", "Meters"), DURATION("aggregatedDurationTwdTransition", "Seconds");

        private final String collectioName;
        private final String unitName;

        private AggregatedSingleDimensionType(String collectionName, String unitName) {
            this.collectioName = collectionName;
            this.unitName = unitName;
        }

        public String getCollectioName() {
            return collectioName;
        }

        public String getUnitName() {
            return unitName;
        }

        public String getDimensionName() {
            String dimensionName = this.toString().toLowerCase();
            return dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
        }
    }

}
