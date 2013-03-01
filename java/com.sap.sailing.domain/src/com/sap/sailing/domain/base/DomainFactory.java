package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.MarkPassing;

public interface DomainFactory extends SharedDomainFactory {
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    /**
     * Looks up or, if not found, creates a {@link Nationality} object and re-uses <code>threeLetterIOCCode</code> also as the
     * nationality's name.
     */
    Nationality getOrCreateNationality(String threeLetterIOCCode);

    /**
     * The name will also be used as the mark's ID. If you have a unique ID, use {@link #getOrCreateMark(Serializable, String)} instead.
     */
    Mark getOrCreateMark(String name);
    
    /**
     * Since some ID types, such as {@link UUID}, cannot be serialized as objects to a GWT client, only the
     * {@link Object#toString()} representations of those IDs are serialized to the clients. When a client then requests
     * to identify a mark again, only the ID's string representation will be submitted to the server and now needs to be
     * mapped to the actual ID. This domain factory keeps a mapping of all mark ID's string representations to the
     * actual ID for all marks ever managed through any of the <code>getOrCreateMark(...)</code> overloads.
     * <p>
     * 
     * This method first looks up the actual ID whose string representation is <code>toStringRepresentationOfID</code>
     * and then calls {@link #getOrCreateMark(Serializable, String)} with the result and the <code>name</code>
     * parameter, or with <code>ToStringRepresentationOfID</code> and <code>name</code> in case the string
     * representation of the ID is not known. So in the latter case, the string is used as the ID for the new mark.
     */
    Mark getOrCreateMark(String toStringRepresentationOfID, String name);
    
    Mark getOrCreateMark(Serializable id, String name);
    
    /**
     * If the single mark with ID <code>id</code> already exists, it is returned. Its color may differ from <code>color</code>
     * in that case. Otherwise, a new {@link Mark} is created with <code>color</code> as its {@link Mark#getColor()} 
     * and <code>shape</code> as its {@link Mark#getShape()}.
     */
    Mark getOrCreateMark(Serializable id, String name, MarkType type, String color, String shape, String pattern);

    /**
     * @param name also uses the name as the gate's ID; if you have a real ID, use {@link #createGate(Serializable, Mark, Mark, String)} instead
     */
    Gate createGate(Mark left, Mark right, String name);
    
    Gate createGate(Serializable id, Mark left, Mark right, String name);

    /**
     * The waypoint created is weakly cached so that when requested again by
     * {@link #getExistingWaypointById(Waypoint)} it is found.
     */
    Waypoint createWaypoint(ControlPoint controlPoint, NauticalSide passingSide);
    
    Waypoint getExistingWaypointById(Waypoint waypointPrototype);

    /**
     * Atomically checks if a waypoint by an equal {@link Waypoint#getId()} as <code>waypoint</code> exists in this domain factory's
     * waypoint cache. If so, the cached waypoint is returned. Otherwise, <code>waypoint</code> is added to the cache and returned.
     */
    Waypoint getExistingWaypointByIdOrCache(Waypoint waypoint);

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);
    
    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory.
     * The stream returned by this method can be used 
     */
    ObjectInputStreamResolvingAgainstDomainFactory createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;
    
    ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType);

}
