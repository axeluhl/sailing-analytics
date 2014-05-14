package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockSmartphoneImeiMongoServiceFinder implements TypeBasedServiceFinder<DeviceIdentifierMongoHandler> {
    private final SmartphoneImeiMongoHandler handler = new SmartphoneImeiMongoHandler();
    private DeviceIdentifierMongoHandler fallback;

    @Override
    public DeviceIdentifierMongoHandler findService(String deviceType) {
        if (deviceType.equals(SmartphoneImeiIdentifier.TYPE)) return handler;
        return fallback;
    }

    @Override
    public void setFallbackService(DeviceIdentifierMongoHandler fallback) {
        this.fallback = fallback;
    }
}
