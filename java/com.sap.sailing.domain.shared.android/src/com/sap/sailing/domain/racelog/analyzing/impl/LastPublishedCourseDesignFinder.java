package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public class LastPublishedCourseDesignFinder extends RaceLogAnalyzer<CourseBase> {

    public LastPublishedCourseDesignFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected CourseBase performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogCourseDesignChangedEvent) {
                RaceLogCourseDesignChangedEvent courseDesignEvent = (RaceLogCourseDesignChangedEvent) event;
                return courseDesignEvent.getCourseDesign();
            }
        }
        
        return null;
    }

}
