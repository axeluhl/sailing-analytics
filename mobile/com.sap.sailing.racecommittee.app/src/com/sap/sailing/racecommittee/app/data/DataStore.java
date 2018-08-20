package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import android.content.Context;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface DataStore {

    void setContext(Context context);
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
    void addRace(int index, ManagedRace race);
    void removeRace(ManagedRace race);
    ManagedRace getRace(String id);
    ManagedRace getRace(SimpleRaceLogIdentifier id);
    boolean hasRace(String id);
    boolean hasRace(SimpleRaceLogIdentifier id);
    void registerRaces(Collection<ManagedRace> races);

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
