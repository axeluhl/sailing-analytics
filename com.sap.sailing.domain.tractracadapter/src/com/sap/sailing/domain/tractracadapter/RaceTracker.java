package com.sap.sailing.domain.tractracadapter;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.RaceDefinition;

public interface RaceTracker {

    void stop() throws MalformedURLException, IOException, InterruptedException;

    com.sap.sailing.domain.base.Event getEvent();

    RaceDefinition getRace();
    
}
