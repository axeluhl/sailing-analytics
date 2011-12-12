package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;

public interface RaceTracker {
    /**
     * Stops tracking the races.
     */
    void stop() throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Event getEvent();

    /**
     * Returns the races currently being tracked by this tracker. Non-blocking call that returns <code>null</code> if
     * the {@link RaceDefinition} for a TracTrac Event hasn't been created yet, e.g., because the course definition
     * hasn't been received yet or the listener for receiving course information hasn't been registered (yet).
     */
    Set<RaceDefinition> getRaces();

    RacesHandle getRaceHandle();

    DynamicTrackedEvent getTrackedEvent();
    
    WindStore getWindStore();

    /**
     * returns a unique key for this tracker which can, e.g., be used as a key in a {@link Map}
     */
    Object getID();
    
}
