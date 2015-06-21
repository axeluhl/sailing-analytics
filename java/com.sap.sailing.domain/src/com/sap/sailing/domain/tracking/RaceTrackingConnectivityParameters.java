package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;




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
     * @param raceLogResolver TODO
     */
    RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore, GPSFixStore gpsFixStore, RaceLogResolver raceLogResolver) throws Exception;
    
    /**
     * Starts a {@link RaceTracker}, associating the resulting races with the {@link Regatta} passed as argument
     * instead of using the tracker's domain factory to obtain a default {@link Regatta} object for the tracking
     * parameters. This is particularly useful if a predefined regatta with {@link Series} and {@link Fleet}s
     * is to be used.
     * @param raceLogResolver TODO
     */
    RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore, GPSFixStore gpsFixStore, RaceLogResolver raceLogResolver) throws Exception;
    
    /**
     * Deliver an ID object equal to that of the {@link RaceTracker#getID()} delivered by the {@link RaceTracker}
     * that will be created from these parameters by calling {@link #createRaceTracker(TrackedRegattaRegistry)}.
     */
    Object getTrackerID();

    /** 
     * Gets the configured delay time to the 'live' timepoint for this tracker
     */
    public long getDelayToLiveInMillis();
}
