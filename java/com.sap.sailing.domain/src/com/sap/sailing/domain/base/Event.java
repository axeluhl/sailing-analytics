package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

/**
 * An event is a group of {@link Regatta regattas} carried out at a common venue within a common time frame. For
 * example, Kiel Week 2011 is an event, and the International German Championship 2011 held, e.g., in Travemünde, is an event,
 * too.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Event extends Named {
    /**
     * @return a non-<code>null</code> venue for this event
     */
    Venue getVenue();

    Iterable<Regatta> getRegattas();
    
    void addRegatta(Regatta regatta);
    
    void removeRegatta(Regatta regatta);
}
