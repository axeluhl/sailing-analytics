package com.sap.sailing.xrr.resultimport;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface XRRDocumentProvider {
    Iterable<InputStream> getDocuments() throws FileNotFoundException;
}
