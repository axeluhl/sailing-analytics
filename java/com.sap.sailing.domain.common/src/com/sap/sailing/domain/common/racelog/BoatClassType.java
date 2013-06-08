package com.sap.sailing.domain.common.racelog;

import java.util.EnumMap;
import java.util.Map;

public enum BoatClassType {
    @SuppressWarnings("serial")
    boatClass470er("470er", new EnumMap<CourseLayout, Integer>(CourseLayout.class) {
        {
            put(CourseLayout.innerLoopTrapezoid, 60);
            put(CourseLayout.outerLoopTrapezoid, 60);
            put(CourseLayout.windWardLeewardLeeward, 30);
            put(CourseLayout.windWardLeewardWindward, 30);
        }
    }),
    @SuppressWarnings("serial")
    boatClass49er("49er", new EnumMap<CourseLayout, Integer>(CourseLayout.class) {
        {
            put(CourseLayout.windWardLeewardLeeward, 30);
            put(CourseLayout.windWardLeewardWindward, 30);
        }
    });

    private String displayName;
    private Map<CourseLayout, Integer> possipleCourseLayoutsWithStandardTargetTime;

    private BoatClassType(String displayName, Map<CourseLayout, Integer> possipleCourseLayoutsWithStandardTargetTime) {
        this.displayName = displayName;
        this.possipleCourseLayoutsWithStandardTargetTime = possipleCourseLayoutsWithStandardTargetTime;
    }

    @Override
    public String toString() {
        return displayName;
    }
    
    public Map<CourseLayout, Integer> getPossibleCourseLayoutsWithTargetTime(){
        return possipleCourseLayoutsWithStandardTargetTime;
    }
}
