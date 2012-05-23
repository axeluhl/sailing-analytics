package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;


public interface WindPatternSetting<SettingsType> {
    
    public enum DisplayWidgetType {
        SLIDERBAR,
        LISTBOX
    }
    
    public DisplayWidgetType getDisplayWidgetType();
    
    public SettingsType getMin();
    
    public SettingsType getMax();
    
    public SettingsType getDefault();
    
    public List<SettingsType> getValues();
    
}
