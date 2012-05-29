package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager.WindPattern;


public interface WindPatternDisplay {
    
    public List<WindPatternSetting<?>> getSettings();
    
    public void addSetting(WindPatternSetting<?> setting);

    public WindPattern getWindPattern();
}
