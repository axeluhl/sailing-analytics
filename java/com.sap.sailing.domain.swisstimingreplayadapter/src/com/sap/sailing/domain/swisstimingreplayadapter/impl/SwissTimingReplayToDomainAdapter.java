package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.swisstimingreplayadapter.CompetitorStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayParser;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Turns the data received through the {@link SwissTimingReplayListener} callback interface into domain objects, creating
 * a {@link RaceDefinition} and {@link Competitor}s as well as a {@link Course} with {@link Waypoint}s and {@link Mark}s.
 * Also, this adapter creates a {@link TrackedRace} for the race and records all tracked positions for competitors and
 * marks as well as wind data in it.<p>
 * 
 * This adapter is stateful and not thread safe. It must be used only once for registering with a {@link SwissTimingReplayParser}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SwissTimingReplayToDomainAdapter extends SwissTimingReplayAdapter {
    private final Map<String, RaceDefinition> racePerRaceID;
    private final Map<String, DynamicTrackedRace> trackedRacePerRaceID;
    
    /**
     * The last race ID received from {@link #raceID(String)}. Used as key into {@link #racePerRaceID} and
     * {@link #trackedRacePerRaceID} for storing data from subsequent messages.
     */
    private String currentRaceID;
    
    /**
     * Reference time point for time specifications
     */
    private TimePoint referenceTimePoint;
    
    /**
     * feference location for location / lat/lng specifications
     */
    private Position referenceLocation;
    
    public SwissTimingReplayToDomainAdapter() {
        racePerRaceID = new HashMap<>();
        trackedRacePerRaceID = new HashMap<>();
    }

    @Override
    public void referenceTimestamp(long referenceTimestampMillis) {
        referenceTimePoint = new MillisecondsTimePoint(referenceTimestampMillis);
    }

    @Override
    public void referenceLocation(int latitude, int longitude) {
        referenceLocation = new DegreePosition(((double) latitude)/10000000., ((double) longitude)/10000000.); 
    }

    @Override
    public void raceID(String raceID) {
        currentRaceID = raceID;
    }

    @Override
    public void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus,
            short distanceToNextMark, Weather weather, short humidity, short temperature, String messageText,
            byte cFlag, byte rFlag, byte duration, short nm) {
        // TODO Auto-generated method stub
        super.frameMetaData(cid, raceTime, startTime, estimatedStartTime, raceStatus, distanceToNextMark, weather, humidity,
                temperature, messageText, cFlag, rFlag, duration, nm);
    }

    @Override
    public void competitor(int hashValue, String nation, String sailNumber, String name,
            CompetitorStatus competitorStatus, BoatType boatType, short cRank_Bracket, short cnPoints_x10_Bracket,
            short ctPoints_x10_Winner) {
        // TODO Auto-generated method stub
        super.competitor(hashValue, nation, sailNumber, name, competitorStatus, boatType, cRank_Bracket, cnPoints_x10_Bracket,
                ctPoints_x10_Winner);
    }

    @Override
    public void mark(MarkType markType, String name, byte index, String id1, String id2, short windSpeedInKnots,
            short trueWindDirectionInDegrees) {
        // TODO Auto-generated method stub
        super.mark(markType, name, index, id1, id2, windSpeedInKnots, trueWindDirectionInDegrees);
    }

    @Override
    public void trackers(int hashValue, int latitude, int longitude, short cog, short sog_Knots_x10, short average_sog,
            short vmg_Knots_x10, CompetitorStatus competitorStatus, short rank, short distanceToLeader_meters,
            short distanceToNextMark_meters, short nextMark, short pRank, short ptPoints, short pnPoints) {
        // TODO Auto-generated method stub
        super.trackers(hashValue, latitude, longitude, cog, sog_Knots_x10, average_sog, vmg_Knots_x10, competitorStatus, rank,
                distanceToLeader_meters, distanceToNextMark_meters, nextMark, pRank, ptPoints, pnPoints);
    }

    @Override
    public void ranking(int hashValue, short rank, short rankIndex, short racePoints,
            CompetitorStatus competitorStatus, short finishRank, short finishRankIndex, int gap_seconds,
            int raceTime_seconds) {
        // TODO Auto-generated method stub
        super.ranking(hashValue, rank, rankIndex, racePoints, competitorStatus, finishRank, finishRankIndex, gap_seconds,
                raceTime_seconds);
    }

}
