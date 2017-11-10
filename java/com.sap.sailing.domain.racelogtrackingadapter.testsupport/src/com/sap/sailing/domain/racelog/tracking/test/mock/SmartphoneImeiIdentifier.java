package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.DeviceIdentifier;


public class SmartphoneImeiIdentifier implements DeviceIdentifier {
    private static final long serialVersionUID = -1830014229310181702L;

    public static final String TYPE = "smartphoneImei";

    private final String imei;

    public SmartphoneImeiIdentifier(String imei) {
        this.imei = imei;
    }

    public String getImei() {
        return imei;
    }

    @Override
    public String getIdentifierType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "IMEI " + imei;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmartphoneImeiIdentifier) {
            return ((SmartphoneImeiIdentifier) obj).imei.equals(imei);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return imei.hashCode();
    }
    
    @Override
    public String getStringRepresentation() {
        return imei;
    }
}
