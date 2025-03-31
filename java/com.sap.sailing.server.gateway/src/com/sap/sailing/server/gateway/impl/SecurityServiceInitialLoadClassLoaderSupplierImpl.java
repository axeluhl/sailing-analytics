package com.sap.sailing.server.gateway.impl;

import com.sap.sse.security.SecurityServiceInitialLoadClassLoaderSupplier;

public class SecurityServiceInitialLoadClassLoaderSupplierImpl implements SecurityServiceInitialLoadClassLoaderSupplier {
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
}
