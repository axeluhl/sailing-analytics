package com.sap.sailing.domain.common.impl;

import java.util.HashSet;
import java.util.Set;


/**
 * Keeps track of how many instances of domain classes are created in the process of
 * the master data import.
 * 
 * Also keeps track of names and ids of the created objects to ignore one that are 
 * listed multiple times.
 * 
 * @author Frederik Petersen (D054528)
 *
 */
public class MasterDataImportObjectCreationCountImpl extends BaseMasterDataImportObjectCreationCountImpl {
    private static final long serialVersionUID = -3677005970701170818L;
    private Set<String> namesOfCreatedLeaderboards = new HashSet<>(); 
    private Set<String> idsOfCreatedLeaderboardGroups = new HashSet<>(); 
    private Set<String> idsOfCreatedEvents = new HashSet<>(); 
    private Set<String> namesOfCreatedRegattas = new HashSet<>(); 
    private Set<String> idsOfCreatedTrackedRaces = new HashSet<>();

    public MasterDataImportObjectCreationCountImpl() {
        super();
    };

    public void addOneLeaderboard(String name) {
        super.addOneLeaderboard(name);
        namesOfCreatedLeaderboards.add(name);
    }
    
    public void addOneLeaderboardGroup(String name) {
        super.addOneLeaderboardGroup(name);
        idsOfCreatedLeaderboardGroups.add(name);
    }
    
    public void addOneEvent(String idAsString) {
        super.addOneEvent(idAsString);
        idsOfCreatedEvents.add(idAsString);
    }

    public void addOneRegatta(String idAsString) {
        super.addOneRegatta(idAsString);
        namesOfCreatedRegattas.add(idAsString);
    }
    
    public void addOneTrackedRace(String idAsString) {
        super.addOneTrackedRace(idAsString);
        idsOfCreatedTrackedRaces.add(idAsString);
    }
    
    public boolean alreadyAddedLeaderboardWithName(String name) {
        return namesOfCreatedLeaderboards.contains(name);
    }
    
    public boolean alreadyAddedLeaderboardGroupWithName(String name) {
        return idsOfCreatedLeaderboardGroups.contains(name);
    }
    
    public boolean alreadyAddedEventWithId(String idAsString) {
        return idsOfCreatedEvents.contains(idAsString);
    }
    
    public boolean alreadyAddedRegattaWithId(String idAsString) {
        return namesOfCreatedRegattas.contains(idAsString);
    }
    
    public boolean alreadyAddedTrackedRaceWithId(String idAsString) {
        return idsOfCreatedTrackedRaces.contains(idAsString);
    }
}
