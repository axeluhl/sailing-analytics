package com.sap.sailing.resultimport;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ResultDocumentProvider {
    Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException, URISyntaxException;
}
