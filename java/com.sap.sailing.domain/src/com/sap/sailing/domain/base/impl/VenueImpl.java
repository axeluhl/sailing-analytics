package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class VenueImpl extends NamedImpl implements Venue {
    private static final long serialVersionUID = 6854152040737643290L;
    
    /**
     * The course areas are ordered because they typically follow an ordered naming pattern borrowed from the
     * "NATO alphabet" (Alpha, Bravo, Charlie, ...).
     */
    private final List<CourseArea> courseAreas;

    public VenueImpl(String name) {
        super(name);
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
    
}
