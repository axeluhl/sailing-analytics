package com.sap.sailing.domain.test.mock;

import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.domain.smartphoneadapter.impl.SmartphoneImeiHandlerImpl;

/**
 * A simplified implementation of the {@link TypeBasedServiceFinder} interface that, when the device type
 * {@link SmartphoneImeiIdentifier#TYPE} is requested, returns a specific handler that was passed to this object's
 * constructor.
 * 
 * @author Fredrik Teschke
 *
 */
public class MockDeviceTypeServiceFinder<ServiceT> implements TypeBasedServiceFinder<ServiceT> {
    private final SmartphoneImeiHandlerImpl handler = new SmartphoneImeiHandlerImpl();

    @SuppressWarnings("unchecked")
    @Override
    public ServiceT findService(String deviceType) {
        if (deviceType.equals(SmartphoneImeiIdentifier.TYPE)) {
            return (ServiceT) handler;
        }
        return null;
    }
}
