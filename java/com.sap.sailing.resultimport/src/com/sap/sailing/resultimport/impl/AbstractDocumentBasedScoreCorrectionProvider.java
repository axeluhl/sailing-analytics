package com.sap.sailing.resultimport.impl;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultDocumentProvider;

public abstract class AbstractDocumentBasedScoreCorrectionProvider implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -207867446958976110L;
    private final ResultDocumentProvider resultDocumentProvider;

    public AbstractDocumentBasedScoreCorrectionProvider(ResultDocumentProvider resultDocumentProvider) {
        this.resultDocumentProvider = resultDocumentProvider;
    }

    protected ResultDocumentProvider getResultDocumentProvider() {
        return resultDocumentProvider;
    }
}
