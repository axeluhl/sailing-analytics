package com.sap.sailing.gwt.ui.simulator.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPattern;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternSetting;

public class WindPatternDisplayImpl implements WindPatternDisplay, IsSerializable {
    
    private String windPatternName;
    private List<WindPatternSetting<?>> windPatternSettings;
    
    public WindPatternDisplayImpl() {
        windPatternSettings = new ArrayList<WindPatternSetting<?>>();
    }
    
    public WindPatternDisplayImpl(WindPattern windPattern) {
        this.windPatternName = windPattern.name();
        windPatternSettings = new ArrayList<WindPatternSetting<?>>();
    }
    
    @Override
    public List<WindPatternSetting<?>> getSettings() {
        return windPatternSettings;
    }

    @Override
    public void addSetting(WindPatternSetting<?> setting) {
        windPatternSettings.add(setting);
    }
    
    @Override
    public String getWindPatternName() {
        return windPatternName;
    }

}
