package com.sap.sailing.domain.tractracadapter;

import java.io.IOException;
import java.net.MalformedURLException;

public interface EventTracker {

    public abstract void stop() throws MalformedURLException, IOException, InterruptedException;

    public abstract com.sap.sailing.domain.base.Event getEvent();

}
