package com.sap.sailing.domain.coursetemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;

/**
 * Reusable implementation of the semantics for the repeatable part of a course sequence. The sequence is defined by the
 * start and end of the zero-based repeatable part of the complete sequence. The following requirements need to be
 * fulfilled:
 * <ul>
 * <li>The {@link #getZeroBasedIndexOfRepeatablePartStart()} and {@link #getZeroBasedIndexOfRepeatablePartEnd()} are
 * required to be >=0</li>
 * <li>{@link #getZeroBasedIndexOfRepeatablePartStart()} is <= {@link #getZeroBasedIndexOfRepeatablePartEnd()}</li>
 * </ul>
 */
public interface RepeatablePart extends Serializable {
    /**
     * The index into {@link #waypoints} of the first waypoint that is to be cloned for repetitive laps.
     */
    int getZeroBasedIndexOfRepeatablePartStart();

    /**
     * The index into {@link #waypoints} of the first waypoint that comes after the sub-sequence to be cloned for
     * repetitive laps.
     */
    int getZeroBasedIndexOfRepeatablePartEnd();

    /**
     * Returns a sequence of waypoints (T) that can be use to construct a course. If this course template
     * defines a repeatable waypoint sub-sequence, the {@code numberOfLaps} parameter is used to decide how many times
     * to repeat this sub-sequence. Typically, the repeatable sub-sequence will be repeated one times fewer than the
     * {@code numberOfLaps}. For example, in a typical windward-leeward "L" course we would have
     * {@code Start/Finish, [1, 4p/4s], 1, Start/Finish}. For an "L1" course with only one lap, we'd like to have
     * {@code Start/Finish, 1, Start/Finish}, so the repeatable sub-sequence, enclosed by the brackets in the example
     * above, will occur zero times. For an "L2" the repeatable sub-sequence will occur once, and so on.
     * 
     * @throws IllegalArgumentException
     *             in case the given {@code sequenceWithRepeatablePart} contains less or equal elements to
     *             {@link #getZeroBasedIndexOfRepeatablePartEnd()}.
     */
    default <T> Iterable<T> createSequence(int laps, Iterable<T> sequenceWithRepeatablePart) {
        validateRepeatablePartForSequence(sequenceWithRepeatablePart);
        final int zeroBasedIndexOfRepeatablePartStart = getZeroBasedIndexOfRepeatablePartStart();
        final int zeroBasedIndexOfRepeatablePartEnd = getZeroBasedIndexOfRepeatablePartEnd();
        final List<T> sequenceWithRepeatablePartAsList = new ArrayList<>();
        Util.addAll(sequenceWithRepeatablePart, sequenceWithRepeatablePartAsList);
        final int sequenceLength = sequenceWithRepeatablePartAsList.size();
        final List<T> result = new ArrayList<>();
        // Non-repeatable start of the sequence
        for (int i = 0; i < zeroBasedIndexOfRepeatablePartStart; i++) {
            result.add(sequenceWithRepeatablePartAsList.get(i));
        }
        // Repeatable part of the sequence
        for (int lap = 1; lap < laps; lap++) {
            for (int i = zeroBasedIndexOfRepeatablePartStart; i < zeroBasedIndexOfRepeatablePartEnd; i++) {
                result.add(sequenceWithRepeatablePartAsList.get(i));
            }
        }
        // Non-repeatable end of the sequence
        for (int i = zeroBasedIndexOfRepeatablePartEnd; i < sequenceLength; i++) {
            result.add(sequenceWithRepeatablePartAsList.get(i));
        }
        return result;
    }

    /**
     * Validates that this repeatable part is compatible with the given {@code sequenceWithRepeatablePart} and causes an
     * exception in case it isn't.
     * 
     * @throws IllegalArgumentException
     *             in case the given {@code sequenceWithRepeatablePart} contains less or equal elements to
     *             {@link #getZeroBasedIndexOfRepeatablePartEnd()}.
     */
    default <T> void validateRepeatablePartForSequence(Iterable<T> sequenceWithRepeatablePart) {
        final int zeroBasedIndexOfRepeatablePartEnd = getZeroBasedIndexOfRepeatablePartEnd();
        final int sequenceLength = Util.size(sequenceWithRepeatablePart);
        if (sequenceLength < zeroBasedIndexOfRepeatablePartEnd) {
            throw new IllegalArgumentException("Repeatable part (" + getZeroBasedIndexOfRepeatablePartStart() + ", "
                    + zeroBasedIndexOfRepeatablePartEnd + ") is out of range for sequence of length " + sequenceLength);
        }
    }
}
