package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogInformation;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class RaceLogInformationImpl implements RaceLogInformation {

    private final RaceLogStore store;
    private final RaceLogIdentifierTemplate identifierTemplate;
    
    public RaceLogInformationImpl(RaceLogStore store, RaceLogIdentifierTemplate identifierTemplate) {
        this.store = store;
        this.identifierTemplate = identifierTemplate;
    }

    @Override
    public RaceLogStore getStore() {
        return store;
    }

    @Override
    public RaceLogIdentifierTemplate getIdentifierTemplate() {
        return identifierTemplate;
    }

    @Override
    public RaceLog getRaceLog(RaceColumn raceColumn, Fleet fleet) {
        return store.getRaceLog(identifierTemplate.compileRaceLogIdentifier(fleet), false);
    }

}
