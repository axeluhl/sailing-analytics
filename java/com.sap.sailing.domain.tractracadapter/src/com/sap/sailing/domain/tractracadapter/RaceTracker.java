package com.sap.sailing.domain.tractracadapter;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.RaceDefinition;

public interface RaceTracker {

    void stop() throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Event getEvent();

    /**
     * Non-blocking call that returns <code>null</code> if the {@link RaceDefinition} for the TracTrac Event
     * hasn't been created yet, e.g., because the course definition hasn't been received yet or the listener
     * for receiving course information hasn't been registered (yet).
     */
    RaceDefinition getRace();

    RaceHandle getRaceHandle();
    
}
