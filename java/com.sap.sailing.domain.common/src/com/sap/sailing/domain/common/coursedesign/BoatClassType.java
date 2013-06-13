package com.sap.sailing.domain.common.coursedesign;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

public enum BoatClassType {
    @SuppressWarnings("serial")
    boatClass470er("470er", new EnumMap<CourseLayout, Integer>(CourseLayout.class) {
        {
            put(CourseLayout.innerLoopTrapezoid, 60);
            put(CourseLayout.outerLoopTrapezoid, 60);
            put(CourseLayout.windWardLeewardLeeward, 30);
            put(CourseLayout.windWardLeewardWindward, 30);
        }
    }, new TreeMap<WindRange, Map<PointOfSail, Float>>() {
        {
            put(new WindRange(5, 8), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 15f);
                    put(PointOfSail.Downwind, 11f);
                    put(PointOfSail.Reach, 8f);
                }
            });
            put(new WindRange(8, 12), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 13f);
                    put(PointOfSail.Downwind, 10f);
                    put(PointOfSail.Reach, 7f);
                }
            });
            put(new WindRange(12, 15), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 11f);
                    put(PointOfSail.Downwind, 7.5f);
                    put(PointOfSail.Reach, 5.5f);
                }
            });
            put(new WindRange(15, 99), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 10f);
                    put(PointOfSail.Downwind, 6.5f);
                    put(PointOfSail.Reach, 5.5f);
                }
            });
        }
    }), @SuppressWarnings("serial")
    boatClass49er("49er", new EnumMap<CourseLayout, Integer>(CourseLayout.class) {
        {
            put(CourseLayout.windWardLeewardLeeward, 30);
            put(CourseLayout.windWardLeewardWindward, 30);
        }
    }, new TreeMap<WindRange, Map<PointOfSail, Float>>() {
        {
            put(new WindRange(5, 8), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 15f);
                    put(PointOfSail.Downwind, 11f);
                }
            });
            put(new WindRange(8, 12), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 11f);
                    put(PointOfSail.Downwind, 5.5f);
                }
            });
            put(new WindRange(12, 15), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 9f);
                    put(PointOfSail.Downwind, 4.5f);
                }
            });
            put(new WindRange(15, 99), new EnumMap<PointOfSail, Float>(PointOfSail.class) {
                {
                    put(PointOfSail.Upwind, 7.5f);
                    put(PointOfSail.Downwind, 4f);
                }
            });
        }
    });

    private String displayName;
    private Map<CourseLayout, Integer> possipleCourseLayoutsWithStandardTargetTime;

    private BoatClassType(String displayName, Map<CourseLayout, Integer> possipleCourseLayoutsWithStandardTargetTime,
            Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable) {
        this.displayName = displayName;
        this.possipleCourseLayoutsWithStandardTargetTime = possipleCourseLayoutsWithStandardTargetTime;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Map<CourseLayout, Integer> getPossibleCourseLayoutsWithTargetTime() {
        return possipleCourseLayoutsWithStandardTargetTime;
    }
}
