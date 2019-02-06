package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public class RegularManeuversForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public static final String COLLECTION_NAME = "regularManeuversForEstimation";

    public RegularManeuversForEstimationPersistenceManager() throws UnknownHostException {
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
                "    $and: [\r\n" + 
                "                {$or: [\r\n" + 
                "                                {'category':\r\n" + 
                "                                        {$eq: 'REGULAR'}\r\n" + 
                "                },\r\n" + 
                "                                {'category':\r\n" + 
                "                                        {$eq: 'MARK_PASSING'}\r\n" + 
                "                                }\r\n" + 
                "                        ]},\r\n" + 
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
                "{$out: '" + COLLECTION_NAME + "'}\r\n" + 
                "])";
    }

}
