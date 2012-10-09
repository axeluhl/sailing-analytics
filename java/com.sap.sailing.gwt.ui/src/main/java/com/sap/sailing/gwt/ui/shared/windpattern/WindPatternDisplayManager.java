package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.impl.WindPatternDisplayManagerImpl;

public interface WindPatternDisplayManager {
    
   public enum WindPattern {
        NONE ("Choose a wind pattern"),
        OSCILLATIONS ("Oscillations"),
        OSCILLATION_WITH_BLASTS ("Oscillation with Gusts"),
        BLASTS ("Gusts"),
        MEASURED ("Measured");
        
        private String displayName;
        WindPattern(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    static WindPatternDisplayManager INSTANCE = new WindPatternDisplayManagerImpl();
    
    public List<WindPatternDTO> getWindPatterns();
    
    public WindPatternDisplay getDisplay(WindPattern windPattern);
}
