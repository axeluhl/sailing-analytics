package com.sap.sailing.resultimport.impl;

import java.io.InputStream;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;

/**
 * Uses a {@link ResultDocumentProvider} that can enumerate result documents in the form of
 * {@link ResultDocumentDescriptor}s which, in turn, can {@link ResultDocumentDescriptor#getInputStream() provide}
 * an {@link InputStream} for the document.
 */
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
