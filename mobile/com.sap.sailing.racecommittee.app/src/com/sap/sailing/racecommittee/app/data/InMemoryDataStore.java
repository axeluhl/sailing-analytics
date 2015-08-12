package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.*;

import android.content.Context;
import android.util.Log;
import com.sap.sailing.android.shared.util.CollectionUtils;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.SharedDomainFactoryImpl;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sailing.racecommittee.app.utils.UrlHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

public enum InMemoryDataStore implements DataStore {
    INSTANCE;

    private static String TAG = InMemoryDataStore.class.getName();
    private HashMap<Serializable, EventBase> eventsById;
    private HashMap<SimpleRaceLogIdentifier, ManagedRace> managedRaceById;
    private HashMap<Serializable, Mark> marksById;
    private CourseBase courseData;
    private SharedDomainFactory domainFactory;

    private Serializable eventUUID;
    private UUID courseUUID;

    InMemoryDataStore() {
        reset();
    }

    @Override
    public void reset() {
        eventsById = new HashMap<>();
        managedRaceById = new HashMap<>();
        marksById = new HashMap<>();
        courseData = null;
        domainFactory = new SharedDomainFactoryImpl(new AndroidRaceLogResolver());

        eventUUID = null;
        courseUUID = null;
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
        managedRaceById.put(convertManagedRaceIdentiferToSimpleRaceLogIdentifier(race.getIdentifier()), race);
    }

    private SimpleRaceLogIdentifier convertManagedRaceIdentiferToSimpleRaceLogIdentifier(ManagedRaceIdentifier id) {
        return new SimpleRaceLogIdentifierImpl(id.getRaceGroup().getName(), id.getRaceName(), id.getFleet().getName());
    }

    @Override
    public ManagedRace getRace(String id) {
        return managedRaceById.get(parseManagedRaceLogIdentifier(id));
    }

    @Override
    public ManagedRace getRace(SimpleRaceLogIdentifier id) {
        return managedRaceById.get(id);
    }

    @Override
    public boolean hasRace(String id) {
        return managedRaceById.containsKey(parseManagedRaceLogIdentifier(id));
    }

    /**
     * Parses a serialized version of a ManagedRaceIdentifier and creates a SimpleRaceLogIdentifier
     * this is needed as the serialized version is passed around in the bundle context, but the 
     * InMemoryDataStore now has to use SimpleRaceLogIdentifier as key in the managedRaces HashMap in 
     * order to allow for retrieving a managed race with exclusively the information provided by a 
     * SimpleRaceLogIdentifier (which is less than the information provided by a ManagedRaceIdentifier)
     *  
     * @param escapedId
     *          serialized version of a ManagedRaceIdentifier 
     * @return
     *          corresponding SimpleRaceLogIdentifier
     */
    public SimpleRaceLogIdentifier parseManagedRaceLogIdentifier(final String escapedId) {
        //Undo escaping
        final Triple<String, String, String> id = FleetIdentifierImpl.unescape(escapedId);        
        return new SimpleRaceLogIdentifierImpl(id.getA(), id.getB(), id.getC());
    }

    @Override
    public boolean hasRace(SimpleRaceLogIdentifier id) {
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

    @Override
    public Serializable getEventUUID() {
        return eventUUID;
    }

    @Override
    public void setEventUUID(Serializable uuid) {
        eventUUID = uuid;
    }

    @Override
    public UUID getCourseUUID() {
        return courseUUID;
    }

    @Override
    public void setCourseUUID(UUID uuid) {
        courseUUID = uuid;
    }

}
