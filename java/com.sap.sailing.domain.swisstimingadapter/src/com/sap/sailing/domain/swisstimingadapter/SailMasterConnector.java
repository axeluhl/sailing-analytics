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
    
    /**
     * Adds the listener and ensures that the connector is actually connected, even if no request has explicitly
     * been sent, so that the connector will at least receive spontaneous events.
     */
    void addSailMasterListener(SailMasterListener listener) throws UnknownHostException, IOException;
    
    void removeSailMasterListener(SailMasterListener listener);

    SailMasterMessage receiveMessage(MessageType type) throws InterruptedException;
    
    /**
     * Enables receiving RPD (Race Position Data) events to be emitted by the server. If such events are received, they
     * are forwarded to {@link #addSailMasterListener(SailMasterListener) registered} listeners by calling their
     * {@link SailMasterListener#receivedRacePositionData(String, RaceStatus, TimePoint, TimePoint, Long, Integer, com.sap.sailing.domain.base.Distance, java.util.Collection)}
     * method.
     */
    void enableRacePositionData() throws UnknownHostException, IOException, InterruptedException;
    
    /**
     * Stops the server from emitting RPD (Race Position Data) events.
     */
    void disableRacePositionData() throws UnknownHostException, IOException, InterruptedException;
}
