package com.sap.sailing.server;


/**
 * A JMX management bean that lets JMX clients such as JConsole manage an instance of {@link RacingEventService}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RacingEventServiceMXBean {
    public int getNumberOfLeaderboards();
    public long getNumberOfTrackedRacesToRestore();
    public int getNumberOfTrackedRacesRestored();
}
