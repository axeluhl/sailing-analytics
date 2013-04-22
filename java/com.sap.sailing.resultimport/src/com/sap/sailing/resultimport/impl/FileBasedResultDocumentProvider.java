package com.sap.sailing.resultimport.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.resultimport.ResultDocumentProvider;

public class FileBasedResultDocumentProvider implements ResultDocumentProvider {
    private static final Logger logger = Logger.getLogger(FileBasedResultDocumentProvider.class.getName());
    
    private final File scanDir;
    
    public FileBasedResultDocumentProvider(File scanDir) {
        this.scanDir = scanDir;
    }

    @Override
    public Iterable<Triple<InputStream, String, TimePoint>> getDocumentsAndNamesAndLastModified() throws FileNotFoundException {
        List<Triple<InputStream, String, TimePoint>> result = new ArrayList<>();
        final File[] fileList = scanDir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.canRead() && file.isFile() && !file.getName().startsWith(".")) {
                    logger.fine("adding " + file + " to result import list");
                    result.add(new Triple<InputStream, String, TimePoint>(new FileInputStream(file), file.toString(),
                            new MillisecondsTimePoint(file.lastModified())));
                }
            }
        }
        return result;
    }
}
