package com.sap.sailing.kiworesultimport.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;

public class Activator implements BundleActivator {
    
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(ScoreCorrectionProvider.class, new ScoreCorrectionProviderImpl(), /* properties */ null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
