package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

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
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('" + RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager.COLLECTION_NAME + "').aggregate([\r\n" + "{$match: {\r\n"
                + "    $and: [\r\n" + "        {'category': {\r\n" + "            $ne: 'MARK_PASSING'\r\n"
                + "        }}\r\n" + "    ]\r\n" + "}},\r\n"
                + "{$out: '" + COLLECTION_NAME + "'}\r\n" + "])\r\n";
    }

}
