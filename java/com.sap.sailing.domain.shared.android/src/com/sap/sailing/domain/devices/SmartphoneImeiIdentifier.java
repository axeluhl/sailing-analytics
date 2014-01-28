package com.sap.sailing.domain.devices;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.SharedDomainFactory;

public class SmartphoneImeiIdentifier implements DeviceIdentifier {
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

}
