package com.sap.sailing.winregatta.resultimport.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;

public class Activator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    @Override
    public void start(BundleContext context) throws Exception {

        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl();
        context.registerService(ScoreCorrectionProvider.class,
                service, /* properties */ null);
        logger.info("Scanning for official result lists of "+service.getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
