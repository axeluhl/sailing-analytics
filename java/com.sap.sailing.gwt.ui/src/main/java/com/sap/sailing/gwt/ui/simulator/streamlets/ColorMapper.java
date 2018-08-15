package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.HashSet;
import java.util.Set;

/**
 * This class maps a given range of values to a full color spectrum. The color spectrum could also be a grey scale.
 * Therefore it needs the max and the min value to be mapped. It uses a {@link ValueRangeFlexibleBoundaries} to get those. The
 * ColorMapper has listeners that will be notified if the colormapping has been changed. The ColorMapper accepts value
 * ranges that are negative, negative numbers will display as blue (hsl(240, 100%, 50%)) or fully transparent grey
 * (rgba(255,255,255,1.0)).
 * 
 * @author D073259
 *
 */
public class ColorMapper implements ValueRangeFlexibleBoundariesChangedListener {
    private final ValueRangeFlexibleBoundaries valueRange;
    private double minValue;
    private double maxValue;
    private boolean isGrey;
    private final Set<ColorMapperChangedListener> colorMapperChangedListeners;

    public ColorMapper(ValueRangeFlexibleBoundaries valueRange, boolean isGrey) {
        this.valueRange = valueRange;
        this.valueRange.addListener(this);
        minValue = this.valueRange.getMinLeft();
        maxValue = this.valueRange.getMaxRight();
        this.isGrey = isGrey;
        colorMapperChangedListeners = new HashSet<>();
    }

    public void setGrey(boolean isGrey) {
        this.isGrey = isGrey;
        notifyListeners();
    }

    public String getColor(double value) {
        if (isGrey) {
            if (value < 0) {
                return "rgba(255,255,255, 1.0)";
            }
            return "rgba(255,255,255," + Math.min(1.0, (value - minValue) / (maxValue - minValue)) + ")";
        } else {
            if (value < 0) {
                return "hsl(240, 100%, 50%)";
            }
            double h = (1 - (value - minValue) / (maxValue - minValue)) * 240;
            return "hsl(" + Math.round(h) + ", 100%, 50%)";
        }
    }

    private void updateMinMax() {
        minValue = valueRange.getMinLeft();
        maxValue = valueRange.getMaxRight();
    }

    @Override
    public void onValueRangeBoundariesChanged() {
        updateMinMax();
        notifyListeners();
    }
    public void addListener(ColorMapperChangedListener listener) {
        colorMapperChangedListeners.add(listener);
    }
    public void removeListener(ColorMapperChangedListener listener) {
        colorMapperChangedListeners.remove(listener);
    }
    public void notifyListeners() {
        for (ColorMapperChangedListener listener : colorMapperChangedListeners) {
            listener.onColorMappingChanged();
        }
    }
}
