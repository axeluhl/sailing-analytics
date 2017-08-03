package com.sap.sailing.domain.anniversary;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class SimpleAnniversaryRaceInfo {

    protected RegattaAndRaceIdentifier identifier;
    protected Date startOfRace;
    private String remoteName;
    
    public SimpleAnniversaryRaceInfo(RegattaAndRaceIdentifier identifier, Date startOfRace) {
        if(identifier == null || startOfRace == null){
            throw new IllegalStateException("Anniversary Data is not allowed to contain any null values!");
        }
        this.identifier = identifier;
        this.startOfRace = startOfRace;
    }

    public RegattaAndRaceIdentifier getIdentifier() {
        return identifier;
    }

    public Date getStartOfRace() {
        return startOfRace;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }
    
    
}