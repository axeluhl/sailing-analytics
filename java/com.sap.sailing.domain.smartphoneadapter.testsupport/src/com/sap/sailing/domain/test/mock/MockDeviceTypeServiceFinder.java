package com.sap.sailing.domain.test.mock;

import com.sap.sailing.domain.devices.DeviceTypeServiceFinder;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.smartphoneadapter.impl.SmartphoneImeiHandlerImpl;

/**
 * A simplified implementation of the {@link DeviceTypeServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockDeviceTypeServiceFinder implements DeviceTypeServiceFinder {
    private final SmartphoneImeiHandlerImpl handler = new SmartphoneImeiHandlerImpl();

    @SuppressWarnings("unchecked")
    @Override
    public <ServiceType> ServiceType findService(Class<ServiceType> clazz, String deviceType) {
        if (deviceType.equals(SmartphoneImeiIdentifier.TYPE)) {
            return (ServiceType) handler;
        }
        return null;
    }
}
