package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.datamining.data.HasBackendPolarBoatClassContext;

public class BoatClassWithBackendPolarContext implements HasBackendPolarBoatClassContext {

    private BoatClass boatClass;
    private PolarDataService polarDataService;

    public BoatClassWithBackendPolarContext(BoatClass bc, PolarDataService polarDataService) {
        this.boatClass = bc;
        this.polarDataService = polarDataService;
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

}
