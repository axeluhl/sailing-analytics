package com.sap.sailing.domain.coursedesign;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
@SuppressWarnings("serial")
public enum BoatClassType {
    boatClass470erMen("470er Men", new HashMap<CourseLayouts, Integer>() {
        {
            put(TrapezoidCourseLayouts.innerLoopTrapezoid60, 60);
            put(TrapezoidCourseLayouts.outerLoopTrapezoid60, 60);
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, 30);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, 30);
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
    }, 290),
    boatClass470eromen("470er Women", new HashMap<CourseLayouts, Integer>() {
        {
            put(TrapezoidCourseLayouts.innerLoopTrapezoid60, 60);
            put(TrapezoidCourseLayouts.outerLoopTrapezoid60, 60);
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, 30);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, 30);
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
    }, 110),
    boatClass49er("49er", new HashMap<CourseLayouts, Integer>() {
        {
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, 30);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, 30);
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
    }, 255);

    private String displayName;
    private Map<CourseLayouts, Integer> possipleCourseLayoutsWithStandardTargetTime;
    private Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable;
    private Integer startLineLengthInMeters;

    private BoatClassType(String displayName, Map<CourseLayouts, Integer> possipleCourseLayoutsWithStandardTargetTime,
            Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable, Integer startLineLengthInMeters) {
        this.displayName = displayName;
        this.possipleCourseLayoutsWithStandardTargetTime = possipleCourseLayoutsWithStandardTargetTime;
        this.boatSpeedTable = boatSpeedTable;
        this.startLineLengthInMeters = startLineLengthInMeters;
    }

    public Map<WindRange, Map<PointOfSail, Float>> getBoatSpeedTable() {
        return boatSpeedTable;
    }

    public void setBoatSpeedTable(Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable) {
        this.boatSpeedTable = boatSpeedTable;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Map<CourseLayouts, Integer> getPossibleCourseLayoutsWithTargetTime() {
        return possipleCourseLayoutsWithStandardTargetTime;
    }

    public Integer getStartLineLengthInMeters() {
        return startLineLengthInMeters;
    }
}
