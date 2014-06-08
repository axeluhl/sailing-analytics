package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.racelog.RaceLogStore;

public class MasterDataImportInformation {

    private final RaceLogStore raceLogStore;

    public MasterDataImportInformation(RaceLogStore raceLogStore) {
        this.raceLogStore = raceLogStore;
    }

    public RaceLogStore getRaceLogStore() {
        return raceLogStore;
    }

}
