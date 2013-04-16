package com.sap.sailing.xrr.resultimport.impl;

import java.io.File;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.FileBasedResultDocumentProvider;
import com.sap.sailing.xrr.resultimport.ParserFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "xrr.results";
    private static final String DEFAULT_SCAN_DIR = "xrrresults";
    
    @Override
    public void start(BundleContext context) throws Exception {
        final String scanDirPath;
        if (context.getProperty(SCAN_DIR_PATH_PROPERTY_NAME) == null) {
            scanDirPath = System.getProperty(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
        } else {
            scanDirPath = context.getProperty(SCAN_DIR_PATH_PROPERTY_NAME);
        }
        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(new FileBasedResultDocumentProvider(
                new File(scanDirPath)),
                ParserFactory.INSTANCE);
        context.registerService(ScoreCorrectionProvider.class,
                service, /* properties */ null);
        logger.info("Scanning "+scanDirPath+" for official XRR result lists of "+service.getName());
        context.registerService(ScoreCorrectionProvider.class,
                service, /* properties */ null);
        logger.info("Scanning "+scanDirPath+" for official result lists of "+service.getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
