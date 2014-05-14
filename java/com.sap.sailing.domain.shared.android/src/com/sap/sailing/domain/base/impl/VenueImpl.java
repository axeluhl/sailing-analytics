package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

public class VenueImpl implements Venue {
    private static final long serialVersionUID = 6854152040737643290L;
    private String name;
    
    /**
     * The course areas are ordered because they typically follow an ordered naming pattern borrowed from the
     * "NATO alphabet" (Alpha, Bravo, Charlie, ...).
     */
    private final List<CourseArea> courseAreas;
    
    private final NamedReentrantReadWriteLock courseAreasLock;

    public VenueImpl(String name) {
        this.name = name;
        courseAreas = new ArrayList<CourseArea>();
        courseAreasLock = new NamedReentrantReadWriteLock("Course Areas for venue "+name, /* fair */ false);
    }

    @Override
    public Iterable<CourseArea> getCourseAreas() {
        LockUtil.lockForRead(courseAreasLock);
        try {
            return Collections.unmodifiableList(courseAreas);
        } finally {
            LockUtil.unlockAfterRead(courseAreasLock);
        }
    }

    @Override
    public void addCourseArea(CourseArea courseArea) {
        LockUtil.lockForWrite(courseAreasLock);
        try {
            courseAreas.add(courseArea);
        } finally {
            LockUtil.unlockAfterWrite(courseAreasLock);
        }
    }

    @Override
    public void removeCourseArea(CourseArea courseArea) {
        LockUtil.lockForWrite(courseAreasLock);
        try {
            courseAreas.remove(courseArea);
        } finally {
            LockUtil.unlockAfterWrite(courseAreasLock);
        }
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
