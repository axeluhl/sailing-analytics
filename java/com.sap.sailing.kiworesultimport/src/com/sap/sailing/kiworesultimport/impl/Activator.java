package com.sap.sailing.kiworesultimport.impl;

import java.io.File;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.AbstractFileBasedScoreCorrectionProviderActivator;

public class Activator extends AbstractFileBasedScoreCorrectionProviderActivator {
    
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "kiwo.results";
    private static final String DEFAULT_SCAN_DIR = "kiworesults";
    
    public Activator() {
        super(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
    }
    
    @Override
    protected ScoreCorrectionProvider create(File scanDir) {
        return new ScoreCorrectionProviderImpl(scanDir);
    }

}
