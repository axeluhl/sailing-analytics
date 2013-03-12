package com.sap.sailing.odf.resultimport;

import java.io.InputStream;

public interface CumulativeResultDocumentProvider {
    Iterable<InputStream> getAllAvailableCumulativeResultDocuments();
}
