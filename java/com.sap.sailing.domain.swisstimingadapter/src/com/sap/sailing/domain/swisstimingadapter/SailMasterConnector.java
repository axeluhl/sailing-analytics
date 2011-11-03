package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Map;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.util.Util.Pair;

public interface SailMasterConnector {
    SailMasterMessage sendRequestAndGetResponse(MessageType messageType, String... args) throws UnknownHostException, IOException, InterruptedException;
    
    Iterable<Race> getRaces() throws UnknownHostException, IOException, InterruptedException;
    
    Course getCourse(String raceID) throws UnknownHostException, IOException, InterruptedException;
    
    StartList getStartList(String raceID) throws UnknownHostException, IOException, InterruptedException;
    
    TimePoint getStartTime(String raceID) throws UnknownHostException, IOException, ParseException, InterruptedException;
    
    /**
     * @return a map with all indices of those marks already passed by the leading boat as key; pairs of mark passing
     *         times and boat ID of the boat passing that mark first as values.
     * @throws InterruptedException 
     */
    Map<Integer, Pair<TimePoint, String>> getDeltaClockAtMark(String raceID) throws UnknownHostException, IOException, NumberFormatException, ParseException, InterruptedException;
    
    double getDistanceToMarkInMeters(String raceID, int markIndex, String boatID) throws UnknownHostException, IOException, InterruptedException;
    
    double getCurrentBoatSpeedInMetersPerSecond(String raceID, String boatID) throws UnknownHostException, IOException, InterruptedException;
    
    double getAverageBoatSpeedInMetersPerSecond(String raceID, String leg, String boatID) throws UnknownHostException, IOException, InterruptedException;
    
    double getDistanceBetweenBoatsInMeters(String raceID, String boatID1, String boatID2) throws UnknownHostException, IOException, InterruptedException;

    /**
     * Retrieves the mark passing times for a boat ID within a race specified by <code>raceID</code>.
     * 
     * @return keys are the mark indices; values are the pairs telling the requested boat's rank at the key mark and the
     *         number of milliseconds that passed between the race start and the point in time at which the boat with ID
     *         <code>boatID</code> passed the mark.
     * @throws InterruptedException 
     */
    Map<Integer, Pair<Integer, Long>> getMarkPassingTimesInMillisecondsSinceRaceStart(String raceID, String boatID)
            throws UnknownHostException, IOException, InterruptedException;
    
    void addSailMasterListener(SailMasterListener listener);
    
    void removeSailMasterListener(SailMasterListener listener);

    SailMasterMessage receiveMessage(MessageType type) throws InterruptedException;
}
