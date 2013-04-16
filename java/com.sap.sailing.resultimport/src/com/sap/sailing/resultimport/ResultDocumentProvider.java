package com.sap.sailing.resultimport;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;

public interface ResultDocumentProvider {
    Iterable<Triple<InputStream, String, TimePoint>> getDocumentsAndNamesAndLastModified() throws IOException;
}
