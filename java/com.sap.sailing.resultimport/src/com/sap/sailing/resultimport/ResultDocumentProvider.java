package com.sap.sailing.resultimport;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.sap.sailing.domain.common.impl.Util.Pair;

public interface ResultDocumentProvider {
    Iterable<Pair<InputStream, String>> getDocumentsAndNames() throws FileNotFoundException;
}
