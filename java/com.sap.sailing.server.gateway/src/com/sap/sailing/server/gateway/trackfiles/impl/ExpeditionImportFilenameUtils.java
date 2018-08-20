package com.sap.sailing.server.gateway.trackfiles.impl;

import static com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler.supportedExpeditionArchiveFileExtensions;
import static com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler.supportedExpeditionLogFileExtensions;

class ExpeditionImportFilenameUtils {

    static String truncateFilenameExtentions(String filenameWithExtensions) {
        return removeTailIfExists(removeTailIfExists(filenameWithExtensions, supportedExpeditionArchiveFileExtensions),
                supportedExpeditionLogFileExtensions);
    }

    private static String removeTailIfExists(String source, Iterable<String> extensionsToRemove) {
        for (String toRemove : extensionsToRemove) {
            final String tailToRemove = "." + toRemove;
            if (source.toLowerCase().endsWith(tailToRemove)) {
                return source.substring(0, source.length() - tailToRemove.length());
            }
        }
        return source;
    }

}
