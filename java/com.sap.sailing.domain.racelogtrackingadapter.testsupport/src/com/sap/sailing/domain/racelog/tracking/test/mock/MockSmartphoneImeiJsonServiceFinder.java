package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockSmartphoneImeiJsonServiceFinder implements TypeBasedServiceFinder<DeviceIdentifierJsonHandler> {
    private final SmartphoneImeiJsonHandler handler = new SmartphoneImeiJsonHandler();
    private DeviceIdentifierJsonHandler fallback;

    @Override
    public DeviceIdentifierJsonHandler findService(String deviceType) {
        if (deviceType.equals(SmartphoneImeiIdentifier.TYPE)) return handler;
        return fallback;
    }

    @Override
    public void setFallbackService(DeviceIdentifierJsonHandler fallback) {
        this.fallback = fallback;
    }
}
