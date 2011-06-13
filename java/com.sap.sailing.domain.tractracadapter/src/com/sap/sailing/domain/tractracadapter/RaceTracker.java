package com.sap.sailing.domain.tractracadapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.util.Util.Triple;

public interface RaceTracker {

    /**
     * Stops tracking the race and removes the {@link TrackedRace} object one gets from calling
     * {@link #getTrackedEvent()}.{@link TrackedEvent#getTrackedRace(RaceDefinition) getTrackedRace(}{@link #getRace() getRace())}
     * from the {@link #getTrackedEvent() tracked event}.
     */
    void stop() throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Event getEvent();

    /**
     * Non-blocking call that returns <code>null</code> if the {@link RaceDefinition} for the TracTrac Event
     * hasn't been created yet, e.g., because the course definition hasn't been received yet or the listener
     * for receiving course information hasn't been registered (yet).
     */
    RaceDefinition getRace();

    RaceHandle getRaceHandle();

    DynamicTrackedEvent getTrackedEvent();

    WindStore getWindStore();
    
    Triple<URL, URI, URI> getURLs();
    
}
