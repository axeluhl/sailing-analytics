package com.sap.sailing.racecommittee.app.domain.coursedesign;

public interface CourseLayouts {
    public String getShortName();

    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass();

    public String name();
}
