package com.sap.sailing.resultimport.impl;

import java.io.File;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;

public abstract class AbstractFileBasedScoreCorrectionProviderActivator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(AbstractFileBasedScoreCorrectionProviderActivator.class.getName());
    
    private final String scanDirPathPropertyName;
    private final String defaultScanDir;
    
    protected abstract ScoreCorrectionProvider create(File scanDir);
    
    public AbstractFileBasedScoreCorrectionProviderActivator(String scanDirPathPropertyName, String defaultScanDir) {
        super();
        this.scanDirPathPropertyName = scanDirPathPropertyName;
        this.defaultScanDir = defaultScanDir;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        String scanDirPath = context.getProperty(scanDirPathPropertyName);
        if (scanDirPath == null) {
            scanDirPath = System.getProperty(scanDirPathPropertyName, defaultScanDir);
        }
        final ScoreCorrectionProvider service = create(new File(scanDirPath));
        context.registerService(ScoreCorrectionProvider.class, service, /* properties */ null);
        logger.info("Scanning "+scanDirPath+" for official result lists of "+service.getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
