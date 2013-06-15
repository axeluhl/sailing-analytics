package com.sap.sailing.domain.coursedesign;

public interface CourseLayouts {
    public String getShortName();
    public Class<? extends CourseDesignFactory> getCourseDesignFactoryClass();
}
