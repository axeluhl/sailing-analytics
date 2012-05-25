package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class WindPatternDisplayManagerImpl implements  WindPatternDisplayManager {
    
   
    
    public WindPatternDisplayManagerImpl() {
        
    }
    
    @Override
    public List<WindPatternDTO> getWindPatterns() {
        List<WindPatternDTO> list = new ArrayList<WindPatternDTO>();
        for (WindPattern w : WindPattern.values()) {
            list.add(new WindPatternDTO(w.name(),w.getDisplayName()));
        }
        return list;
    }

    @Override
    public WindPatternDisplay getDisplay(WindPattern windPattern) {
        WindPatternDisplay display = new WindPatternDisplayImpl();
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseSpeed", "Base Wind Speed", 1,10,1);
        display.addSetting(windSpeedSetting);  
       
        switch(windPattern) {
            case BLASTS :          
                break;
            case OSCILLATIONS :
                WindPatternSetting<Double> noise = new WindPatternSettingSliderBar("noise", "Noise", 0,1,0);
                display.addSetting(noise);
                WindPatternSetting<String> names = new WindPatternSettingListBox("names", "Names");
                names.getValues().add("A");
                names.getValues().add("B");
                display.addSetting(names);
                break;
            case OSCILLATION_WITH_BLASTS :
                break;
        }
        return display;
    }
    
    
}
