package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface DataStore {

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
	public ManagedRace getRace(Serializable id);
	public boolean hasRace(Serializable id);
	
}
