package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.common.tracking.TrackingConnectorType;

public class TracTracConnectorType implements TrackingConnectorType {
    private static final long serialVersionUID = -8403681207142620931L;
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