package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;

public abstract class ExpeditionImportFileHandler implements CompressedStreamsUtil.FileHandler {

    public static final List<String> supportedExpeditionLogFileExtensions = Arrays.asList("csv", "log", "txt");
    public static final List<String> supportedExpeditionArchiveFileExtensions = Arrays.asList("gz", "zip");
    public static final List<String> supportedExpeditionFileExtensions;

    static {
        final List<String> allExtensions = new ArrayList<>(supportedExpeditionLogFileExtensions);
        allExtensions.addAll(supportedExpeditionArchiveFileExtensions);
        supportedExpeditionFileExtensions = Collections.unmodifiableList(allExtensions);
    }

    @Override
    public final void handle(String fileName, InputStream inputStream) throws IOException, FormatNotSupportedException {
        final String lowerCaseFileName = fileName.toLowerCase();
        boolean extensionSupported = false;
        for (String extension : supportedExpeditionLogFileExtensions) {
            if (lowerCaseFileName.endsWith(extension)) {
                extensionSupported = true;
                break;
            }
        }
        if (extensionSupported) {
            handleExpeditionFile(lowerCaseFileName, inputStream);
        }
    }

    protected abstract void handleExpeditionFile(String fileName, InputStream inputStream) throws IOException, FormatNotSupportedException;
}
