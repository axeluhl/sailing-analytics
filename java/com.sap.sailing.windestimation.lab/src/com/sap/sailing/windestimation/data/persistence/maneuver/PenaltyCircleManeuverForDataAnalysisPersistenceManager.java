package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public class PenaltyCircleManeuverForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public static final String COLLECTION_NAME = "penaltyCirclesForDataAnalysis";

    public PenaltyCircleManeuverForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new ManeuverForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "{" +
                "aggregate: '" + ManeuverForDataAnalysisPersistenceManager.COLLECTION_NAME + "',\r\n" +
                "pipeline: [\r\n" +  
                "{$match: {\r\n" + "    $and: [\r\n"
                + "        {'category': {\r\n" + "            $eq: '_360'\r\n" + "        }}\r\n" + "    ]\r\n"
                + "}},\r\n" + "{$out: '" + COLLECTION_NAME + "'}\r\n" +
                "],\r\n" +
                "cursor: {}\r\n" +
                "}";
    }

}
