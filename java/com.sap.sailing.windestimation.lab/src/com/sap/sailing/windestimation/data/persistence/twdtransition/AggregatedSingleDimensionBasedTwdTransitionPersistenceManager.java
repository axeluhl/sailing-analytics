package com.sap.sailing.windestimation.data.persistence.twdtransition;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.deserializer.AggregatedSingleDimensionBasedTwdTransitionJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.AggregatedSingleDimensionBasedTwdTransitionJsonSerializer;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractPersistenceManager;

public class AggregatedSingleDimensionBasedTwdTransitionPersistenceManager
        extends AbstractPersistenceManager<AggregatedSingleDimensionBasedTwdTransition> {

    private final JsonSerializer<AggregatedSingleDimensionBasedTwdTransition> serializer = new AggregatedSingleDimensionBasedTwdTransitionJsonSerializer();
    private final String collectionName;

    public AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(AggregatedSingleDimensionType dimensionType)
            throws UnknownHostException {
        this.collectionName = dimensionType.getCollectioName();
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
        DBObject dbObject = (DBObject) JSON.parse(jsonObject.toString());
        getDb().getCollection(getCollectionName()).insert(dbObject);
    }

    public void add(List<AggregatedSingleDimensionBasedTwdTransition> twdTransitions) {
        List<DBObject> dbObjects = twdTransitions.stream()
                .map(twdTransition -> (DBObject) JSON.parse(serializer.serialize(twdTransition).toString()))
                .collect(Collectors.toList());
        getDb().getCollection(getCollectionName()).insert(dbObjects);
    }

    public enum AggregatedSingleDimensionType {
        DISTANCE("aggregatedDistanceTwdTransition"), DURATION("aggregatedDurationTwdTransition");

        private final String collectioName;

        private AggregatedSingleDimensionType(String collectionName) {
            this.collectioName = collectionName;
        }

        public String getCollectioName() {
            return collectioName;
        }
    }

}
