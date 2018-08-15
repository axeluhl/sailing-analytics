package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.HashSet;
import java.util.Set;

/**
 * This class defines if a given value is within boundaries that are flexible by a given rangeWidth. The rangeWidth is
 * defined by a percentage of the minimum value, otherwise the adjustment of the boundaries will be biased and the max
 * boundaries are always much bigger then the minimum boundaries. It needs four types of boundaries: 
 * a) minLeft
 * b) maxLeft
 * c) minRight 
 * d) maxRight 
 * (a)<--value->(b)..........(c)<--value->(d) 
 * If a value is not within the left or the right boundaries the boundaries should be adjusted in a way that the value will fit in the
 * one or the other. Also there should be the possibility to add listeners that will notify on a boundary change.
 * 
 * A ValueRangeBoundaries needs an initialMin, a initialMax and a percentage that indicates the width of the value range
 * surrounding initialMin and Max.
 * 
 * @author D073259
 *
 */
public class ValueRangeBoundaries {
    private double minLeft;
    private double maxLeft;
    private double minRight;
    private double maxRight;
    private final double percentage;
    private double halfRangeWidth;
    private final Set<ValueRangeBoundariesChangedListener> valueRangeChangedListeners;

    public ValueRangeBoundaries(double initialMin, double initialMax, double percentage) {
        this.percentage = percentage;
        updateLeft(initialMin);
        updateRight(initialMax);
        valueRangeChangedListeners = new HashSet<>();
        this.halfRangeWidth = ((initialMin * (1 + percentage)) - (initialMin * (1 - percentage)))/2;
    }
    private void updateLeft(double value) {
        minLeft = value - halfRangeWidth;
        maxLeft = value + halfRangeWidth;
    }
    private void updateRight() {
        minRight =- halfRangeWidth;
        maxRight =+ halfRangeWidth;
    }
    private void updateRight(double value) {
        minRight = value - halfRangeWidth;
        maxRight = value + halfRangeWidth;
    }
    private void updateRangeWidth(double newMinValue) {
        halfRangeWidth = ((newMinValue * (1 + percentage)) - (newMinValue * (1 - percentage)))/2;
    }
    public void checkIfValueIsInRightBoundaryRangeAndUpdateIfNecessary(double value) {
        if (!isValueInRightRange(value)) {
            updateRight(value);
            notifyListeners();
        }
    }
    public void checkIfValueIsInLeftBoundaryRangeAndUpdateIfNecessary(double value) {
        if (!isValueInLeftRange(value)) {
            updateRangeWidth(value);
            updateRight();
            updateLeft(value);
            notifyListeners();
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
    public void addListener(ValueRangeBoundariesChangedListener listener) {
        valueRangeChangedListeners.add(listener);
    }
    public void removeListener(ValueRangeBoundariesChangedListener listener) {
        valueRangeChangedListeners.remove(listener);
    }
    public void notifyListeners() {
        for (ValueRangeBoundariesChangedListener listener : valueRangeChangedListeners) {
            listener.onValueRangeBoundariesChanged();
        }
    }
}
