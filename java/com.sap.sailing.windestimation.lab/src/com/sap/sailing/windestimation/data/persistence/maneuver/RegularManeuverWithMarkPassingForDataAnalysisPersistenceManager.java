package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

public class RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public static final String COLLECTION_NAME = "regularManeuversWithMarkPassingsForDataAnalysis";

    public RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new ManeuverForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException {
        return getDb().getCollection(ManeuverForDataAnalysisPersistenceManager.COLLECTION_NAME);
    }

    @Override
    protected List<? extends Bson> getMongoDbAggregationPipelineForTransformation() {
        return Arrays.asList(new Document[] {
                Document.parse("{ $match: { $and: ["
                + "        {'absMainCurveAngle': { $gte: 20 }},"
                + "        {'absMainCurveAngle': { $lte: 120 }},"
                + "        {'deviationTackAngle': { $ne: null }},"
                + "        {'deviationJibeAngle': { $ne: null }},"
                + "        {'clean': { $eq: true }}]}}"),
                getOutPipelineStage()
        });
    }

}
