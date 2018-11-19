package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public class PenaltyCircleManeuverForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public PenaltyCircleManeuverForEstimationPersistenceManager() throws UnknownHostException {
        super(new ManeuverForEstimationPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return "penaltyCirclesForEstimation";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('maneuversForEstimation').aggregate([\r\n" + 
                "{$match: {\r\n" + 
                "        {'category': {\r\n" + 
                "                $eq: '_360'\r\n" + 
                "        }}\r\n" + 
                "}},\r\n" + 
                "{$out: 'penaltyCirclesForEstimation'}\r\n" + 
                "])";
    }

}
