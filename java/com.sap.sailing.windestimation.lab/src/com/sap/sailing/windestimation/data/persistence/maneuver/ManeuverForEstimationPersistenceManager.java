package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import org.bson.conversions.Bson;

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
    protected Bson getMongoDbEvalStringForTransformation() {
        return "db.getCollection('" + RaceWithManeuverForEstimationPersistenceManager.COLLECTION_NAME + "." + AbstractRaceWithEstimationDataPersistenceManager.COMPETITOR_TRACKS_COLLECTION_NAME_EXTENSION + "').aggregate([\r\n" + 
                "{$match: {\r\n" + 
                "    'clean': true\r\n" + 
                "}},\r\n" + 
                "{$project: {\r\n" + 
                "    elements: '$elements'\r\n" + 
                "}},\r\n" + 
                "{$unwind: '$elements'},\r\n" + 
                "{$replaceRoot: {newRoot : '$elements'}},\r\n" + 
                "{$out: '" + COLLECTION_NAME + "'}\r\n" + 
                "])";
    }

}
