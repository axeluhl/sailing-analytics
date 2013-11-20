package com.sap.sailing.domain.igtimiadapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(IgtimiConnectionFactory.class, IgtimiConnectionFactory.INSTANCE, /* properties */ null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
