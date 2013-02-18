package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceLogInformation;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;


public class RaceLogInformationImpl implements RaceLogInformation {
    
    public static RaceLogInformation create(Named parent, RaceLogStore store) {
        return new RaceLogInformationImpl(
                store, 
                new RaceLogStoreIdentifierTemplateImpl(parent));
    }

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
