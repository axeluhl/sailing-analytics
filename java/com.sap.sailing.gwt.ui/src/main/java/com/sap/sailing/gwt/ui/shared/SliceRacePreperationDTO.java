package com.sap.sailing.gwt.ui.shared;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

public class SliceRacePreperationDTO implements IsSerializable {
    
    private String proposedRaceName;
    private HashSet<String> alreadyUsedNames;
    
    public SliceRacePreperationDTO(String proposedRaceName, Iterable<String> alreadyUsedNames) {
        this.proposedRaceName = proposedRaceName;
        this.alreadyUsedNames = new HashSet<>();
        Util.addAll(alreadyUsedNames, this.alreadyUsedNames);
    }
    
    protected SliceRacePreperationDTO() {
    }

    public String getProposedRaceName() {
        return proposedRaceName;
    }
    
    public Set<String> getAlreadyUsedNames() {
        return alreadyUsedNames;
    }
}
