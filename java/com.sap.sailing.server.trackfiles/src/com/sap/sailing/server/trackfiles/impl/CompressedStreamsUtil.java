package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;

public final class CompressedStreamsUtil {

    private static final Logger logger = Logger.getLogger(CompressedStreamsUtil.class.getName());
    private static final String GZIP_SUFFIX = ".gz";
    
    public interface FileHandler {
        void handle(String fileName, InputStream inputStream) throws IOException, FormatNotSupportedException;
    }

    private CompressedStreamsUtil() {
    }

    public static void handlePotentiallyCompressedFiles(final String filename, final InputStream inputStream,
            FileHandler fileHandler) throws IOException, FormatNotSupportedException {
        try (InputStream stream = inputStream) {
            if (filename.toLowerCase().endsWith(".zip")) {
                logger.info("Bravo file " + filename + " is a ZIP file");
                final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    fileHandler.handle(entry.getName(), zipInputStream);
                }
            } else if (filename.toLowerCase().endsWith(GZIP_SUFFIX)) {
                final GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                final String actualFileName = filename.substring(0, filename.length() - GZIP_SUFFIX.length());
                fileHandler.handle(actualFileName, gzipInputStream);
            } else {
                fileHandler.handle(filename, inputStream);
            }
        }
    }

}
