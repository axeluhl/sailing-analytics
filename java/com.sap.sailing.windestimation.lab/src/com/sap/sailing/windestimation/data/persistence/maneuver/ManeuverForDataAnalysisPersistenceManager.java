package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

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

    @Override
    protected MongoCollection<?> getCollectionForTransformation() throws UnknownHostException {
        return getDb().getCollection(new RaceWithManeuverForDataAnalysisPersistenceManager().getCompetitorTracksCollectionName());
    }

    @Override
    protected List<? extends Bson> getMongoDbAggregationPipelineForTransformation() throws UnknownHostException {
        return Arrays.asList(new Document[] {
                Document.parse("{$addFields: {\"competitorTracks.elements.regattaName\": '$regattaName'}}"),
                Document.parse("{$addFields: {\"competitorTracks.elements.windQuality\": '$windQuality'}}"),
                Document.parse("{$addFields: {\"competitorTracks.trackId\": {$concat: ['$regattaName', ' - ', '$trackedRaceName']}}}"),
                Document.parse("{$unwind: '$competitorTracks'}"),
                Document.parse("{$addFields: {"
                        + "    \"competitorTracks.elements.trackId\": {$concat: ['$competitorTracks.trackId', ' # ', '$competitorTracks.competitorName']}"
                        + "    'competitorTracks.avgTrackSpeedInKnots': {$divide: [" + "        {$divide:"
                        + "            ['$competitorTracks.distanceTravelledInMeters'" + "                {$divide: "
                        + "                    [ {$subtract: ['$competitorTracks.endUnixTime', '$competitorTracks.startUnixTime']}"
                        + "                        1000.0" + "                ]}" + "            ]"
                        + "        }" + "        0.5144444444444" + "        ]}"
                        + "    'markPassingsCountMatchesWaypointsCount': {$eq: ['$competitorTracks.markPassingsCount', '$competitorTracks.waypointsCount']}"
                        + "    'competitorTracks.elements.boatClass': '$competitorTracks.boatClass.name'"
                        + "    'competitorTracks.elements.fixesCountForPolars': '$competitorTracks.fixesCountForPolars'"
                        + "}}"),
                Document.parse("{$match: {" + "    $and: ["
                        + "        {'competitorTracks.markPassingsCount': { $gt: 1 }}"
                        + "        {'markPassingsCountMatchesWaypointsCount': { $eq: true }}"
                        + "        {'competitorTracks.avgIntervalBetweenFixesInSeconds': { $lt: 8 }}"
                        + "        {'competitorTracks.avgTrackSpeedInKnots': { $gt: 1.0 }}"
                        + "    ]}}"),
                Document.parse("{$project: { elements: '$competitorTracks.elements' }}"),
                Document.parse("{$unwind: '$elements'}"),
                Document.parse("{$replaceRoot: {newRoot : '$elements'}}"),
                getOutPipelineStage()
        });
    }
}
