package com.sap.sailing.resultimport.impl;

import java.io.File;

public abstract class AbstractFileBasedScoreCorrectionProvider extends AbstractDocumentBasedScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    public AbstractFileBasedScoreCorrectionProvider(File scanDir) {
        super(new FileBasedResultDocumentProvider(scanDir));
        if (!scanDir.exists()) {
            scanDir.mkdirs();
        }
        if (!scanDir.isDirectory()) {
            throw new IllegalArgumentException("scanDir " + scanDir + " must be a directory");
        }
    }
}
