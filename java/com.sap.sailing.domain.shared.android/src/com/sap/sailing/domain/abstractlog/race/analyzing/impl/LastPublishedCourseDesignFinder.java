package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.CourseBase;

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
