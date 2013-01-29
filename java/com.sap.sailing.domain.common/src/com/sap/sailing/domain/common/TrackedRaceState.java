package com.sap.sailing.domain.common;

import java.util.HashMap;

/**
 * <p>Races undergo a variety of different states in our application. They may be created with a competitor list, maybe not
 * having a confirmed course yet and not even an expected start time. Later, the start time may be set. The race starts,
 * may be abandoned, restarted, running, finishing, finished, protests, completed, race committee confirmed.</p>
 * 
 * <p>When a race is then re-loaded or re-connected while it is running, stored data may start to load, be loading, be done
 * loading. Live data may continue to be received, done receiving.</p>
 * 
 * <p>The race may be archived (stored persistently in our database), loading from the DB, done loading from the DB. It has
 * to be possible to update an archived race in case something changes after it has been archived.</p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public enum TrackedRaceState implements LifecycleState {
    
    /**
     * Race has just been initialized, no further actions
     * has been applied. 
     * 
     * This state has no additional properties.
     */
    INITIAL,
    
    /**
     * Race data is being loaded into memory. This state indicates that stored data is being received
     * from the tracking server.
     * 
     * PROPERTY_LOADING_INDICATOR indicates progress of loading and can take values in the range of 0.0 to 1.0 
     *         where 0.0 means no progress yet and 1.0 means loading of stored data has completed.
     *         1.0 will, however, hardly be seen in state {@link TrackedRaceState#LOADING} because the status will probably
     *         already have transitioned to {@link TrackedRaceState#TRACKING} or {@link TrackedRaceState#FINISHED}.
     */
    LOADING_STORED_DATA,
    
    /**
     * Tracking data is being received. This state does not indicate that the race has finished or all
     * data has been received. 
     * 
     * This state has no additional properties.
     */
    TRACKING_LIVE_DATA,
    
    /**
     * The race is completely loaded into memory. No further changes will occur.
     * 
     * This state has no additional properties.
     */
    FINISHED;
    
    public static String PROPERTY_LOADING_INDICATOR = "LOADING_INDICATOR";

    private HashMap<String, Object> properties = new HashMap<String, Object>();
    
    @Override
    public Object getProperty(String name) {
        synchronized(properties) {
            return this.properties.get(name);
        }
    }

    @Override
    public void updateProperty(String name, Object value) {
        synchronized(properties) {
            this.properties.put(name, value);
        }
    }

    @Override
    public HashMap<String, Object> allProperties() {
        return this.properties;
    }

}
