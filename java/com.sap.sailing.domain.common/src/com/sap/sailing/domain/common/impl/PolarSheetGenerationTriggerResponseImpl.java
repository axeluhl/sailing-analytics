package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.PolarSheetGenerationTriggerResponse;

public class PolarSheetGenerationTriggerResponseImpl implements PolarSheetGenerationTriggerResponse {

    private static final long serialVersionUID = -2160795576114448218L;
    private String id;
    private String boatClassName;

    // For GWT serialization
    PolarSheetGenerationTriggerResponseImpl() {
    };

    public PolarSheetGenerationTriggerResponseImpl(String id, String boatClassName) {
        this.id = id;
        this.boatClassName = boatClassName;
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
