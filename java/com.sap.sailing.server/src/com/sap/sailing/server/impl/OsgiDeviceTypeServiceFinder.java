package com.sap.sailing.server.impl;

import java.util.Collection;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.DeviceTypeServiceFinder;
import com.sap.sailing.domain.devices.NoCorrespondingDeviceMapperRegisteredException;

public class OsgiDeviceTypeServiceFinder implements DeviceTypeServiceFinder {
    private static final Logger logger = Logger.getLogger(OsgiDeviceTypeServiceFinder.class.getName());

    private final BundleContext context;

    public OsgiDeviceTypeServiceFinder(BundleContext context) {
        this.context = context;
    }

    @Override
    public <ServiceType> ServiceType findService(Class<ServiceType> clazz, String deviceType) {
        if (context == null) {
            return null;
        }

        ServiceType service = null;

        try {
            String filter = String.format("(%s=%s)", DeviceIdentifier.TYPE, deviceType);
            Collection<ServiceReference<ServiceType>> references = context.getServiceReferences(clazz, filter);

            // TODO we should probably cache this for performance reasons
            for (ServiceReference<ServiceType> reference : references) {
                service = context.getService(reference);
                if (service != null) {
                    break;
                }
            }
        } catch (InvalidSyntaxException e) {
            logger.severe("Invalid syntax for OSGi service filter (LDAP syntax)");
            e.printStackTrace();
        }

        if (service == null) throw new NoCorrespondingDeviceMapperRegisteredException(
                "Could not find handler for device identifier", deviceType);
        
        return service;
    }
}
