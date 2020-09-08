package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.common.tracking.TrackingConnectorType;

public class TracTracConnectorType implements TrackingConnectorType {
    public static final String NAME = "TracTrac";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String getDefaultUrl() {
        return "https://www.tractrac.com/";
    }
}