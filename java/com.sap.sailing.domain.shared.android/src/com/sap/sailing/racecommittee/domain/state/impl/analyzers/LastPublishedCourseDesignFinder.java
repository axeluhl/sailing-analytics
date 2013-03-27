package com.sap.sailing.racecommittee.domain.state.impl.analyzers;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.domain.state.impl.analyzers.RaceLogAnalyzer;

public class LastPublishedCourseDesignFinder extends RaceLogAnalyzer {

    public LastPublishedCourseDesignFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public CourseBase getLastCourseDesign() {
        CourseBase lastCourseData = null;
        
        this.raceLog.lockForRead();
        try {
            lastCourseData = searchForLastCourseData();
        } finally {
            this.raceLog.unlockAfterRead();
        }
        
        return lastCourseData;
    }
    
    private CourseBase searchForLastCourseData() {
        CourseBase lastCourseData = null;
        
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogCourseDesignChangedEvent) {
                RaceLogCourseDesignChangedEvent courseDesignEvent = (RaceLogCourseDesignChangedEvent) event;
                lastCourseData = courseDesignEvent.getCourseDesign();
            }
        }
        
        return lastCourseData;
    }

}
