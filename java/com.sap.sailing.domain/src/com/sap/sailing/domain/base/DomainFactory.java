package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface DomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    /**
     * Looks up or, if not found, creates a {@link Nationality} object and re-uses <code>threeLetterIOCCode</code> also as the
     * nationality's name.
     */
    Nationality getOrCreateNationality(String threeLetterIOCCode);

    SingleMark getOrCreateSingleMark(String id);
    
    /**
     * If the single mark with ID <code>id</code> already exists, it is returned. Its color may differ from <code>color</code>
     * in that case. Otherwise, a new {@link SingleMark} is created with <code>color</code> as its {@link SingleMark#getColor()} 
     * and <code>shape</code> as its {@link SingleMark#getShape()}.
     */
    SingleMark getOrCreateSingleMark(String id, String color, String shape, String pattern);

    Gate createGate(SingleMark left, SingleMark right, String name);
    
    /**
     * The waypoint created is weakly cached so that when requested again by
     * {@link #getExistingWaypointById(Waypoint)} it is found.
     */
    Waypoint createWaypoint(ControlPoint controlPoint);
    
    Waypoint getExistingWaypointById(Waypoint waypointPrototype);

    /**
     * Atomically checks if a waypoint by an equal {@link Waypoint#getId()} as <code>waypoint</code> exists in this domain factory's
     * waypoint cache. If so, the cached waypoint is returned. Otherwise, <code>waypoint</code> is added to the cache and returned.
     */
    Waypoint getExistingWaypointByIdOrCache(Waypoint waypoint);

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);
    
    BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind);
    
    /**
     * Like {@link #getOrCreateBoatClass(String, boolean)}, only that a default for <code>typicallyStartsUpwind</code> based
     * on the boat class name is calculated.
     */
    BoatClass getOrCreateBoatClass(String name);
    
    Competitor getExistingCompetitorById(Serializable competitorId);
    
    Competitor createCompetitor(Serializable id, String name, Team team, Boat boat);
    
    Competitor getOrCreateCompetitor(Serializable competitorId, String name, Team team, Boat boat);
    
    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory.
     * The stream returned by this method can be used 
     */
    ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;
    
    ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType);

}
