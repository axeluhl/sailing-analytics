package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.HashSet;
import java.util.Set;

/**
 * This class defines if a given extremal value is within boundaries that are flexible by a given halfBoundaryWidth. The halfBoundaryWidth is
 * defined by a percentage of the difference between max and min of a value range. It needs four types of boundaries: 
 * a) minLeft
 * b) maxLeft
 * c) minRight 
 * d) maxRight 
 * (a)<--minValue->(b)..........(c)<--maxValue->(d) 
 * If an extremal value is not within the left or the right boundaries the boundaries should be adjusted in a way that the value will fit in the
 * one or the other. Also there should be the possibility to add listeners that will notify on a boundary change.
 * 
 * A ValueRangeFlexibleBoundaries needs an initialMin, a initialMax and a percentage that indicates the width of the value range
 * surrounding initialMin and Max.
 * 
 * @author D073259
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
    private double halfBoundaryWidth;
    private final Set<ValueRangeFlexibleBoundariesChangedListener> valueRangeChangedListeners;

    public ValueRangeFlexibleBoundaries(double initialMin, double initialMax, double percentage) {
        this.percentage = percentage;
        valueRangeChangedListeners = new HashSet<>();
        min = initialMin;
        max = initialMax;
        update();
    }
    private void update() {
        halfBoundaryWidth = (max - min) * (percentage);
        minLeft = min - halfBoundaryWidth;
        maxLeft = min + halfBoundaryWidth;
        minRight = max - halfBoundaryWidth;
        maxRight = max + halfBoundaryWidth;
        notifyListeners();
    }
    public void checkIfValueIsInRightBoundaryRangeAndUpdateIfNecessary(double maxValue) {
        if (!isValueInRightRange(maxValue)) {
            max = maxValue;
            update();
        }
    }
    public void checkIfValueIsInLeftBoundaryRangeAndUpdateIfNecessary(double minValue) {
        if (!isValueInLeftRange(minValue)) {
            min = minValue;
            update();
        }
    }
    private boolean isValueInLeftRange(double value) {
        if (value >= minLeft && value <= maxLeft) {
            return true;
        } else {
            return false;
        }
    }
    private boolean isValueInRightRange(double value) {
        if (value >= minRight && value <= maxRight) {
            return true;
        } else {
            return false;
        }
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
