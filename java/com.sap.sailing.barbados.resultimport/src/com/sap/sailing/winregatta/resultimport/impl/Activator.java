package com.sap.sailing.winregatta.resultimport.impl;

import java.io.File;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.AbstractFileBasedScoreCorrectionProviderActivator;
import com.sap.sailing.resultimport.impl.FileBasedResultDocumentProvider;

public class Activator extends AbstractFileBasedScoreCorrectionProviderActivator {
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "barbados.results";
    private static final String DEFAULT_SCAN_DIR = "barbados";

    public Activator() {
        super(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
    }

    @Override
    protected ScoreCorrectionProvider create(File scanDir) {
        return new ScoreCorrectionProviderImpl(new FileBasedResultDocumentProvider(scanDir));
    }
}
