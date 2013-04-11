package com.sap.sailing.odf.resultimport.impl;

import java.io.InputStream;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.odf.resultimport.CumulativeResultDocumentProvider;
import com.sap.sailing.odf.resultimport.ParserFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "kiwo.results";
    private static final String DEFAULT_SCAN_DIR = "kiworesults";
    
    @Override
    public void start(BundleContext context) throws Exception {
        String scanDirPath = context.getProperty(SCAN_DIR_PATH_PROPERTY_NAME);
        if (scanDirPath == null) {
            scanDirPath = System.getProperty(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
        }
        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(new CumulativeResultDocumentProvider() {
            @Override
            public Iterable<InputStream> getAllAvailableCumulativeResultDocuments() {
                // TODO Auto-generated method stub
                return null;
            }
        }, ParserFactory.INSTANCE);
        context.registerService(ScoreCorrectionProvider.class,
                service, /* properties */ null);
        logger.info("Scanning "+scanDirPath+" for official result lists of "+service.getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
