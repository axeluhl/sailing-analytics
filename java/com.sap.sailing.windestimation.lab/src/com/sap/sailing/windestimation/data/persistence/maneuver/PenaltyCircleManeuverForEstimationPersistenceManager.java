package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public class PenaltyCircleManeuverForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public static final String COLLECTION_NAME = "penaltyCirclesForEstimation";

    public PenaltyCircleManeuverForEstimationPersistenceManager() throws UnknownHostException {
        super(new ManeuverForEstimationPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException {
        return getDb().getCollection(ManeuverForEstimationPersistenceManager.COLLECTION_NAME);
    }

    @Override
    protected List<? extends Bson> getMongoDbAggregationPipelineForTransformation() {
        return Arrays.asList(new Document[] {
                Document.parse("{$match: {" + 
                        "        {'category': {" + 
                        "                $eq: '_360'" + 
                        "        }}" + 
                        "}}"),
                getOutPipelineStage()
        });
    }
}
