package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public class ManeuverForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public static final String COLLECTION_NAME = "maneuversForEstimation";

    public ManeuverForEstimationPersistenceManager() throws UnknownHostException {
        super();
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException {
        return getDb().getCollection(new RaceWithManeuverForEstimationPersistenceManager().getCompetitorTracksCollectionName());
    }

    @Override
    protected List<? extends Bson> getMongoDbAggregationPipelineForTransformation() {
        return Arrays.asList(new Document[] {
                Document.parse("{$match: { 'clean': true }}"),
                Document.parse("{$project: { elements: '$elements'}}"),
                Document.parse("{$unwind: '$elements'}"),
                Document.parse("{$replaceRoot: {newRoot : '$elements'}}"),
                getOutPipelineStage()
        });
    }

}
