package com.sap.sailing.domain.base;

/**
 * An event is a group of {@link Regatta regattas} carried out at a common venue within a common time frame. For
 * example, Kiel Week is an event, and the International German Championship held, e.g., in Travemünde, is an event,
 * too.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Event {
    Iterable<Regatta> getRegattas();
}
