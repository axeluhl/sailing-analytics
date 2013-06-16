package com.sap.sailing.racecommittee.app.domain.coursedesign;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
@SuppressWarnings("serial")
public enum BoatClassType {
    boatClass470erMen("470er Men", new HashMap<CourseLayouts, TargetTime>() {
        {
            put(TrapezoidCourseLayouts.innerLoopTrapezoid60, TargetTime.sixty);
            put(TrapezoidCourseLayouts.outerLoopTrapezoid60, TargetTime.sixty);
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, TargetTime.thirty);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, TargetTime.thirty);
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
    }, new MeterDistance(4.7), new MeterDistance(290)),
    boatClass470eromen("470er Women", new HashMap<CourseLayouts, TargetTime>() {
        {
            put(TrapezoidCourseLayouts.innerLoopTrapezoid60, TargetTime.sixty);
            put(TrapezoidCourseLayouts.outerLoopTrapezoid60, TargetTime.sixty);
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, TargetTime.thirty);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, TargetTime.thirty);
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
    }, new MeterDistance(4.7), new MeterDistance(110)),
    boatClass49er("49er", new HashMap<CourseLayouts, TargetTime>() {
        {
            put(WindWardLeeWardCourseLayouts.windWardLeewardLeeward, TargetTime.thirty);
            put(WindWardLeeWardCourseLayouts.windWardLeewardWindward, TargetTime.thirty);
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
    }, new MeterDistance(4.9), new MeterDistance(255));

    private String displayName;
    private Map<CourseLayouts, TargetTime> possipleCourseLayoutsWithStandardTargetTime;
    private Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable;
    private Distance hullLength;
    private Distance startLineLength;

    private BoatClassType(String displayName, Map<CourseLayouts, TargetTime> possipleCourseLayoutsWithStandardTargetTime,
            Map<WindRange, Map<PointOfSail, Float>> boatSpeedTable, Distance hullLength, Distance startLineLengthInMeters) {
        this.displayName = displayName;
        this.possipleCourseLayoutsWithStandardTargetTime = possipleCourseLayoutsWithStandardTargetTime;
        this.boatSpeedTable = boatSpeedTable;
        this.hullLength = hullLength;
        this.startLineLength = startLineLengthInMeters;
    }

    public Map<WindRange, Map<PointOfSail, Float>> getBoatSpeedTable() {
        return boatSpeedTable;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Map<CourseLayouts, TargetTime> getPossibleCourseLayoutsWithTargetTime() {
        return possipleCourseLayoutsWithStandardTargetTime;
    }
    
    public Distance getHullLength() {
        return hullLength;
    }

    public Distance getStartLineLength() {
        return startLineLength;
    }
}
