package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;


public interface WindPatternDisplay {
    
    public List<WindPatternSetting<?>> getSettings();
    
    public void addSetting(WindPatternSetting<?> setting);

    public String getWindPatternName();
}
