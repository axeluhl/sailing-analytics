package com.sap.sailing.xrr.resultimport.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.XRRDocumentProvider;

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
        final ScoreCorrectionProviderImpl service = new ScoreCorrectionProviderImpl(new XRRDocumentProvider() {
            @Override
            public Iterable<InputStream> getDocuments() throws FileNotFoundException {
                List<InputStream> result = new ArrayList<InputStream>();
                for (File file : new File(scanDirPath).listFiles()) {
                    result.add(new FileInputStream(file));
                }
                return result;
            }
        }, ParserFactory.INSTANCE);
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
