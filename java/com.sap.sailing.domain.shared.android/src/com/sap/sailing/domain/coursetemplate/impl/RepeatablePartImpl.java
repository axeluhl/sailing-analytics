package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.coursetemplate.RepeatablePart;

public class RepeatablePartImpl implements RepeatablePart {
    private static final long serialVersionUID = -7432656529810302123L;

    private final int zeroBasedIndexOfRepeatablePartStart;

    private final int zeroBasedIndexOfRepeatablePartEnd;

    public RepeatablePartImpl(int zeroBasedIndexOfRepeatablePartStart, int zeroBasedIndexOfRepeatablePartEnd) {
        super();
        if (zeroBasedIndexOfRepeatablePartStart < 0 || zeroBasedIndexOfRepeatablePartEnd < 0) {
            throw new IllegalArgumentException("No negative indices are possible for the repeatable part.");
        }
        if (zeroBasedIndexOfRepeatablePartStart >= zeroBasedIndexOfRepeatablePartEnd) {
            throw new IllegalArgumentException(
                    "The start of the repeatable part needs to be less than the end such that the repeatable part has at least one element.");
        }
        this.zeroBasedIndexOfRepeatablePartStart = zeroBasedIndexOfRepeatablePartStart;
        this.zeroBasedIndexOfRepeatablePartEnd = zeroBasedIndexOfRepeatablePartEnd;
    }

    @Override
    public int getZeroBasedIndexOfRepeatablePartStart() {
        return zeroBasedIndexOfRepeatablePartStart;
    }

    @Override
    public int getZeroBasedIndexOfRepeatablePartEnd() {
        return zeroBasedIndexOfRepeatablePartEnd;
    }

    @Override
    public String toString() {
        return "["+getZeroBasedIndexOfRepeatablePartStart()+".."+getZeroBasedIndexOfRepeatablePartEnd()+"]";
    }
}
