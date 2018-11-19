package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public class RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new ManeuverForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return "regularManeuversWithMarkPassingsForDataAnalysis";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('maneuversForDataAnalysis').aggregate([\r\n" + "{$match: {\r\n" + "    $and: [\r\n"
                + "        {'absMainCurveAngle': {\r\n" + "            $gte: 20\r\n" + "        }},\r\n"
                + "        {'absMainCurveAngle': {\r\n" + "            $lte: 120\r\n" + "        }},\r\n"
                + "        {'deviationTackAngle': {\r\n" + "            $ne: null\r\n" + "        }},\r\n"
                + "        {'deviationJibeAngle': {\r\n" + "            $ne: null\r\n" + "        }},\r\n"
                + "        {'clean': {\r\n" + "            $eq: true\r\n" + "        }}\r\n" + "    ]\r\n" + "}},\r\n"
                + "{$out: 'regularManeuversWithMarkPassingsForDataAnalysis'}\r\n" + "])\r\n";
    }

}
