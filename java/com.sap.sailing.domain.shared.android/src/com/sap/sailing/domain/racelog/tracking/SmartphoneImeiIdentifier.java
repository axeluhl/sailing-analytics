package com.sap.sailing.domain.racelog.tracking;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.SharedDomainFactory;

public class SmartphoneImeiIdentifier implements DeviceIdentifier {
    private static final long serialVersionUID = -1830014229310181702L;

    public static final String TYPE = "smartphoneImei";

    private static final Map<String, SmartphoneImeiIdentifier> cache = new HashMap<String, SmartphoneImeiIdentifier>();

    private final String imei;

    public SmartphoneImeiIdentifier(String imei) {
        this.imei = imei;
    }

    @Override
    public DeviceIdentifier resolve(SharedDomainFactory domainFactory) {
        // ignore the domainFactory

        DeviceIdentifier result = cache.get(imei);
        if (result == null) {
            cache.put(imei, this);
            return this;
        }
        return result;
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
}
