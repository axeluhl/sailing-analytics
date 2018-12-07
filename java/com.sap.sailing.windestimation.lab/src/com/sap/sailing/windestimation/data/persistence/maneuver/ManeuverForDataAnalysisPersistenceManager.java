package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public class ManeuverForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public ManeuverForDataAnalysisPersistenceManager() throws UnknownHostException {
        super();
    }

    @Override
    public String getCollectionName() {
        return "maneuversForDataAnalysis";
    }

    @Override
    protected String getMongoDbEvalStringForTransformation() {
        return "db.getCollection('racesWithManeuversForDataAnalysis').aggregate([\r\n"
                + "{$addFields: {\"competitorTracks.elements.regattaName\": '$regattaName'}},\r\n"
                + "{$addFields: {\"competitorTracks.elements.windQuality\": '$windQuality'}},\r\n"
                + "{$addFields: {\"competitorTracks.trackId\": {$concat: ['$regattaName', ' - ', '$trackedRaceName']}}},\r\n"
                + "{$unwind: '$competitorTracks'},\r\n" + "{$addFields: {\r\n"
                + "    \"competitorTracks.elements.trackId\": {$concat: ['$competitorTracks.trackId', ' # ', '$competitorTracks.competitorName']},\r\n"
                + "    'competitorTracks.avgTrackSpeedInKnots': {$divide: [\r\n" + "        {$divide:\r\n"
                + "            ['$competitorTracks.distanceTravelledInMeters',\r\n" + "                {$divide: \r\n"
                + "                    [ {$subtract: ['$competitorTracks.endUnixTime', '$competitorTracks.startUnixTime']},\r\n"
                + "                        1000.0\r\n" + "                ]}\r\n" + "            ]\r\n"
                + "        },\r\n" + "        0.5144444444444\r\n" + "        ]},\r\n"
                + "    'markPassingsCountMatchesWaypointsCount': {$eq: ['$competitorTracks.markPassingsCount', '$competitorTracks.waypointsCount']},\r\n"
                + "    'competitorTracks.elements.boatClass': '$competitorTracks.boatClass.name',\r\n"
                + "    'competitorTracks.elements.fixesCountForPolars': '$competitorTracks.fixesCountForPolars'\r\n"
                + "}},\r\n" + "{$match: {\r\n" + "    $and: [\r\n"
                + "        {'competitorTracks.markPassingsCount': {\r\n" + "            $gt: 1\r\n" + "        }},\r\n"
                + "        {'markPassingsCountMatchesWaypointsCount': {\r\n" + "            $eq: true\r\n"
                + "        }},\r\n" + "        {'competitorTracks.avgIntervalBetweenFixesInSeconds': {\r\n"
                + "            $lt: 8\r\n" + "        }},\r\n"
                + "        {'competitorTracks.avgTrackSpeedInKnots': {\r\n" + "            $gt: 1.0\r\n"
                + "        }}\r\n" + "    ]\r\n" + "}},\r\n" + "{$project: {\r\n"
                + "    elements: '$competitorTracks.elements'\r\n" + "}},\r\n" + "{$unwind: '$elements'},\r\n"
                + "{$replaceRoot: {newRoot : '$elements'}},\r\n" + "{$out: 'maneuversForDataAnalysis'}\r\n" + "])\r\n";
    }

}
