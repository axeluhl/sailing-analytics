package com.sap.sailing.domain.swisstimingadapter;

/**
 * When a {@link SailMasterConnector} starts tracking a specific race, events recorded previously about this race
 * may be loaded from some durable store before the live reception of events from the SailMaster instance starts.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceSpecificMessageLoader {
    
}
