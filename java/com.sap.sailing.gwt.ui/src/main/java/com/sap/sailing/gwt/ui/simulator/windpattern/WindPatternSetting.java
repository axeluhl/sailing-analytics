package com.sap.sailing.gwt.ui.simulator.windpattern;

import java.util.List;

import com.sap.sse.common.Named;


public interface WindPatternSetting<SettingsType> extends Named {
    
    public enum DisplayWidgetType {
        SLIDERBAR,
        LISTBOX
    }
    
    public enum SettingName {
        BASE_BEARING_IN_DEGREES, RACE_COURSE_DIFF_IN_DEGREES, BASE_SPEED_IN_KNOTS, PROPABILITY_IN_PERCENT, GUST_SIZE,
        AVERAGE_SPEED_IN_PERCENT, SPEED_VARIANCE_IN_PERCENT, AVERAGE_DIRECTION_IN_DEGREES, SPEED_LEFT_SIDE_IN_PERCENT,
        SPEED_MIDDLE_IN_PERCENT, SPEED_RIGHT_SIDE_IN_PERECENT, FREQUENCY_PER_HOURS, AMPLITUDE_IN_DEGREES,
        CURRENT_SPEED_IN_KNOTS, CURRENT_BEARING_IN_DEGREES;
    }
    
    public DisplayWidgetType getDisplayWidgetType();
    
    public SettingsType getMin();
    
    public SettingsType getMax();
    
	public SettingsType getResolution();

	public SettingsType getDefault();
    
    public int getSteps();
    
    public List<SettingsType> getValues();
    
    public void setValue(Double value);
    
    public void setValue(String value);
    
    public SettingsType getValue();
    
    public SettingName getSettingName();
    
}
