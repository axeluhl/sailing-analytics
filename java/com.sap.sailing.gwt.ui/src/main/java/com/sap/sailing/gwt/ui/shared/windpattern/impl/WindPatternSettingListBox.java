package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class WindPatternSettingListBox implements WindPatternSetting<String>, IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5396311857725053774L;
    /**
     * name should match the corresponding field name from @WindControlParameters for which this control is being set
     */
    private String name;
    private String displayName;
    private String currentValue;
    private List<String> values;
    
    /**
     * Required for serialization
     */
    public WindPatternSettingListBox() {
        
    }
    
    public WindPatternSettingListBox(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        this.values = new ArrayList<String>();
    }
    
    @Override
    public DisplayWidgetType getDisplayWidgetType() {
       return DisplayWidgetType.LISTBOX;
    }

    @Override
    public String getMin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMax() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefault() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getValues() {
       return values;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override 
    public String toString() {
        return getName() + " " + getDisplayWidgetType() +  " " + getValues();
        
    }

    @Override
    public void setValue(String value) {
        this.currentValue = value;
        
    }

    @Override
    public String getValue() {
      return currentValue;
    }

    @Override
    public void setValue(Double value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getDisplayName() {
       return displayName;
    }
}
