package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetsData;

public class PolarSheetGenerationResponseImpl implements PolarSheetGenerationResponse {

    private static final long serialVersionUID = -2160795576114448218L;
    private String id;
    private String boatClassName;
    private PolarSheetsData data;

    // For GWT serialization
    PolarSheetGenerationResponseImpl() {
    };

    public PolarSheetGenerationResponseImpl(String id, String boatClassName, PolarSheetsData data) {
        this.id = id;
        this.boatClassName = boatClassName;
        this.data = data;
    }

    @Override
    public PolarSheetsData getData() {
        return data;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getBoatClassName() {
        return boatClassName;
    }

}
