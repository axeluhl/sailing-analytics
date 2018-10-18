package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;

public class SwissTimingArchiveConfigurationImpl implements SwissTimingArchiveConfiguration {
    private static final long serialVersionUID = 3576476452171509082L;
    
    private final String jsonUrl;
    
    public SwissTimingArchiveConfigurationImpl(String jsonUrl) {
        super();
        this.jsonUrl = jsonUrl;
    }

    @Override
    public String getJsonUrl() {
        return jsonUrl;
    }
}
