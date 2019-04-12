package com.sap.sse.common;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages a number range that contains a minimum and a maximum value. Those minimum and maximum values can be updated.
 * If at least one of the two new values is outside of the range, the range is adjusted to ensure it again contains both
 * values. The range will also shrink if the new minimum/maximum values are too far away from the range boundaries. This
 * compares to a "Schmitt Trigger" for each end of the range, such that for the minimum and the maximum value there is
 * each a value range at the respective end of the overall range, and when the minimum/maximum value is outside of its
 * range the overall range will be adjusted.
 * <p>
 * 
 * When the range is initialized or updated, the ranges for the minimum and the maximum value at each end of the overall
 * range are determined symmetrically around the minimum/maximum value, with the {@link Math#max(double, double)} given {@link #percentage} of the absolute
 * difference between the minimum and maximum value as leeway either way. This percentage is provided to this class's
 * constructor. For example, if the overall range is -5..5, its length is 10. If a 10% (0.1) leeway is configured, this
 * means that 10% of the length of 10 which equals 1 is provided around each extremal value, yielding a range from
 * -6..-4 around the minimum value -5, and a range from 4..6 around the maximum value 5.
 * <p>
 * 
 * Visually, if we have:
 * <p>
 * 
 * a) minLeft<br>
 * b) maxLeft<br>
 * c) minRight<br>
 * d) maxRight
 * <p>
 * then the range can be imagined like this:
 * <p>
 * 
 * (a)&lt;--minValue--&gt;(b)..........(c)&lt;--maxValue--&gt;(d)
 * <p>
 * 
 * If an extremal value is not within the left or the right boundaries the boundaries will be adjusted as described
 * above, and all {@link ValueRangeFlexibleBoundariesChangedListener listeners} that were
 * {@link #addListener(ValueRangeFlexibleBoundariesChangedListener) added} will be notified.
 * <p>
 * 
 * A ValueRangeFlexibleBoundaries needs an initialMin, an initialMax, a percentage that indicates the width of the
 * value range surrounding initialMin and Max and minimumHalfBoundaryWidth.
 * 
 * @author D073259 (Alessandro Stoltenberg)
 *
 */
public class ValueRangeFlexibleBoundaries {
    private double minLeft;
    private double maxLeft;
    private double minRight;
    private double maxRight;
    private double min;
    private double max;
    private final double percentage;
    private final double minimumHalfBoundaryWidth;
    private double halfBoundaryWidth;
    private final Set<ValueRangeFlexibleBoundariesChangedListener> valueRangeChangedListeners;

    public ValueRangeFlexibleBoundaries(double initialMin, double initialMax, double percentage, double minimumHalfBoundaryWidth) {
        this.percentage = percentage;
        this.minimumHalfBoundaryWidth = minimumHalfBoundaryWidth;
        valueRangeChangedListeners = new HashSet<>();
        min = initialMin;
        max = initialMax;
        update();
    }
    
    private void update() {
        halfBoundaryWidth = Math.max((max - min) * (percentage), minimumHalfBoundaryWidth);
        minLeft = min - halfBoundaryWidth;
        maxLeft = min + halfBoundaryWidth;
        minRight = max - halfBoundaryWidth;
        maxRight = max + halfBoundaryWidth;
        notifyListeners();
    }
    
    /**
     * Updates the minimum and maximum value. If {@code minValue} is greater than {@code maxValue},
     * an {@link IllegalArgumentException} will be thrown. If any of the values exceeds its range at
     * its respective end of this overall range then the overall range will be adjusted, and the
     * listeners will be notified.
     */
    public void setMinMax(double minValue, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue "+minValue+" is greater than maxValue "+maxValue);
        }
        min = minValue;
        max = maxValue;
        if (!isValueInRightRange(maxValue) || !isValueInLeftRange(minValue)) {
            update();
        }
        assert minLeft <= minValue;
        assert minValue <= maxLeft;
        assert minRight <= maxValue;
        assert maxValue <= maxRight;
    }
    
    private boolean isValueInLeftRange(double value) {
        return value >= minLeft && value <= maxLeft;
    }
    
    private boolean isValueInRightRange(double value) {
        return value >= minRight && value <= maxRight;
    }
    
    public double getMinLeft() {
        return minLeft;
    }
    
    public double getMaxRight() {
        return maxRight;
    }
    
    public void addListener(ValueRangeFlexibleBoundariesChangedListener listener) {
        valueRangeChangedListeners.add(listener);
    }
    
    public void removeListener(ValueRangeFlexibleBoundariesChangedListener listener) {
        valueRangeChangedListeners.remove(listener);
    }
    
    public void notifyListeners() {
        for (ValueRangeFlexibleBoundariesChangedListener listener : valueRangeChangedListeners) {
            listener.onValueRangeBoundariesChanged();
        }
    }
}
