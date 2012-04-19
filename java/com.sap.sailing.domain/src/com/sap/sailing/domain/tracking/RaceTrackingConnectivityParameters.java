package com.sap.sailing.domain.tracking;




/**
 * Different tracking providers require different sets of arguments to start tracking a race.
 * This interface represents the functionality required to start tracking a race, agnostic of the
 * particular parameters and ways of launching the connector. Use {@link #createRaceTracker()} to
 * create the tracker that starts tracking the race identified by these tracking parameters.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceTrackingConnectivityParameters {
    /**
     * Starts a {@link RaceTracker} using the connectivity parameters provided by this object.
     */
    RaceTracker createRaceTracker(TrackedEventRegistry trackedEventRegistry) throws Exception;
    
    /**
     * Deliver an ID object equal to that of the {@link RaceTracker#getID()} delivered by the {@link RaceTracker}
     * that will be created from these parameters by calling {@link #createRaceTracker(TrackedEventRegistry)}.
     */
    Object getTrackerID();

}
