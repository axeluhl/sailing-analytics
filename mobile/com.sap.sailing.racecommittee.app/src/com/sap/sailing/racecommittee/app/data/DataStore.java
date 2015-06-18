package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.domain.base.*;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface DataStore {
    
    void reset();
    
    SharedDomainFactory getDomainFactory();

    Collection<EventBase> getEvents();
    EventBase getEvent(Serializable id);
    boolean hasEvent(Serializable id);
    void addEvent(EventBase event);

    Collection<CourseArea> getCourseAreas(EventBase event);
    CourseArea getCourseArea(Serializable id);
    boolean hasCourseArea(Serializable id);
    void addCourseArea(EventBase event, CourseArea courseArea);

    Collection<ManagedRace> getRaces();
    void addRace(ManagedRace race);
    ManagedRace getRace(Serializable id);
    boolean hasRace(Serializable id);

    Collection<Mark> getMarks();
    Mark getMark(Serializable id);
    boolean hasMark(Serializable id);
    void addMark(Mark mark);
    
    CourseBase getLastPublishedCourseDesign();
    void setLastPublishedCourseDesign(CourseBase courseData);

    Set<RaceGroup> getRaceGroups();
    RaceGroup getRaceGroup(String name);

    Serializable getEventUUID();
    void setEventUUID(Serializable uuid);

    UUID getCourseUUID();
    void setCourseUUID(UUID uuid);
}
