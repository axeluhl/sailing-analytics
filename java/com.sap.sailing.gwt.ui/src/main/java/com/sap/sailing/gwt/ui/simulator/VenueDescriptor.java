package com.sap.sailing.gwt.ui.simulator;

import java.util.List;

import com.google.gwt.maps.client.base.LatLng;

public class VenueDescriptor {
    private String name;
    private LatLng centerPos;
    private List<CourseAreaDescriptor> courseAreas;
    private CourseAreaDescriptor defaultCourseArea;

    public VenueDescriptor(String name, List<CourseAreaDescriptor> courseAreas) {
        this.name = name;
        this.courseAreas = courseAreas;
    }

    public String getName() {
        return name;
    }

    public LatLng getCenterPos() {
        return centerPos;
    }

    public List<CourseAreaDescriptor> getCourseAreas() {
        return courseAreas;
    }

    public CourseAreaDescriptor getDefaultCourseArea() {
        return defaultCourseArea;
    }

    public void setDefaultCourseArea(CourseAreaDescriptor defaultCourseArea) {
        this.defaultCourseArea = defaultCourseArea;
    }

    public void setCenterPos(LatLng centerPos) {
        this.centerPos = centerPos;
    }
    
}
