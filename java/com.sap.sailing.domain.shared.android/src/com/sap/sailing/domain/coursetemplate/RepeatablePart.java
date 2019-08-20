package com.sap.sailing.domain.coursetemplate;

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
public interface RepeatablePart {
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
     * From the {@code sequenceWithRepeatablePart} a sequence is created with the number of laps based on the
     * {@link #getZeroBasedIndexOfRepeatablePartStart()} and {@link #getZeroBasedIndexOfRepeatablePartEnd()} of this
     * Repeatable part. The sequence is constructed using the following algorithm:
     * <ul>
     * <li>Part with 0<= index < {@link #getZeroBasedIndexOfRepeatablePartStart()}. In case,
     * {@link #getZeroBasedIndexOfRepeatablePartStart()} == 0, this part is empty.</li>
     * <li>Part with {@link #getZeroBasedIndexOfRepeatablePartStart()} <= index <=
     * {@link #getZeroBasedIndexOfRepeatablePartEnd()}. In case, {@link #getZeroBasedIndexOfRepeatablePartStart()} ==
     * {@link #getZeroBasedIndexOfRepeatablePartEnd()}, this part is just one item having the index of both, start and
     * end.</li>
     * <li>Part with {@link #getZeroBasedIndexOfRepeatablePartEnd()} < index <= last entry. In case,
     * {@link #getZeroBasedIndexOfRepeatablePartEnd()} == last entry, this part is empty.</li>
     * </ul>
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
        for (int lap = 0; lap < laps; lap++) {
            for (int i = zeroBasedIndexOfRepeatablePartStart; i <= zeroBasedIndexOfRepeatablePartEnd; i++) {
                result.add(sequenceWithRepeatablePartAsList.get(i));
            }
        }
        // Non-repeatable end of the sequence
        for (int i = zeroBasedIndexOfRepeatablePartEnd + 1; i < sequenceLength; i++) {
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
        if (sequenceLength <= zeroBasedIndexOfRepeatablePartEnd) {
            throw new IllegalArgumentException("Repeatable part (" + getZeroBasedIndexOfRepeatablePartStart() + ", "
                    + zeroBasedIndexOfRepeatablePartEnd + ") is out of range for sequence of length " + sequenceLength);
        }
    }
}
