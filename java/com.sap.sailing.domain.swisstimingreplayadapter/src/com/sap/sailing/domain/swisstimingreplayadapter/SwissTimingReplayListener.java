package com.sap.sailing.domain.swisstimingreplayadapter;

import com.sap.sailing.domain.swisstimingreplayadapter.impl.BoatType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.MarkType;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.RaceStatus;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.SwissTimingReplayParserImpl;
import com.sap.sailing.domain.swisstimingreplayadapter.impl.Weather;

/**
 * Implementations are passed to {@link SwissTimingReplayParserImpl#loadRaceData(String, SwissTimingReplayListener)} for
 * getting notified of about content parsed from SwissTiming's binary replay file format.
 * <p>
 * 
 * The protocol is:
 * 
 * <pre>
 * 
 *   1 x keyFrameIndex
 *   n x keyFrameIndexPosition
 *   1 x eot
 *   1 x frameMetaData
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
 * </pre>
 * 
 * For each tracked object on the course, a "competitor" message is contained in each frame. The <code>sailNumber</code>
 * parameter corresponds to the {@link #mark(MarkType, String, byte, String, String, short, short)} message's
 * <code>id1/id2</code> parameter if a tracked object is used as part of a mark. The
 * {@link #mark(MarkType, String, byte, String, String, short, short)} messages define the course layout by
 * providing the marks in the order they must be passed.
 * 
 * @author D047974 - Jens Rommel
 * 
 */
public interface SwissTimingReplayListener {

    void keyFrameIndex(int keyFrameIndex);

    void keyFrameIndexPosition(int keyFrameIndexPosition);

    void referenceTimestamp(long referenceTimestamp);

    /**
     * @param latitude latitude in degrees * 10000000
     * @param longitude longitude in degrees * 10000000
     */
    void referenceLocation(int latitude, int longitude);

    void rsc_cid(String text);

    void frameMetaData(byte cid, int raceTime, int startTime, int estimatedStartTime, RaceStatus raceStatus, short distanceToNextMark,
            Weather weather, short humidity, short temperature, String messageText, byte cFlag, byte rFlag,
            byte duration, short nm);
    
    void competitorsCount(short competitorsCount);

    void competitor(int hashValue, String nation, String sailNumber, String name, CompetitorStatus competitorStatus, BoatType boatType,
            short cRank_Bracket, short cnPoints_x10_Bracket, short ctPoints_x10_Winner);

    /**
     * @param name
     *            a symbolic name, such as "Start" or "Finish"
     * @param index
     *            the mark's ordinal number in the course's mark sequence, starting with 0
     * @param id1
     *            the "competitor" ID for the first mark's part / buoy, corresponds with <code>sailNumberOrID</code> of
     *            the {@link #competitor(int, String, String, String, CompetitorStatus, BoatType, short, short, short)}
     *            messages
     * @param id2
     *            <code>null</code>, if the mark is a single buoy, otherwise the "competitor" ID, corresponds with
     *            <code>sailNumberOrID</code> of the
     *            {@link #competitor(int, String, String, String, CompetitorStatus, BoatType, short, short, short)}
     *            messages
     * @param windSpeedInKnots
     *            the true wind speed in knots at the mark, or -1 if no wind is measured at this mark
     * @param trueWindDirectionInDegrees
     *            the true wind direction in degrees at the mark, or -1 if no wind is measured at this mark
     */
    void mark(MarkType markType, String name, byte index, String id1, String id2, short windSpeedInKnots,
            short trueWindDirectionInDegrees);
    
    void trackersCount(short trackersCount);
    
    void trackers(int hashValue, int latitude, int longitude, short cog, short sog_Knots_x10, short average_sog, short vmg_Knots_x10,
            CompetitorStatus competitorStatus, short rank, short distanceToLeader_meters, short distanceToNextMark_meters, short nextMark, short pRank, short ptPoints, short pnPoints);
    
    void rankingsCount(short entriesCount);
    
    void ranking(int hashValue, short rank, short rankIndex, short racePoints, CompetitorStatus competitorStatus, short finishRank,
            short finishRankIndex, int gap_seconds, int raceTime_seconds);

    void rankingMark(short marksRank, short marksRankIndex, int marksGap_seconds, int marksRaceTime_seconds);

    void eot();

}