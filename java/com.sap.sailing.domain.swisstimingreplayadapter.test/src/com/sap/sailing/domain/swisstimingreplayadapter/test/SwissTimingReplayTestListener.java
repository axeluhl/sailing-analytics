package com.sap.sailing.domain.swisstimingreplayadapter.test;

import com.sap.sailing.domain.swisstimingreplayadapter.CompetitorStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayListener;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.BoatType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.MarkType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.RaceStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.Weather;


public class SwissTimingReplayTestListener implements SwissTimingReplayListener {

    int keyFrameIndexSum;
    int keyFrameIndexPositionCount;
    int eotCount;
    int frameCount;
    int referenceTimestampCount;
    int referenceLocationCount;
    int rsc_cidCount;
    int competitorsCountSum;
    int competitorsCount;
    int markCount;
    int trackersCountSum;
    int trackersCount;
    int rankingsCountSum;
    int rankingsCount;
    int rankingMarkCount;

    public SwissTimingReplayTestListener() {
    }

    @Override
    public void raceID(String raceID) {
        rsc_cidCount++;
    }

    @Override
    public void referenceTimestamp(long referenceTimestampMillis) {
        referenceTimestampCount++;
    }

    @Override
    public void referenceLocation(int latitude, int longitude) {
        referenceLocationCount++;
    }

    @Override
    public void keyFrameIndexPosition(int keyFrameIndexPosition) {
        keyFrameIndexPositionCount++;
    }

    @Override
    public void keyFrameIndex(int keyFrameIndex) {
        keyFrameIndexSum += keyFrameIndex;
    }

    @Override
    public void competitorsCount(short competitorsCount) {
            competitorsCountSum += competitorsCount;
    }

    @Override
    public void competitor(int hashValue, String threeLetterIOCCode, String sailNumberOrID, String name,
            CompetitorStatus competitorStatus, BoatType boatType, short cRank_Bracket, short cnPoints_x10_Bracket,
            short ctPoints_x10_Winner) {
        competitorsCount++;
    }

    @Override
    public void mark(MarkType markType, String name, byte index, String id1, String id2, short windSpeedInKnots,
            short windDirection) {
            markCount++;
    }

    @Override
    public void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus,
            short distanceToNextMark, Weather weather, short humidity, short temperature, String messageText,
            byte cFlag, byte rFlag, byte duration, short nm) {
        frameCount++;
    }

    @Override
    public void ranking(int hashValue, short rank, short rankIndex, short racePoints,
            CompetitorStatus competitorStatus, short finishRank, short finishRankIndex, int gap, int raceTime) {
            rankingsCount++;
    }

    @Override
    public void rankingsCount(short entriesCount) {
        rankingsCountSum += entriesCount;
    }

    @Override
    public void rankingMark(short marksRank, short marksRankIndex, int marksGap, int marksRaceTime) {
        rankingMarkCount++;
    }

    @Override
    public void trackersCount(short trackersCount) {
        trackersCountSum += trackersCount;
    }

    @Override
    public void trackers(int hashValue, int latitude, int longitude, short cog, short sog, short average_sog,
            short vmg, CompetitorStatus competitorStatus, short rank, short dtl, short dtnm, short nm, short pRank,
            short ptPoints, short pnPoints) {
        trackersCount++;
    }

    @Override
    public void eot() {
        eotCount++;
    }

    @Override
    public void progress(double progress) {
    }

}
