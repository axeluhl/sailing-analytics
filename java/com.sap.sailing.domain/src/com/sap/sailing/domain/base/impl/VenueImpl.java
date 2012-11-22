package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;

public class VenueImpl implements Venue {
    private static final long serialVersionUID = 6854152040737643290L;
    private String name;
    
    /**
     * The course areas are ordered because they typically follow an ordered naming pattern borrowed from the
     * "NATO alphabet" (Alpha, Bravo, Charlie, ...).
     */
    private final List<CourseArea> courseAreas;

    public VenueImpl(String name) {
        this.name = name;
        courseAreas = new ArrayList<CourseArea>();
    }

    @Override
    public Iterable<CourseArea> getCourseAreas() {
        return Collections.unmodifiableList(courseAreas);
    }

    @Override
    public void addCourseArea(CourseArea courseArea) {
        courseAreas.add(courseArea);
    }

    @Override
    public void removeCourseArea(CourseArea courseArea) {
        courseAreas.remove(courseArea);
    }
    
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param newName must not be <code>null</code>
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("An venue name must not be null");
        }
        this.name = newName;
    }
}
