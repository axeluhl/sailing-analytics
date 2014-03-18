package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface HasTrackedRaceContext extends HasLeaderboardContext {

    public CourseArea getCourseArea();
    public Fleet getFleet();
    public TrackedRace getTrackedRace();
    public Integer getYear();

}