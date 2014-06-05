package com.sap.sailing.gwt.ui.simulator.windpattern;

import java.util.List;

import com.sap.sse.common.Named;


public interface WindPatternSetting<SettingsType> extends Named {
    
    public enum DisplayWidgetType {
        SLIDERBAR,
        LISTBOX
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
    
    public String getDisplayName();
    
}
