package com.sap.sailing.racecommittee.app.data;

import android.content.Context;
import android.content.Intent;

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
import com.sap.sailing.racecommittee.app.services.RaceStateService;
import com.sap.sse.common.Util.Triple;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public enum InMemoryDataStore implements DataStore {
    INSTANCE;

    private LinkedHashMap<Serializable, EventBase> eventsById;
    private LinkedHashMap<SimpleRaceLogIdentifier, ManagedRace> managedRaceById;
    private LinkedHashMap<RaceGroup, LinkedHashMap<Serializable, Mark>> marksData;
    private LinkedHashMap<RaceGroup, CourseBase> courseData;
    private SharedDomainFactory domainFactory;

    private Serializable eventUUID;
    private UUID courseAreaId;

    InMemoryDataStore() {
        reset();
    }

    @Override
    public void reset() {
        eventsById = new LinkedHashMap<>();
        managedRaceById = new LinkedHashMap<>();
        marksData = new LinkedHashMap<>();
        courseData = new LinkedHashMap<>();
        domainFactory = new SharedDomainFactoryImpl(new AndroidRaceLogResolver());

        eventUUID = null;
        courseAreaId = null;
    }

    @Override
    public SharedDomainFactory getDomainFactory() {
        return domainFactory;
    }

    /*
     * * * * * * EVENTS * * * * * *
     */

    @Override
    public Collection<EventBase> getEvents() {
        return eventsById.values();
    }

    @Override
    public void addEvent(EventBase event) {
        eventsById.put(event.getId(), event);
    }

    @Override
    public EventBase getEvent(Serializable id) {
        return eventsById.get(id);
    }

    @Override
    public boolean hasEvent(Serializable id) {
        return eventsById.containsKey(id);
    }

    /*
     * * * * * * * * COURSE AREA * * * * * * * *
     */

    @Override
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

    @Override
    public CourseArea getCourseArea(Serializable id) {
        for (EventBase event : eventsById.values()) {
            final Collection<CourseArea> courseAreas = getCourseAreas(event);
            if (courseAreas != null) {
                for (CourseArea courseArea : courseAreas) {
                    if (courseArea.getId().equals(id)) {
                        return courseArea;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasCourseArea(Serializable id) {
        for (EventBase event : eventsById.values()) {
            final Collection<CourseArea> courseAreas = getCourseAreas(event);
            if (courseAreas != null) {
                for (CourseArea courseArea : courseAreas) {
                    if (courseArea.getId().equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addCourseArea(EventBase event, CourseArea courseArea) {
        if (event.getVenue() != null) {
            event.getVenue().addCourseArea(courseArea);
        }
    }

    /*
     * * * * * * * * MANAGED RACE * * * * * * * *
     */

    @Override
    public Collection<ManagedRace> getRaces() {
        return managedRaceById.values();
    }

    @Override
    public void addRace(ManagedRace race) {
        managedRaceById.put(convertManagedRaceIdentifierToSimpleRaceLogIdentifier(race.getIdentifier()), race);
    }

    @Override
    public void addRace(int index, ManagedRace race) {
        if (index >= 0 && index <= managedRaceById.size()) {
            LinkedHashMap<SimpleRaceLogIdentifier, ManagedRace> output = new LinkedHashMap<>();
            int i = 0;
            if (index == 0) {
                output.put(convertManagedRaceIdentifierToSimpleRaceLogIdentifier(race.getIdentifier()), race);
                output.putAll(managedRaceById);
            } else {
                for (Map.Entry<SimpleRaceLogIdentifier, ManagedRace> entry : managedRaceById.entrySet()) {
                    if (i == index) {
                        output.put(convertManagedRaceIdentifierToSimpleRaceLogIdentifier(race.getIdentifier()), race);
                    }
                    output.put(entry.getKey(), entry.getValue());
                    i++;
                }
            }
            if (index == managedRaceById.size()) {
                output.put(convertManagedRaceIdentifierToSimpleRaceLogIdentifier(race.getIdentifier()), race);
            }
            managedRaceById.clear();
            managedRaceById.putAll(output);
            output.clear();
        } else {
            throw new IndexOutOfBoundsException(
                    "index " + index + " must be greater than zero and less than size of the map");
        }
    }

    @Override
    public void removeRace(Context context, ManagedRace race) {
        unregisterRace(context, race);
        managedRaceById.remove(convertManagedRaceIdentifierToSimpleRaceLogIdentifier(race.getIdentifier()));
    }

    private SimpleRaceLogIdentifier convertManagedRaceIdentifierToSimpleRaceLogIdentifier(ManagedRaceIdentifier id) {
        return new SimpleRaceLogIdentifierImpl(id.getRaceGroup().getName(), id.getRaceColumnName(),
                id.getFleet().getName());
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
     * Parses a serialized version of a ManagedRaceIdentifier and creates a SimpleRaceLogIdentifier this is needed as
     * the serialized version is passed around in the bundle context, but the InMemoryDataStore now has to use
     * SimpleRaceLogIdentifier as key in the managedRaces HashMap in order to allow for retrieving a managed race with
     * exclusively the information provided by a SimpleRaceLogIdentifier (which is less than the information provided by
     * a ManagedRaceIdentifier)
     *
     * @param escapedId
     *            serialized version of a ManagedRaceIdentifier
     * @return corresponding SimpleRaceLogIdentifier
     */
    public SimpleRaceLogIdentifier parseManagedRaceLogIdentifier(final String escapedId) {
        // Undo escaping
        final Triple<String, String, String> id = FleetIdentifierImpl.unescape(escapedId);
        return new SimpleRaceLogIdentifierImpl(id.getA(), id.getB(), id.getC());
    }

    @Override
    public boolean hasRace(SimpleRaceLogIdentifier id) {
        return managedRaceById.containsKey(id);
    }

    /*
     * * * * * * MARKS * * * * * *
     */

    private LinkedHashMap<Serializable, Mark> getMarksByRaceGroup(RaceGroup raceGroup) {
        LinkedHashMap<Serializable, Mark> marks = marksData.get(raceGroup);
        if (marks == null) {
            marks = new LinkedHashMap<>();
            marksData.put(raceGroup, marks);
        }
        return marks;
    }

    private void setMarksByRaceGroup(RaceGroup raceGroup, LinkedHashMap<Serializable, Mark> marks) {
        marksData.put(raceGroup, marks);
    }

    @Override
    public Collection<Mark> getMarks(RaceGroup raceGroup) {
        return getMarksByRaceGroup(raceGroup).values();
    }

    @Override
    public Mark getMark(RaceGroup raceGroup, Serializable id) {
        return getMarksByRaceGroup(raceGroup).get(id);
    }

    @Override
    public boolean hasMark(RaceGroup raceGroup, Serializable id) {
        return getMarksByRaceGroup(raceGroup).containsKey(id);
    }

    @Override
    public void addMark(RaceGroup raceGroup, Mark mark) {
        getMarksByRaceGroup(raceGroup).put(mark.getId(), mark);
    }

    @Override
    public CourseBase getLastPublishedCourseDesign(RaceGroup raceGroup) {
        return courseData.get(raceGroup);
    }

    @Override
    public void setLastPublishedCourseDesign(RaceGroup raceGroup, CourseBase courseData) {
        this.courseData.put(raceGroup, courseData);
    }

    @Override
    public Set<RaceGroup> getRaceGroups() {
        Set<RaceGroup> raceGroups = new HashSet<>();
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
    public UUID getCourseAreaId() {
        return courseAreaId;
    }

    @Override
    public void setCourseAreaId(UUID uuid) {
        courseAreaId = uuid;
    }

    @Override
    public void registerRaces(Context context, Collection<ManagedRace> races) {
        for (ManagedRace race : races) {
            final Intent registerIntent = new Intent(context, RaceStateService.class);
            registerIntent.setAction(AppConstants.INTENT_ACTION_REGISTER_RACE);
            registerIntent.putExtra(AppConstants.INTENT_EXTRA_RACE_ID, race.getId());
            context.startService(registerIntent);
        }
    }

    @Override
    public void clearRaces(Context context) {
        managedRaceById.clear();
        final Intent intent = new Intent(context, RaceStateService.class);
        intent.setAction(AppConstants.INTENT_ACTION_CLEAR_RACES);
        context.startService(intent);
    }

    private void unregisterRace(Context context, ManagedRace race) {
        final Intent intent = new Intent(context, RaceStateService.class);
        intent.setAction(AppConstants.INTENT_ACTION_UNREGISTER_RACE);
        intent.putExtra(AppConstants.INTENT_EXTRA_RACE_ID, race.getId());
        context.startService(intent);
    }
}
