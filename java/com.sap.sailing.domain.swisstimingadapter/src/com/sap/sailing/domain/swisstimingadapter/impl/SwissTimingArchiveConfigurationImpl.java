package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;

public class SwissTimingArchiveConfigurationImpl implements SwissTimingArchiveConfiguration {
    private static final long serialVersionUID = 1L;
    private final String jsonUrl;
    private final String creatorName;
    
    public SwissTimingArchiveConfigurationImpl(String jsonUrl, String creatorName) {
        super();
        this.jsonUrl = jsonUrl;
        this.creatorName = creatorName;
    }

    @Override
    public String getJsonURL() {
        return jsonUrl;
    }

    @Override
    public String getCreatorName() {
        return creatorName;
    }
}
