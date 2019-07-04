package com.sap.sailing.domain.coursetemplate.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedWithIDImpl;

public class CourseTemplateImpl extends NamedWithIDImpl implements CourseTemplate {
    private static final long serialVersionUID = -183875832585632806L;

    private final Iterable<MarkTemplate> marks;
    
    private final ArrayList<WaypointTemplate> waypoints;
    
    /**
     * The index into {@link #waypoints} of the first waypoint that is to be cloned for repetitive laps.
     * -1 means no repeatable part.
     */
    private final int zeroBasedIndexOfRepeatablePartStart;

    /**
     * The index into {@link #waypoints} of the first waypoint that comes after the sub-sequence to be cloned for
     * repetitive laps. -1 means no repeatable part.
     */
    private final int zeroBasedIndexOfRepeatablePartEnd;
    
    public CourseTemplateImpl(String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints) {
        this(name, marks, waypoints, -1, -1); // no repeatable part
    }
    
    public CourseTemplateImpl(String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            int zeroBasedIndexOfRepeatablePartStart, int zeroBasedIndexOfRepeatablePartEnd) {
        super(name);
        if ((zeroBasedIndexOfRepeatablePartEnd == -1) != (zeroBasedIndexOfRepeatablePartStart == -1)) {
            throw new IllegalArgumentException("Either both, start and end of repeatable sub-sequence indices must be -1 or none: "+
                    zeroBasedIndexOfRepeatablePartStart+".."+zeroBasedIndexOfRepeatablePartEnd);
        }
        if (zeroBasedIndexOfRepeatablePartStart > zeroBasedIndexOfRepeatablePartEnd) {
            throw new IllegalArgumentException("A repeatable part must span a positive number of waypoints. It doesn't: "+
                    zeroBasedIndexOfRepeatablePartStart+".."+zeroBasedIndexOfRepeatablePartEnd);
        }
        final Set<MarkTemplate> theMarks = new HashSet<>();
        Util.addAll(marks, theMarks);
        this.waypoints = new ArrayList<>();
        Util.addAll(waypoints, this.waypoints);
        this.marks = theMarks;
        this.zeroBasedIndexOfRepeatablePartStart = zeroBasedIndexOfRepeatablePartStart;
        this.zeroBasedIndexOfRepeatablePartEnd = zeroBasedIndexOfRepeatablePartEnd;
        validateWaypointsAgainstMarks();
    }

    /**
     * Throws an {@link IllegalArgumentException} in case a waypoint from {@link #waypoints} uses a mark template that
     * is not in {@link #marks}.
     */
    private void validateWaypointsAgainstMarks() {
        for (final WaypointTemplate waypoint : waypoints) {
            for (final MarkTemplate mark : waypoint.getControlPoint().getMarks()) {
                if (!Util.contains(marks, mark)) {
                    throw new IllegalArgumentException("Mark "+mark+" used by waypoint template "+
                            waypoint+" in course template "+this+" is not provided in the collection of marks");
                }
            }
        }
    }

    @Override
    public Iterable<MarkTemplate> getMarks() {
        return marks;
    }

    @Override
    public Iterable<WaypointTemplate> getWaypoints(int numberOfLaps) {
        final Iterable<WaypointTemplate> result;
        if (hasRepeatablePart()) {
            final List<WaypointTemplate> resultList = new LinkedList<>();
            for (int i=0; i<waypoints.size(); i++) {
                if (i == zeroBasedIndexOfRepeatablePartStart) {
                    for (int lap=1; lap<numberOfLaps; lap++) {
                        for (i=zeroBasedIndexOfRepeatablePartStart; i<zeroBasedIndexOfRepeatablePartEnd; i++) {
                            resultList.add(waypoints.get(i));
                        }
                    }
                }
                // special case: repeatable part ends at end of waypoint list:
                if (i<waypoints.size()) {
                    resultList.add(waypoints.get(i));
                }
            }
            result = resultList;
        } else {
            result = waypoints;
        }
        return result;
    }

    private boolean hasRepeatablePart() {
        return zeroBasedIndexOfRepeatablePartEnd == -1;
    }
}
