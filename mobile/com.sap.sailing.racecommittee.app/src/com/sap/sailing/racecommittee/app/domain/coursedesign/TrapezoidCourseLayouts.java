package com.sap.sailing.racecommittee.app.domain.coursedesign;

public enum TrapezoidCourseLayouts implements CourseLayouts {
    innerLoopTrapezoid60("Trapezoid 60° Inner Loop", "I", 60), outerLoopTrapezoid60("Trapezoid 60° Outer Loop", "O",
            60), innerLoopTrapezoid70("Trapezoid 70° Inner Loop", "I",
                    70), outerLoopTrapezoid70("Trapezoid 70° Outer Loop", "O", 70);

    private String displayName;
    private String shortName;
    private Integer reachAngle;

    public Integer getReachAngle() {
        return reachAngle;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    private TrapezoidCourseLayouts(String displayName, String shortName, Integer reachAngle) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.reachAngle = reachAngle;
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass() {
        return TrapezoidCourseDesignFactoryImpl.class;
    }

}
