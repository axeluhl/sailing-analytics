package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;

public interface DataStore {

	public Collection<Event> getEvents();
	public Event getEvent(Serializable id);
	public boolean hasEvent(Serializable id);
	public void addEvent(Event event);
	
	public Collection<CourseArea> getCourseAreas(Event event);
	public CourseArea getCourseArea(Event event, String name);
	public boolean hasCourseArea(Event event, String name);
	public void addCourseArea(Event event, CourseArea courseArea);
	
}
