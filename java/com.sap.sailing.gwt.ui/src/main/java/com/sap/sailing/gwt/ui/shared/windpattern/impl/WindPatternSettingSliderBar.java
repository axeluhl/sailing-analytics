package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class WindPatternSettingSliderBar implements WindPatternSetting<Double>, IsSerializable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 2817622009812937399L;
    private String name;
    private double currentValue;
    private double min;
    private double max;
    private double defaultValue;
    
    /**
     * Required for serialization
     */
    public WindPatternSettingSliderBar() {
        
    }
    
    public WindPatternSettingSliderBar(String name, double min, double max, double defaultValue) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
    }

    @Override
    public DisplayWidgetType getDisplayWidgetType() {
        // TODO Auto-generated method stub
        return WindPatternSetting.DisplayWidgetType.SLIDERBAR;
    }

    @Override
    public List<Double> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getMin() {
        return min;
    }

    @Override
    public Double getMax() {
        return max;
    }

    @Override
    public Double getDefault() {
        return defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " " + getDisplayWidgetType() + " Min:" + getMin() + " Max: " + getMax() + " Default:" + getDefault();

    }

    @Override
    public void setValue(Double value) {
       this.currentValue = value;
        
    }

    @Override
    public Double getValue() {
        return currentValue;
    }

    @Override
    public void setValue(String value) {
        // TODO Auto-generated method stub
        
    }
}
