package com.sap.sailing.domain.swisstimingreplayadapter.impl;

import com.sap.sailing.domain.swisstimingreplayadapter.CompetitorStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;

public abstract class SwissTimingReplayAdapter implements SwissTimingReplayListener {

    @Override
    public void keyFrameIndex(int keyFrameIndex) {}

    @Override
    public void keyFrameIndexPosition(int keyFrameIndexPosition) {}

    @Override
    public void referenceTimestamp(long referenceTimestampMillis) {}

    @Override
    public void referenceLocation(int latitude, int longitude) {}

    @Override
    public void raceID(String raceID) {}

    @Override
    public void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus,
            short distanceToNextMark, Weather weather, short humidity, short temperature, String messageText,
            byte cFlag, byte rFlag, byte duration, short nextMark) {}

    @Override
    public void competitorsCount(short competitorsCount) {}

    @Override
    public void competitor(int hashValue, String nation, String sailNumber, String name,
            CompetitorStatus competitorStatus, BoatType boatType, short cRank_Bracket, short cnPoints_x10_Bracket,
            short ctPoints_x10_Winner) {}

    @Override
    public void mark(MarkType markType, String name, byte index, String id1, String id2, short windSpeedInKnots,
            short trueWindDirectionInDegrees) {}

    @Override
    public void trackersCount(short trackersCount) {}

    @Override
    public void trackers(int hashValue, int latitude, int longitude, short cog, short sog_Knots_x10, short average_sog,
            short vmg_Knots_x10, CompetitorStatus competitorStatus, short rank, short distanceToLeader_meters,
            short distanceToNextMark_meters, short nextMark, short pRank, short ptPoints, short pnPoints) {}

    @Override
    public void rankingsCount(short entriesCount) {}

    @Override
    public void ranking(int hashValue, short rank, short rankIndex, short racePoints,
            CompetitorStatus competitorStatus, short finishRank, short finishRankIndex, int gap_seconds,
            int raceTime_seconds) {}

    @Override
    public void rankingMark(short marksRank, short marksRankIndex, int marksGap_seconds, int marksRaceTime_seconds) {}

    @Override
    public void eot() {}

    @Override
    public void progress(double progress) {}

}
