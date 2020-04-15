package com.sap.sailing.racecommittee.app.domain.coursedesign;

public enum WindWardLeeWardCourseLayouts implements CourseLayouts {
    windWardLeewardWindward("Windward/Leeward with Windward finish", "W",
            true), windWardLeewardLeeward("Windward/Leeward with Leeward finish", "L", false);

    private String displayName;
    private String shortName;
    private boolean upWindFinish;

    @Override
    public String getShortName() {
        return shortName;
    }

    private WindWardLeeWardCourseLayouts(String displayName, String shortName, boolean upWindFinish) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.upWindFinish = upWindFinish;
    }

    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass() {
        return WindWardLeeWardCourseDesignFactoryImpl.class;
    }

    public boolean isUpWindFinish() {
        return upWindFinish;
    }
}
