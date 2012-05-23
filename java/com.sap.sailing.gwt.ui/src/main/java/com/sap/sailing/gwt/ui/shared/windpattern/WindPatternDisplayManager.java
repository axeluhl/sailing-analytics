package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.impl.WindPatternDisplayManagerImpl;

public interface WindPatternDisplayManager {
    
   public enum WindPattern {
        CONSTANT,
        OSCILLATING,
        ANOTHERONE
    }
    
    static WindPatternDisplayManager INSTANCE = new WindPatternDisplayManagerImpl();
    
    public List<WindPatternDTO> getWindPatterns();
    
    public WindPatternDisplay getDisplay(WindPattern windPattern);
}
