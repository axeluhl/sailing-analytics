package com.sap.sailing.sailwave.resultimport;

import java.io.File;

import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.impl.AbstractFileBasedScoreCorrectionProviderActivator;
import com.sap.sailing.resultimport.impl.FileBasedResultDocumentProvider;
import com.sap.sailing.xrr.resultimport.ParserFactory;
import com.sap.sailing.xrr.resultimport.impl.ScoreCorrectionProviderImpl;

public class Activator extends AbstractFileBasedScoreCorrectionProviderActivator {
    private static final String SCAN_DIR_PATH_PROPERTY_NAME = "sailwave.results";
    private static final String DEFAULT_SCAN_DIR = "sailwaveresults";
    
    public Activator() {
        super(SCAN_DIR_PATH_PROPERTY_NAME, DEFAULT_SCAN_DIR);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    @Override
    protected ScoreCorrectionProvider create(File scanDir) {
        return new ScoreCorrectionProviderImpl(new FileBasedResultDocumentProvider(scanDir), ParserFactory.INSTANCE);
    }
}
