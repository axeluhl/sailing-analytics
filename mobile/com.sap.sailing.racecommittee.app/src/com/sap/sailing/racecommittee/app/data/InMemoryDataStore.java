package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.CollectionUtils;

public enum InMemoryDataStore implements DataStore {
    INSTANCE;

	public Collection<EventBase> getEvents() {
		return eventsById.values();
	}
    private HashMap<Serializable, EventBase> eventsById;
    private HashMap<Serializable, ManagedRace> managedRaceById;
    private HashMap<Serializable, Mark> marksById;
    private CourseBase courseData;

	public EventBase getEvent(Serializable id) {
		return eventsById.get(id);
	}
    private InMemoryDataStore() {
        this.eventsById = new HashMap<Serializable, EventBase>();
        this.managedRaceById = new HashMap<Serializable, ManagedRace>();
        this.marksById = new HashMap<Serializable, Mark>();
        this.courseData = null;
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

    public boolean hasEvent(Serializable id) {
        return eventsById.containsKey(id);
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

    public CourseArea getCourseArea(EventData event, String name) {
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
        for (EventData event : eventsById.values()) {
            for (CourseArea courseArea : getCourseAreas(event)) {
                if (courseArea.getId().equals(id))
                    return courseArea;
            }
        }
        return null;
    }

    public boolean hasCourseArea(Serializable id) {
        for (EventData event : eventsById.values()) {
            for (CourseArea courseArea : getCourseAreas(event)) {
                if (courseArea.getId().equals(id))
                    return true;
            }
        }
        return false;
    }

    public void addCourseArea(EventData event, CourseArea courseArea) {
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
    public CourseData getLastPublishedCourseDesign() {
        return courseData;
    }
    
    @Override
    public void setLastPublishedCourseDesign(CourseData courseData) {
        this.courseData = courseData;
    }
}
