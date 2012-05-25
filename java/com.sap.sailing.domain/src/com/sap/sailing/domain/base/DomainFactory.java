package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    /**
     * Looks up or, if not found, creates a {@link Nationality} object and re-uses <code>threeLetterIOCCode</code> also as the
     * nationality's name.
     */
    Nationality getOrCreateNationality(String threeLetterIOCCode);

    Buoy getOrCreateBuoy(String id);

    Gate createGate(Buoy left, Buoy right, String name);
    
    /**
     * The waypoint created is weakly cached so that when requested again by
     * {@link #getExistingWaypointById(Serializable)} it is found.
     */
    Waypoint createWaypoint(ControlPoint controlPoint);
    
    Waypoint getExistingWaypointById(Serializable id);

    /**
     * Caches an existing waypoint. This is useful, e.g., after de-serialization of a waypoint that has a yet unknown ID.
     * 
     * @param waypoint a waypoint that hasn't been cached in this domain factory before
     */
    void cacheWaypoint(Waypoint waypoint);

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);
    
    BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind);
    
    Competitor getExistingCompetitorById(Serializable competitorId);
    
    Competitor createCompetitor(Serializable id, String name, Team team, Boat boat);
    
    Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat);
    
    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory.
     * The stream returned by this method can be used 
     */
    ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;

}
