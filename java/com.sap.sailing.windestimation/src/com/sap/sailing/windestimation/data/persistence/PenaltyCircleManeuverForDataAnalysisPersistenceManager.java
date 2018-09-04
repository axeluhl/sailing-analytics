package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

public class PenaltyCircleManeuverForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public PenaltyCircleManeuverForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new ManeuverForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return "penaltyCirclesForDataAnalysis";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('maneuversForDataAnalysis').aggregate([\r\n" + "{$match: {\r\n" + "    $and: [\r\n"
                + "        {'category': {\r\n" + "            $eq: '_360'\r\n" + "        }}\r\n" + "    ]\r\n"
                + "}},\r\n" + "{$out: 'penaltyCirclesForDataAnalysis'}\r\n" + "])\r\n";
    }

}
