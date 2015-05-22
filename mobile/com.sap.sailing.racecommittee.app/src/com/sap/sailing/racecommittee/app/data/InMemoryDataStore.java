package com.sap.sailing.racecommittee.app.data;

import com.sap.sailing.android.shared.util.CollectionUtils;
import com.sap.sailing.domain.base.*;
import com.sap.sailing.domain.base.impl.SharedDomainFactoryImpl;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum InMemoryDataStore implements DataStore {
    INSTANCE;

    private HashMap<Serializable, EventBase> eventsById;
    private HashMap<Serializable, ManagedRace> managedRaceById;
    private HashMap<Serializable, Mark> marksById;
    private CourseBase courseData;
    private SharedDomainFactory domainFactory;

    private InMemoryDataStore() {
        reset();
    }

    @Override
    public void reset() {
        this.eventsById = new HashMap<>();
        this.managedRaceById = new HashMap<>();
        this.marksById = new HashMap<>();
        this.courseData = null;
        this.domainFactory = new SharedDomainFactoryImpl();
    }

    @Override
    public SharedDomainFactory getDomainFactory() {
        return domainFactory;
    }

    /*
     * * * * * *
     *  EVENTS *
     * * * * * *
     */

    public Collection<EventBase> getEvents() {
        return eventsById.values();
    }
    public void addEvent(EventBase event) {
        eventsById.put(event.getId(), event);
    }


    public EventBase getEvent(Serializable id) {
        return eventsById.get(id);
    }

    public boolean hasEvent(Serializable id) {
        return eventsById.containsKey(id);
    }

    /*
     * * * * * * * *
     * COURSE AREA *
     * * * * * * * *
     */

    public Collection<CourseArea> getCourseAreas(EventBase event) {
        if (event.getVenue() != null) {
            return CollectionUtils.newArrayList(event.getVenue().getCourseAreas());
        }
        return null;
    }


    public CourseArea getCourseArea(EventBase event, String name) {
        Collection<CourseArea> courseAreas = getCourseAreas(event);
        if (courseAreas != null) {
            for (CourseArea courseArea : courseAreas) {
                if (courseArea.getName().equals(name)) {
                    return courseArea;
                }
            }
        }
        return null;
    }

    public CourseArea getCourseArea(Serializable id) {
        for (EventBase event : eventsById.values()) {
            for (CourseArea courseArea : getCourseAreas(event)) {
                if (courseArea.getId().equals(id)) {
                    return courseArea;
                }
            }
        }
        return null;
    }

    public boolean hasCourseArea(Serializable id) {
        for (EventBase event : eventsById.values()) {
            for (CourseArea courseArea : getCourseAreas(event)) {
                if (courseArea.getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addCourseArea(EventBase event, CourseArea courseArea) {
        if (event.getVenue() != null) {
            event.getVenue().addCourseArea(courseArea);
        }
    }

    /*
     * * * * * * *  *
     * MANAGED RACE *
     * * * * * * *  *
     */

    public Collection<ManagedRace> getRaces() {
        return managedRaceById.values();
    }

    public void addRace(ManagedRace race) {
        managedRaceById.put(race.getId(), race);
    }

    public ManagedRace getRace(Serializable id) {
        return managedRaceById.get(id);
    }

    public boolean hasRace(Serializable id) {
        return managedRaceById.containsKey(id);
    }

    /*
     * * * * * *
     *  MARKS  *
     * * * * * *
     */

    @Override
    public Collection<Mark> getMarks() {
        return marksById.values();
    }

    @Override
    public Mark getMark(Serializable id) {
        return marksById.get(id);
    }

    @Override
    public boolean hasMark(Serializable id) {
        return marksById.containsKey(id);
    }

    @Override
    public void addMark(Mark mark) {
        marksById.put(mark.getId(), mark);
    }

    @Override
    public CourseBase getLastPublishedCourseDesign() {
        return courseData;
    }

    @Override
    public void setLastPublishedCourseDesign(CourseBase courseData) {
        this.courseData = courseData;
    }

    @Override
    public Set<RaceGroup> getRaceGroups() {
        Set<RaceGroup> raceGroups = new HashSet<RaceGroup>();
        for (ManagedRace race : getRaces()) {
            raceGroups.add(race.getRaceGroup());
        }
        return raceGroups;
    }
    
    @Override
    public RaceGroup getRaceGroup(String name) {
        for (RaceGroup group : getRaceGroups()) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
}
