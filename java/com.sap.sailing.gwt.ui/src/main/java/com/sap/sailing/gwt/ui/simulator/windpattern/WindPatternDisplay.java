package com.sap.sailing.gwt.ui.simulator.windpattern;

import java.util.List;


public interface WindPatternDisplay {
    
    public List<WindPatternSetting<?>> getSettings();
    
    public void addSetting(WindPatternSetting<?> setting);

    public String getWindPatternName();
}
