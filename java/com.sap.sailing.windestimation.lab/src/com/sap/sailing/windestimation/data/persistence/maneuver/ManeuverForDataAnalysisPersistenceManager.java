package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;

public class ManeuverForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversForDataAnalysisPersistenceManager {

    public static final String COLLECTION_NAME = "maneuversForDataAnalysis";

    public ManeuverForDataAnalysisPersistenceManager() throws UnknownHostException {
        super();
    }

    @Override
    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    //TODO repair if required
    @Override
    protected Bson getMongoDbEvalStringForTransformation() {
        return new Document().append("aggregate", RaceWithManeuverForDataAnalysisPersistenceManager.COLLECTION_NAME + "." + AbstractRaceWithEstimationDataPersistenceManager.COMPETITOR_TRACKS_COLLECTION_NAME_EXTENSION).
                              append("pipeline", Arrays.asList(
                new Document("$addFields", new Document().append("competitorTracks.elements.regattaName", "$regattaName")),
                new Document("$addFields", new Document().append("competitorTracks.elements.windQuality", "$windQuality")),
                new Document("$addFields", new Document().append("competitorTracks.trackId",
                        new Document("$concat", Arrays.asList("$regattaName", " - ", "$trackedRaceName")))),
                new Document("$unwind", "$competitorTracks").
                new Document("$addFields", new Document()
                .append("competitorTracks.elements.trackId", new Document("$concat", Arrays.asList("$competitorTracks.trackId", " # ", "$competitorTracks.competitorName")))
                .append("competitorTracks.avgTrackSpeedInKnots", new Document("$divide", new Document("$divide",
                        Arrays.asList("$competitorTracks.distanceTravelledInMeters", new Document("$divide",
                                Arrays.asList(new Document("$subtract",
                                        Arrays.asList("$competitorTracks.endUnixTime", "$competitorTracks.startUnixTime")))),
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
                + "{$replaceRoot: {newRoot : '$elements'}},\r\n" + "{$out: '" + COLLECTION_NAME + "'}\r\n" + "])\r\n";
    }

}
