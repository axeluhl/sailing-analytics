package com.sap.sailing.resultimport.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.resultimport.ResultDocumentDescriptor;
import com.sap.sailing.resultimport.ResultDocumentProvider;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class FileBasedResultDocumentProvider implements ResultDocumentProvider {
    private static final Logger logger = Logger.getLogger(FileBasedResultDocumentProvider.class.getName());
    
    private final File scanDir;
    
    public FileBasedResultDocumentProvider(File scanDir) {
        this.scanDir = scanDir;
    }

    @Override
    public Iterable<ResultDocumentDescriptor> getResultDocumentDescriptors() throws IOException {
        List<ResultDocumentDescriptor> result = new ArrayList<>();
        final File[] fileList = scanDir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.canRead() && file.isFile() && !file.getName().startsWith(".")) {
                    logger.fine("adding " + file + " to result import list");
                    result.add(new ResultDocumentDescriptorImpl(new FileInputStream(file), file.toString(),
                            new MillisecondsTimePoint(file.lastModified())));
                }
            }
        }
        return result;
    }
}
