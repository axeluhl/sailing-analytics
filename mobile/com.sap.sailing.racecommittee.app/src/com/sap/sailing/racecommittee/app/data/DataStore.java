package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface DataStore {
    
    public void reset();
    
    public SharedDomainFactory getDomainFactory();

    public Collection<EventBase> getEvents();
    public EventBase getEvent(Serializable id);
    public boolean hasEvent(Serializable id);
    public void addEvent(EventBase event);

    public Collection<CourseArea> getCourseAreas(EventBase event);
    public CourseArea getCourseArea(Serializable id);
    public boolean hasCourseArea(Serializable id);
    public void addCourseArea(EventBase event, CourseArea courseArea);

    public Collection<ManagedRace> getRaces();
    public void addRace(ManagedRace race);
    public ManagedRace getRace(String id);
    public ManagedRace getRace(SimpleRaceLogIdentifier id);
    public boolean hasRace(String id);
    public boolean hasRace(SimpleRaceLogIdentifier id);

    public Collection<Mark> getMarks();
    public Mark getMark(Serializable id);
    public boolean hasMark(Serializable id);
    public void addMark(Mark mark);
    
    public CourseBase getLastPublishedCourseDesign();
    public void setLastPublishedCourseDesign(CourseBase courseData);

    public Set<RaceGroup> getRaceGroups();
    public RaceGroup getRaceGroup(String name);
}
