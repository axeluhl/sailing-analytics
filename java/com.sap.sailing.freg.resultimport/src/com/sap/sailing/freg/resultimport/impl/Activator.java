package com.sap.sailing.freg.resultimport.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.resultimport.impl.ResultUrlRegistryServiceTrackerCustomizer;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "freg.results";
    private static final String DEFAULT_SCAN_DIR = "fregresults";

    private ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    /*
     * For testing, consider using the following URLs:
     *   allUrls.add(new URL("http://www.axel-uhl.de/freg/freg_html_export_sample.html"));
     *   allUrls.add(new URL("http://www.axel-uhl.de/freg/eurocup_29er_29e.htm"));
     */
    public void start(BundleContext bundleContext) throws Exception {
        String scanDirPath = bundleContext.getProperty(SCAN_DIR_PATH_PROPERTY_NAME);
        if (scanDirPath == null) {
            scanDirPath = System.getProperty(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
        }
        logger.info("Scanning " + scanDirPath + " for official result lists of "
                + ScoreCorrectionProviderImpl.PROVIDER_NAME);

        resultUrlRegistryServiceTracker = new ServiceTracker<>(bundleContext, ResultUrlRegistry.class,
                new ResultUrlRegistryServiceTrackerCustomizer(bundleContext) {

                    @Override
                    protected ScoreCorrectionProvider configureScoreCorrectionProvider(
                            ResultUrlRegistry resultUrlRegistry) {
                        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(resultUrlRegistry);
                        return service;
                    }

                });

        resultUrlRegistryServiceTracker.open();

    }

    public void stop(BundleContext bundleContext) throws Exception {
        resultUrlRegistryServiceTracker.close();
    }
}
