package com.sap.sailing.xrr.structureimport;

public class RegattaStructureKey {
    
    /*RegattaStructureKey which consists of series and fleet names*/
    private final Iterable<String> regattaStructureKey;

    public RegattaStructureKey(Iterable<String> regattaStructureKey) {
        this.regattaStructureKey = regattaStructureKey;
    }

    public Iterable<String> getRegattaStructureKey() {
        return regattaStructureKey;
    }
    
    //define HashCode and equals?

}
