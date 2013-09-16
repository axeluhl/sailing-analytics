package com.sap.sailing.resultimport;

import java.io.IOException;

public interface ResultDocumentProvider {
    Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException;
}
