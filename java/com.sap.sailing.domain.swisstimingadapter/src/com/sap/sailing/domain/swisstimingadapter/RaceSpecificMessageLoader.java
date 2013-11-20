package com.sap.sailing.domain.swisstimingadapter;

import java.util.List;

/**
 * When a {@link SailMasterConnector} starts tracking a specific race, events recorded previously about this race
 * may be loaded from some durable store before the live reception of events from the SailMaster instance starts.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceSpecificMessageLoader {
    /**
     * Loads all race-specific messages for the race with ID <code>raceID</code> from the store represented by this
     * object. The messages can be expected to have their {@link SailMasterMessage#getSequenceNumber() sequence number}
     * properly set. The result list is expected to be in ascending order regarding this sequence number and always
     * has to be a valid, non-<code>null</code> and perhaps empty list.
     */
    List<SailMasterMessage> loadRaceMessages(String raceID);
    
    void storeSailMasterMessage(SailMasterMessage message);

    Iterable<Race> getRaces();

    Race getRace(String raceID);
    
    boolean hasRaceStartlist(String raceID);

    boolean hasRaceCourse(String raceID);
}
