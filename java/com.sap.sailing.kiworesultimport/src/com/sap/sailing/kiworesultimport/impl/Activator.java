package com.sap.sailing.kiworesultimport.impl;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;

public class Activator implements BundleActivator {
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "kiwo.results";
    private static final String DEFAULT_SCAN_DIR = "kiworesults";
    
    @Override
    public void start(BundleContext context) throws Exception {
        String scanDirPath = context.getProperty(SCAN_DIR_PATH_PROPERTY_NAME);
        if (scanDirPath == null) {
            scanDirPath = System.getProperty(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
        }
        context.registerService(ScoreCorrectionProvider.class,
                new ScoreCorrectionProviderImpl(new File(scanDirPath)), /* properties */ null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
