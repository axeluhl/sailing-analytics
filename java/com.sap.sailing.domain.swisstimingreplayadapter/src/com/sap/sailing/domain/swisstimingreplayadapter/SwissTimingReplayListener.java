package com.sap.sailing.domain.swisstimingreplayadapter;

import com.sap.sailing.domain.swisstimingreplayadapter.impl.BoatType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.MarkType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.RaceStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.Weather;

/**
 * Implementations are passed to {@link SwissTimingReplayParserImpl#loadRaceData(String, SwissTimingReplayListener)} for getting notified of about content parsed from SwissTiming's binary replay file format.
 * 
 *   The protocol is:
 *   
 *   1 x keyFrameIndex
 *   n x keyFrameIndexPosition
 *   1 x eot
 *   n x [
 *          1 x referenceTimestamp
 *          1 x referenceLocation 
 *          1 x rsc_cid    
 *          1 x competitorsCount
 *          competitorsCount x competitor
 *          m x mark
 *          1 x trackersCount
 *          trackersCount x trackers
 *          1 x rankingsCount
 *          rankingsCount x ranking [
 *                                     m x rankingMark
 *                                  ]
 *          1 x eot
 *          
 *       ]
 *   
 * 
 * @author D047974 - Jens Rommel
 *
 */
public interface SwissTimingReplayListener {

    void keyFrameIndex(int keyFrameIndex);

    void keyFrameIndexPosition(int keyFrameIndexPosition);

    void referenceTimestamp(long referenceTimestamp);

    void referenceLocation(int latitude, int longitude);

    void rsc_cid(String text);

    void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus, short distanceToNextMark,
            Weather weather, short humidity, short temperature, String messageText, byte cFlag, byte rFlag,
            byte duration, short nm);
    
    void competitorsCount(short competitorsCount);

    void competitor(int hashValue, String nation, String sailNumber, String name, CompetitorStatus competitorStatus, BoatType boatType,
            short cRank_Bracket, short cnPoints_x10_Bracket, short ctPoints_x10_Winner);

    void mark(MarkType markType, String identifier, byte index, String id1, String id2, short windSpeed_Knots, short windDirection);
    
    void trackersCount(short trackersCount);
    
    void trackers(int hashValue, int latitude, int longitude, short cog, short sog_Knots_x10, short average_sog, short vmg_Knots_x10,
            CompetitorStatus competitorStatus, short rank, short distanceToLeader_meters, short distanceToNextMark_meters, short nextMark, short pRank, short ptPoints, short pnPoints);
    
    void rankingsCount(short entriesCount);
    
    void ranking(int hashValue, short rank, short rankIndex, short racePoints, CompetitorStatus competitorStatus, short finishRank,
            short finishRankIndex, int gap_seconds, int raceTime_seconds);

    void rankingMark(short marksRank, short marksRankIndex, int marksGap_seconds, int marksRaceTime_seconds);

    void eot();

}