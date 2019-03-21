package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import org.bson.conversions.Bson;

public class RegularManeuverWithoutMarkPassingForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public RegularManeuverWithoutMarkPassingForDataAnalysisPersistenceManager() throws UnknownHostException {
        super(new RegularManeuverWithMarkPassingForDataAnalysisPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return "regularManeuversWithoutMarkPassingsForDataAnalysis";
    }

    @Override
    protected Bson getMongoDbEvalStringForTransformation() {
        return "db.getCollection('regularManeuversWithMarkPassingsForDataAnalysis').aggregate([\r\n" + "{$match: {\r\n"
                + "    $and: [\r\n" + "        {'category': {\r\n" + "            $ne: 'MARK_PASSING'\r\n"
                + "        }}\r\n" + "    ]\r\n" + "}},\r\n"
                + "{$out: 'regularManeuversWithoutMarkPassingsForDataAnalysis'}\r\n" + "])\r\n";
    }

}
