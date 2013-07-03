package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;


/**
 * Keeps track of how many instances of domain classes are created in the process of
 * the master data import.
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public class MasterDataImportObjectCreationCountImpl implements MasterDataImportObjectCreationCount {
    
    private static final long serialVersionUID = -3677005970701170818L;
    private int leaderboardCount = 0;
    private int leaderboardGroupCount = 0;
    private int eventCount = 0;
    private int regattaCount = 0;
    
    //For GWT serialization
    public MasterDataImportObjectCreationCountImpl() {};

    public void addOneLeaderboard() {
        leaderboardCount++;
    }
    
    public void addOneLeaderboardGroup() {
        leaderboardGroupCount++;
    }
    
    public void addOneEvent() {
        eventCount++;
    }

    public void addOneRegatta() {
        regattaCount++;
        
    }

    public void add(MasterDataImportObjectCreationCountImpl toAdd) {
        leaderboardCount = leaderboardCount + toAdd.leaderboardCount;
        leaderboardGroupCount = leaderboardGroupCount + toAdd.leaderboardGroupCount;
        eventCount = eventCount + toAdd.eventCount;
        regattaCount = regattaCount + toAdd.regattaCount;
    }

    @Override
    public int getLeaderboardCount() {
        return leaderboardCount;
    }

    @Override
    public int getLeaderboardGroupCount() {
        return leaderboardGroupCount;
    }

    @Override
    public int getEventCount() {
        return eventCount;
    }

    @Override
    public int getRegattaCount() {
        return regattaCount;
    }
   
}
