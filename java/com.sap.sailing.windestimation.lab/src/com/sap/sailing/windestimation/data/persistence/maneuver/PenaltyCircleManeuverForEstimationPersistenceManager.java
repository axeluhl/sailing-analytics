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
        return "{" +
                "aggregate: '" + ManeuverForEstimationPersistenceManager.COLLECTION_NAME + "',\r\n" +
                "pipeline: [\r\n" +  
                "{$match: {\r\n" + 
                "        {'category': {\r\n" + 
                "                $eq: '_360'\r\n" + 
                "        }}\r\n" + 
                "}},\r\n" + 
                "{$out: '" + COLLECTION_NAME + "'}\r\n" + 
                "],\r\n" +
                "cursor: {}\r\n" +
                "}";
    }

}
