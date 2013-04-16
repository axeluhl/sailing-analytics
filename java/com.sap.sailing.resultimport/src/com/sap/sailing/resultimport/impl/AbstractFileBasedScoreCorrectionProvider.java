package com.sap.sailing.resultimport.impl;

import java.io.File;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultDocumentProvider;

public abstract class AbstractFileBasedScoreCorrectionProvider implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private final ResultDocumentProvider resultDocumentProvider;

    public AbstractFileBasedScoreCorrectionProvider(File scanDir) {
        super();
        if (!scanDir.exists()) {
            scanDir.mkdirs();
        }
        if (!scanDir.isDirectory()) {
            throw new IllegalArgumentException("scanDir " + scanDir + " must be a directory");
        }
        this.resultDocumentProvider = new FileBasedResultDocumentProvider(scanDir);
    }

    public AbstractFileBasedScoreCorrectionProvider(ResultDocumentProvider resultDocumentProvider) {
        this.resultDocumentProvider = resultDocumentProvider;
    }

    protected ResultDocumentProvider getResultDocumentProvider() {
        return resultDocumentProvider;
    }
}
