package com.sap.sailing.domain.racelog.impl;

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

}
