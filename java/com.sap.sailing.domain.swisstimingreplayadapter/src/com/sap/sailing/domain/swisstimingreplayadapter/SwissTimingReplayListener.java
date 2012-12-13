package com.sap.sailing.domain.swisstimingreplayadapter;

/**
 * Implementations are passed to {@link SwissTimingReplayService#loadRaceData(String, SwissTimingReplayListener)} for getting notified of about content parsed from SwissTiming's binary replay file format.
 * 
 *   The protocol is
 *   1 x keyFrameIndex
 *   n x keyFrameIndexPosition
 *   1 x eot
 *   n x [
 *          1 x referenceTimestamp
 *          1 x referenceLocation 
 *          1 x rsc_cid    
 *          1 x competitorsCount
 *          competitorsCount x competitor
 *          n x mark
 *          1 x trackersCount
 *          trackersCount x trackers
 *          1 x rankingsCount
 *          rankingsCount x ranking [
 *                                     n x rankingMark
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
            CompetitorStatus competitorStatus, short rank, short distanceToLeader, short distanceToNextMark, short nextMark, short pRank, short ptPoints, short pnPoints);
    
    void rankingsCount(short entriesCount);
    
    void ranking(int hashValue, short rank, short rankIndex, short racePoints, CompetitorStatus competitorStatus, short finishRank,
            short finishRankIndex, int gap, int raceTime);

    void rankingMark(short marksRank, short marksRankIndex, int marksGap, int marksRaceTime);

    void eot();

    /**
     * Called when an unknown message identification codes has been encountered. 
     * See {@link MessageIdentificationCodes} for supported codes.
     * @param messageIdentificationCode
     */
    void unknownMessageIdentificationCode(byte messageIdentificationCode);

    /**
     * Called when STX was expected but startByte has been encountered.
     * @param startByte
     */
    void unexpectedStartByte(byte startByte);

    /**
     * Called when the actual payload was smaller than announced in the message header. 
     * @param remainingPayloadSize
     */
    void payloadMismatch(short remainingPayloadSize);

    /**
     * Called when the end of a message was expected but a non ETX/EOT byte has been encountered. 
     * @param endByte
     */
    void prematureEndOfData(byte endByte);

}
