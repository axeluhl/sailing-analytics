package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;

public class SwissTimingArchiveConfigurationImpl implements SwissTimingArchiveConfiguration {
    private static final long serialVersionUID = 1L;
    private final String jsonUrl;
    
    public SwissTimingArchiveConfigurationImpl(String jsonUrl) {
        super();
        this.jsonUrl = jsonUrl;
    }

    @Override
    public String getJsonURL() {
        return jsonUrl;
    }
}
