package com.sap.sailing.xrr.structureimport;

import java.util.Map;

public class RegattaStructureKey {
    
    /*RegattaStructureKey which consists of series and fleet names*/
    private final Map<String, String> regattaStructureKey;

    public RegattaStructureKey(Map<String, String> regattaStructureKey) {
        this.regattaStructureKey = regattaStructureKey;
    }

    public Map<String, String> getRegattaStructureKey() {
        return regattaStructureKey;
    }
    
    //define HashCode and equals?

}
