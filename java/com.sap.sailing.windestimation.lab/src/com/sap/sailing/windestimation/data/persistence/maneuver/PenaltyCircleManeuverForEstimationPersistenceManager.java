package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

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
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('" + ManeuverForEstimationPersistenceManager.COLLECTION_NAME + "').aggregate([\r\n" + 
                "{$match: {\r\n" + 
                "        {'category': {\r\n" + 
                "                $eq: '_360'\r\n" + 
                "        }}\r\n" + 
                "}},\r\n" + 
                "{$out: 'penaltyCirclesForEstimation'}\r\n" + 
                "])";
    }

}
