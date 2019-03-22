package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

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
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('" + ManeuverForDataAnalysisPersistenceManager.COLLECTION_NAME + "').aggregate([\r\n" + "{$match: {\r\n" + "    $and: [\r\n"
                + "        {'absMainCurveAngle': {\r\n" + "            $gte: 20\r\n" + "        }},\r\n"
                + "        {'absMainCurveAngle': {\r\n" + "            $lte: 120\r\n" + "        }},\r\n"
                + "        {'deviationTackAngle': {\r\n" + "            $ne: null\r\n" + "        }},\r\n"
                + "        {'deviationJibeAngle': {\r\n" + "            $ne: null\r\n" + "        }},\r\n"
                + "        {'clean': {\r\n" + "            $eq: true\r\n" + "        }}\r\n" + "    ]\r\n" + "}},\r\n"
                + "{$out: '" + COLLECTION_NAME + "'}\r\n" + "])\r\n";
    }

}
