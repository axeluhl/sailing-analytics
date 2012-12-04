package com.sap.sailing.domain.swisstimingreplayadapter;

public interface SwissTimingReplayListener {

    void referenceTimestamp(long referenceTimestamp);

    void referenceLocation(int latitude, int longitude);

    void keyFrameIndex(int keyFrameIndex);

    void keyFrameIndexPosition(int keyFrameIndexPosition);

    void rsc_cid(String text);

    void mark(MarkType markType, String identifier, byte index, String id1, String id2, short windSpeed, short windDirection);

    void competitorsCount(short competitorsCount);

    void competitor(int hashValue, String nation, String sailNumber, String name, CompetitorStatus competitorStatus, BoatType boatType,
            short cRank_Bracket, short cnPoints_x10_Bracket, short ctPoints_x10_Winner);

    void frame(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus, short distanceToNextMark,
            Weather weather, short humidity, short temperature, String messageText, byte cFlag, byte rFlag,
            byte duration, short nm);

    void ranking(int hashValue, short rank, short rankIndex, short racePoints, CompetitorStatus competitorStatus, short finishRank,
            short finishRankIndex, int gap, int raceTime);

    void rankingsCount(short entriesCount);

    void rankingMark(short marksRank, short marksRankIndex, int marksGap, int marksRaceTime);

    void trackersCount(short trackersCount);

    void trackers(int hashValue, int latitude, int longitude, short cog, short sog, short average_sog, short vmg,
            CompetitorStatus competitorStatus, short rank, short dtl, short dtnm, short nm, short pRank, short ptPoints, short pnPoints);

    void illegalState(String message);

    void unknownMessageIdentificationCode(byte messageIdentificationCode);

    void eot();

}
