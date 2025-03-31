package com.sap.sailing.ess40.resultimport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.ess40.resultimport.impl.ScoreCorrectionProviderImpl;

public class Activator implements BundleActivator {
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(ScoreCorrectionProvider.class, new ScoreCorrectionProviderImpl(), /* properties */null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
