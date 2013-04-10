package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.CollectionUtils;

public enum InMemoryDataStore implements DataStore {
	INSTANCE;
	
	private HashMap<Serializable, EventBase> eventsById;
	private HashMap<Serializable, ManagedRace> managedRaceById;
	
	private InMemoryDataStore() {
		this.eventsById = new HashMap<Serializable, EventBase>();
		this.managedRaceById = new HashMap<Serializable, ManagedRace>();
	}
	
	/*
	 * * * * * *
	 *  EVENTS *
	 * * * * * *
	 */

	public Collection<EventBase> getEvents() {
		return eventsById.values();
	}

	public EventBase getEvent(Serializable id) {
		return eventsById.get(id);
	}

	public boolean hasEvent(Serializable id) {
		return eventsById.containsKey(id);
	}

	public void addEvent(EventBase event) {
		eventsById.put(event.getId(), event);
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
				if (courseArea.getId().equals(id))
					return courseArea;
			}
		}
		return null;
	}

	public boolean hasCourseArea(Serializable id) {
		for (EventBase event : eventsById.values()) {
			for (CourseArea courseArea : getCourseAreas(event)) {
				if (courseArea.getId().equals(id))
					return true;
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
}
