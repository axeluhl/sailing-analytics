package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager.WindPattern;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class WindPatternDisplayImpl implements WindPatternDisplay, IsSerializable {
    
    private WindPattern windPattern;
    private List<WindPatternSetting<?>> windPatternSettings;
    
    public WindPatternDisplayImpl(WindPattern windPattern) {
        windPattern = windPattern;
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
    public WindPattern getWindPattern() {
        return windPattern;
    }

}
