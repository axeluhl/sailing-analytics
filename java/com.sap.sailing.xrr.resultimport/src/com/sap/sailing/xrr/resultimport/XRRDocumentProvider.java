package com.sap.sailing.xrr.resultimport;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.sap.sailing.domain.common.impl.Util.Pair;

public interface XRRDocumentProvider {
    Iterable<Pair<InputStream, String>> getDocumentsAndNames() throws FileNotFoundException;
}
