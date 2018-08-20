package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Color;

public interface SharedDomainFactory extends CompetitorFactory, BoatFactory {

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
    Mark getOrCreateMark(Serializable id, String name, MarkType type, Color color, String shape, String pattern);
    
    /**
     * @see #getOrCreateMark(String, String)
     */
    Mark getOrCreateMark(String toStringRepresentationOfID, String name, MarkType type, Color color, String shape, String pattern);

    /**
     * @param name also uses the name as the gate's ID; if you have a real ID, use {@link #createControlPointWithTwoMarks(Serializable, Mark, Mark, String)} instead
     */
    ControlPointWithTwoMarks createControlPointWithTwoMarks(Mark left, Mark right, String name);

    ControlPointWithTwoMarks createControlPointWithTwoMarks(Serializable id, Mark left, Mark right, String name);

    /**
     * The waypoint created is weakly cached so that when requested again by
     * {@link #getExistingWaypointById(Waypoint)} it is found.
     */
    Waypoint createWaypoint(ControlPoint controlPoint, PassingInstruction passingInstruction);

    Waypoint getExistingWaypointById(Waypoint waypointPrototype);

    /**
     * Atomically checks if a waypoint by an equal {@link Waypoint#getId()} as <code>waypoint</code> exists in this domain factory's
     * waypoint cache. If so, the cached waypoint is returned. Otherwise, <code>waypoint</code> is added to the cache and returned.
     */
    Waypoint getExistingWaypointByIdOrCache(Waypoint waypoint);

    BoatClass getOrCreateBoatClass(String name, boolean typicallyStartsUpwind);

    /**
     * Like {@link #getOrCreateBoatClass(String, boolean)}, only that a default for <code>typicallyStartsUpwind</code> based
     * on the boat class name is calculated.
     */
    BoatClass getOrCreateBoatClass(String name);

    /**
     * Gets the {@link CompetitorAndBoatStore} of this {@link SharedDomainFactory}.
     */
    CompetitorAndBoatStore getCompetitorAndBoatStore();
   
    /**
     * If a {@link CourseArea} with the given id already exists, it is returned. Otherwise a new {@link CourseArea} 
     * is created.
     */
    CourseArea getOrCreateCourseArea(UUID id, String name);
    
    /**
     * Gets the {@link CourseArea} with passed id; if there is no such {@link CourseArea} <code>null</code> will be returned.
     */
    CourseArea getExistingCourseAreaById(Serializable courseAreaId);
    
    /**
     * Hm.
     */
    DeviceConfigurationMatcher getOrCreateDeviceConfigurationMatcher(List<String> clientIdentifiers);
    
    Mark getExistingMarkByIdAsString(String toStringRepresentationOfID);
    
    Mark getExistingMarkById(Serializable id);
    
    ControlPointWithTwoMarks getOrCreateControlPointWithTwoMarks(Serializable id, String name, Mark left, Mark right);
    
    ControlPointWithTwoMarks getOrCreateControlPointWithTwoMarks(String id, String name, Mark left, Mark right);
    
    RaceLogResolver getRaceLogResolver();

    Mark getOrCreateMark(String name, MarkType markType);

    Mark getOrCreateMark(Serializable id, String name, MarkType markType);
}
