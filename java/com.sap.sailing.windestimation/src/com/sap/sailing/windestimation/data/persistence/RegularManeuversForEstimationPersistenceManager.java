package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

public class RegularManeuversForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public RegularManeuversForEstimationPersistenceManager() throws UnknownHostException {
        super(new ManeuverForEstimationPersistenceManager());
    }

    @Override
    public String getCollectionName() {
        return "regularManeuversForEstimation";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('maneuversForEstimation').aggregate([\r\n" + 
                "{$match: {\r\n" + 
                "    $and: [\r\n" + 
                "                $or: [\r\n" + 
                "                                {'category': 'REGULAR'},\r\n" + 
                "                                {'category': 'MARK_PASSING'}\r\n" + 
                "                        ],\r\n" + 
                "        {'deviationTackAngle': {\r\n" + 
                "            $ne: null\r\n" + 
                "        }},\r\n" + 
                "        {'deviationJibeAngle': {\r\n" + 
                "            $ne: null\r\n" + 
                "        }},\r\n" + 
                "        {'clean': {\r\n" + 
                "            $eq: true\r\n" + 
                "        }}\r\n" + 
                "    ]\r\n" + 
                "}},\r\n" + 
                "{$out: 'regularManeuversForEstimation'}\r\n" + 
                "])";
    }

}
