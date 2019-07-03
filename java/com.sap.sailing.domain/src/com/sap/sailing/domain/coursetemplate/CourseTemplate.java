package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.Course;
import com.sap.sse.common.NamedWithID;

/**
 * A {@link Course} can be created from this template. The template defines {@link MarkTemplate}s,
 * {@link ControlPointTemplate}s, and {@link WaypointTemplate}s and assembles the latter into a sequence. The sequence
 * of waypoint templates can optionally have a repeatable part. When increasing or reducing the number of laps, more or,
 * respectively, fewer occurrences of this repeatable part will be inserted into the {@link Course}.
 * <p>
 * 
 * The course template can define additional mark and control point templates that are not part of the waypoint
 * sequence. This can be used, e.g., to define spare marks.
 * <p>
 * 
 * The course template has a globally unique ID and with this can have a life cycle. It is immutable. If a logical copy
 * is created, the new copy will have a different ID ("Save as...").
 * <p>
 * 
 * The {@link MarkTemplate}s also have their globally unique IDs. When creating a variant, the new copy can reference
 * the <em>same</em> set of a subset of the mark templates that the original references. This is helpful in case
 * tracking options or bindings to physical marks are created and remembered. For example, a course template for an "I"
 * (inner loop) course and a course template for an "O" (outer loop) course may share the mark templates for the start
 * line and the "1" top mark, and when tracker bindings are established, they can automatically be applied to all marks
 * created from the same {@link MarkTemplate}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseTemplate extends NamedWithID {
    Iterable<MarkTemplate> getMarks();

    Iterable<WaypointTemplate> getWaypoints(int numberOfLaps);
}
