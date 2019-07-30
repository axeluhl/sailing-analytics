package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.Util.Pair;

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
public interface CourseTemplate extends NamedWithID, HasTags {
    /**
     * The templates for all the marks that shall be made available in the regatta when applying this template. All
     * marks required to construct the waypoint sequence must be produced from this set of mark templates. There may be
     * additional mark templates returned by this method, for constructing marks not immediately required for the
     * waypoint sequence but, e.g., as proposals for spare or alternative marks. For example, templates for alternative
     * marks for the windward mark may be returned to quickly accommodate for wind shifts.
     */
    Iterable<MarkTemplate> getMarks();

    /**
     * Returns a sequence of {@link WaypointTemplate}s that can be use to construct a course. If this course template
     * defines a repeatable waypoint sub-sequence, the {@code numberOfLaps} parameter is used to decide how many times
     * to repeat this sub-sequence. Typically, the repeatable sub-sequence will be repeated one times fewer than the
     * {@code numberOfLaps}. For example, in a typical windward-leeward "L" course we would have
     * {@code Start/Finish, [1, 4p/4s], 1, Start/Finish}. For an "L1" course with only one lap, we'd like to have
     * {@code Start/Finish, 1, Start/Finish}, so the repeatable sub-sequence, enclosed by the brackets in the example
     * above, will occur zero times. For an "L2" the repeatable sub-sequence will occur once, and so on. However, an
     * implementation is free to choose an interpretation of {@code numberOfLaps} that meets callers' expectations.
     * 
     * @param numberOfLaps
     *            if the course defines a repeatable part, the number of laps at least needs to be {@code 1} for the
     *            default implementation, and an {@link IllegalArgumentException} shall be thrown in case a value less
     *            than {@code 1} is used if this template specifies a repeatable part. Note again that the number of
     *            repetitions of the repeatable part is usually one less than the number of laps, therefore this
     *            limitation.
     */
    Iterable<WaypointTemplate> getWaypoints(int numberOfLaps);
    
    boolean hasRepeatablePart();
    
    Pair<Integer, Integer> getRepeatablePart();
}
