package com.sap.sailing.domain.racelog.tracking.test.mock;

import com.sap.sailing.domain.common.racelog.tracking.TypeBasedServiceFinder;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.SmartphoneImeiJsonHandlerImpl;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockSmartphoneImeiJsonServiceFinder implements TypeBasedServiceFinder<DeviceIdentifierJsonHandler> {
    private final SmartphoneImeiJsonHandlerImpl handler = new SmartphoneImeiJsonHandlerImpl();

    @Override
    public DeviceIdentifierJsonHandler findService(String deviceType) {
        return handler;
    }
}
