package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;

public class SmartphoneImeiJsonHandlerFactoryDefaultingToPlaceHolder extends
        DeviceIdentifierJsonHandlerFactoryDefaultingToPlaceHolder {

    public SmartphoneImeiJsonHandlerFactoryDefaultingToPlaceHolder() {
        super(new SmartphoneImeiJsonHandlerImpl(), SmartphoneImeiIdentifier.TYPE);
    }

}
