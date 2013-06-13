package com.sap.sailing.gwt.ui.shared.windpattern;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.impl.WindPatternDisplayManagerImpl;

public interface WindPatternDisplayManager {
        
    static WindPatternDisplayManager INSTANCE = new WindPatternDisplayManagerImpl();
    
    public void setMode(char mode);
    
    public List<WindPatternDTO> getWindPatterns(char mode);
    
    public WindPatternDisplay getDisplay(WindPattern windPattern);

}
