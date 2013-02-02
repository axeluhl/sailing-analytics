package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.CollectionUtils;

public enum InMemoryDataStore implements DataStore {
	INSTANCE;
	
	private HashMap<Serializable, Event> eventsById;
	private HashMap<Serializable, ManagedRace> managedRaceById;
	
	private InMemoryDataStore() {
		this.eventsById = new HashMap<Serializable, Event>();
		this.managedRaceById = new HashMap<Serializable, ManagedRace>();
	}
	
	/*
	 * * * * * *
	 *  EVENTS *
	 * * * * * *
	 */

	public Collection<Event> getEvents() {
		return eventsById.values();
	}

	public Event getEvent(Serializable id) {
		return eventsById.get(id);
	}

	public boolean hasEvent(Serializable id) {
		return eventsById.containsKey(id);
	}

	public void addEvent(Event event) {
		eventsById.put(event.getId(), event);
	}
	
	/*
	 * * * * * * * *
	 * COURSE AREA *
	 * * * * * * * *
	 */

	public Collection<CourseArea> getCourseAreas(Event event) {
		if (event.getVenue() != null) {
			return CollectionUtils.newArrayList(event.getVenue().getCourseAreas());
		}
		return null;
	}

	public CourseArea getCourseArea(Event event, String name) {
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
		for (Event event : eventsById.values()) {
			for (CourseArea courseArea : getCourseAreas(event)) {
				if (courseArea.getId().equals(id))
					return courseArea;
			}
		}
		return null;
	}

	public boolean hasCourseArea(Serializable id) {
		for (Event event : eventsById.values()) {
			for (CourseArea courseArea : getCourseAreas(event)) {
				if (courseArea.getId().equals(id))
					return true;
			}
		}
		return false;
	}

	public void addCourseArea(Event event, CourseArea courseArea) {
		if (event.getVenue() != null) {
			event.getVenue().addCourseArea(courseArea);
		}
	}
	
	/*
	 * * * * * * *  *
	 * MANAGED RACE *
	 * * * * * * *  *
	 */

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
