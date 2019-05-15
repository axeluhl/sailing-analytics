package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public class RegularManeuverWithoutMarkPassingForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    private static final String COLLECTION_NAME = "regularManeuversWithoutMarkPassingsForDataAnalysis";

    public RegularManeuverWithoutMarkPassingForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException {
        return getDb().getCollection(RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager.COLLECTION_NAME);
    }

    @Override
    protected List<? extends Bson> getMongoDbAggregationPipelineForTransformation() {
        return Arrays.asList(new Document[] {
                Document.parse("{ $match: { $and: [{'category': { $ne: 'MARK_PASSING' }}]}}"),
                getOutPipelineStage()
        });
    }

    protected Document getOutPipelineStage() {
        return Document.parse("{$out: '" + COLLECTION_NAME + "'}");
    }

}
