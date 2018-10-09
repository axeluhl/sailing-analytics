package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

public class ManeuverForEstimationPersistenceManager
        extends AbstractTransformedManeuversForEstimationPersistenceManager {

    public ManeuverForEstimationPersistenceManager() throws UnknownHostException {
        super();
    }

    @Override
    public String getCollectionName() {
        return "maneuversForEstimation";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('racesWithManeuversForEstimation').aggregate([\r\n" + 
                "{$addFields: {\"competitorTracks.elements.regattaName\": '$regattaName'}},\r\n" + 
                "{$unwind: '$competitorTracks'},\r\n" + 
                "{$match: {\r\n" + 
                "    'competitorTracks.clean': true\r\n" + 
                "}},\r\n" + 
                "{$project: {\r\n" + 
                "    elements: '$competitorTracks.elements'\r\n" + 
                "}},\r\n" + 
                "{$unwind: '$elements'},\r\n" + 
                "{$replaceRoot: {newRoot : '$elements'}},\r\n" + 
                "{$out: 'maneuversForEstimation'}\r\n" + 
                "])";
    }

}
