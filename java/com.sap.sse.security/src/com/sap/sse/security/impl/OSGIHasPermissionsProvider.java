package com.sap.sse.security.impl;

import java.util.HashSet;
import java.util.Set;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissionsProvider;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class OSGIHasPermissionsProvider implements HasPermissionsProvider {
    
    private final ServiceTracker<HasPermissionsProvider, HasPermissionsProvider> hasPermissionsProviderServiceTracker;

    public OSGIHasPermissionsProvider(ServiceTracker<HasPermissionsProvider, HasPermissionsProvider> hasPermissionsProviderServiceTracker) {
        this.hasPermissionsProviderServiceTracker = hasPermissionsProviderServiceTracker;
    }

    @Override
    public Iterable<HasPermissions> getAllHasPermissions() {
        Set<HasPermissions> result = new HashSet<>();
        Util.addAll(SecuredSecurityTypes.getAllInstances(), result);
        for (HasPermissionsProvider provider : hasPermissionsProviderServiceTracker.getServices(new HasPermissionsProvider[1])) {
            Util.addAll(provider.getAllHasPermissions(), result);
        }
        return result;
    }

}
