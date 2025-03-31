package com.sap.sailing.gwt.ui.simulator.windpattern.impl;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternSetting;

public class WindPatternSettingSliderBar implements WindPatternSetting<Double>, IsSerializable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 2817622009812937399L;
    /**
     * name should match the corresponding field name from @WindControlParameters for which this control is being set
     */
    private String name;
    private SettingName settingName;
    private double currentValue;
    private double min;
    private double max;
    private double res; // resolution of slider bar
    private double defaultValue;
    private int steps; // number of tickmarks

    /**
     * Required for serialization
     */
    public WindPatternSettingSliderBar() {

    }

    public WindPatternSettingSliderBar(String name, SettingName settingName, double min, double max, double res,
            double defaultValue, int steps) {
        this.name = name;
        this.settingName = settingName;
        this.min = min;
        this.max = max;
        this.res = res;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.steps = steps;
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
    public Double getResolution() {
        return res;
    }

    @Override
    public Double getDefault() {
        return defaultValue;
    }

    @Override
    public int getSteps() {
        return steps;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " " + getDisplayWidgetType() + " Min:" + getMin() + " Max: " + getMax() + " Default:"
                + getDefault();

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

    @Override
    public SettingName getSettingName() {
        return settingName;
    }
}
