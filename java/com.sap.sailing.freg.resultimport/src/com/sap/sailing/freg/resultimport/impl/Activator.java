package com.sap.sailing.freg.resultimport.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "freg.results";
    private static final String DEFAULT_SCAN_DIR = "fregresults";

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        String scanDirPath = context.getProperty(SCAN_DIR_PATH_PROPERTY_NAME);
        if (scanDirPath == null) {
            scanDirPath = System.getProperty(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
        }
        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl();
        context.registerService(ScoreCorrectionProvider.class, service, /* properties */null);
        logger.info("Scanning " + scanDirPath + " for official result lists of " + service.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
