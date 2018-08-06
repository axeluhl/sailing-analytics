package com.sap.sailing.windestimation.data.persistence.transformer;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.sap.sailing.windestimation.data.persistence.AbstractEstimationDataPersistenceManager;

public class CleanManeuversAsTopLevelCollectionTransformer {

    public static void main(String[] args) throws UnknownHostException {
        DB db = new MongoClient(AbstractEstimationDataPersistenceManager.DB_HOST, AbstractEstimationDataPersistenceManager.DB_PORT).getDB(AbstractEstimationDataPersistenceManager.DB_NAME);
        Object result = db.eval("db.getCollection('races_with_maneuvers_for_classification').aggregate([\r\n" + 
                "{$addFields: {\"competitorTracks.trackId\": {$concat: ['$regattaName', ' - ', '$trackedRaceName']}}},\r\n" + 
                "{$unwind: '$competitorTracks'},\r\n" + 
                "{$addFields: {\r\n" + 
                "    \"competitorTracks.elements.trackId\": {$concat: ['$competitorTracks.trackId', ' # ', '$competitorTracks.competitorName']},\r\n" + 
                "    'competitorTracks.avgTrackSpeedInKnots': {$divide: [\r\n" + 
                "        {$divide:\r\n" + 
                "            ['$competitorTracks.distanceTravelledInMeters',\r\n" + 
                "                {$divide: \r\n" + 
                "                    [ {$subtract: ['$competitorTracks.endUnixTime', '$competitorTracks.startUnixTime']},\r\n" + 
                "                        1000.0\r\n" + 
                "                ]}\r\n" + 
                "            ]\r\n" + 
                "        },\r\n" + 
                "        0.5144444444444\r\n" + 
                "        ]},\r\n" + 
                "    'markPassingsCountMatchesWaypointsCount': {$eq: ['$competitorTracks.markPassingsCount', '$competitorTracks.waypointsCount']}\r\n" + 
                "}},\r\n" + 
                "{$match: {\r\n" + 
                "    $and: [\r\n" + 
                "        {'competitorTracks.markPassingsCount': {\r\n" + 
                "            $gt: 1\r\n" + 
                "        }},\r\n" + 
                "        {'markPassingsCountMatchesWaypointsCount': {\r\n" + 
                "            $eq: true\r\n" + 
                "        }},\r\n" + 
                "        {'competitorTracks.avgIntervalBetweenFixesInSeconds': {\r\n" + 
                "            $lt: 8\r\n" + 
                "        }},\r\n" + 
                "        {'competitorTracks.avgTrackSpeedInKnots': {\r\n" + 
                "            $gt: 1.0\r\n" + 
                "        }}\r\n" + 
                "    ]\r\n" + 
                "}},\r\n" + 
                "{$project: {\r\n" + 
                "    elements: '$competitorTracks.elements'\r\n" + 
                "}},\r\n" + 
                "{$unwind: '$elements'},\r\n" + 
                "{$replaceRoot: {newRoot : '$elements'}},\r\n" + 
                "{$out: 'maneuversFiltered'}\r\n" + 
                "])\r\n" + 
                "");
        System.out.println(result);
    }

}
