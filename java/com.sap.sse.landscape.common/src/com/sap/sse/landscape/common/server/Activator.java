package com.sap.sse.landscape.common.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.landscape.common.shared.SecuredLandscapeTypes;
import com.sap.sse.security.shared.HasPermissionsProvider;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(HasPermissionsProvider.class, SecuredLandscapeTypes::getAllInstances, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
