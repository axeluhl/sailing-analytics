package com.sap.sailing.racecommittee.app.data;

import java.io.Serializable;
import java.util.Collection;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.base.EventData;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public interface DataStore {

    public Collection<EventData> getEvents();
    public EventData getEvent(Serializable id);
    public boolean hasEvent(Serializable id);
    public void addEvent(EventData event);

    public Collection<CourseArea> getCourseAreas(EventData event);
    public CourseArea getCourseArea(Serializable id);
    public boolean hasCourseArea(Serializable id);
    public void addCourseArea(EventData event, CourseArea courseArea);

    public Collection<ManagedRace> getRaces();
    public void addRace(ManagedRace race);
    public ManagedRace getRace(Serializable id);
    public boolean hasRace(Serializable id);

    public Collection<Mark> getMarks();
    public Mark getMark(Serializable id);
    public boolean hasMark(Serializable id);
    public void addMark(Mark mark);
    
    public CourseData getLastPublishedCourseDesign();
    public void setLastPublishedCourseDesign(CourseData courseData);
}
