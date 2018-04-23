package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;

public class AlwaysFailingGPSFixImporter implements GPSFixImporter {
    private final int failAfterAtMostSoManyBytes;
    
    /**
     * @param failAfterAtMostSoManyBytes -1 means consume up to EOF
     */
    public AlwaysFailingGPSFixImporter(int failAfterAtMostSoManyBytes) {
        this.failAfterAtMostSoManyBytes = failAfterAtMostSoManyBytes;
    }

    @Override
    public boolean importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing,
            String sourceName)
            throws FormatNotSupportedException, IOException {
        int read = 0;
        while (inputStream.read() != -1 && (failAfterAtMostSoManyBytes == -1 || read++ < failAfterAtMostSoManyBytes)) {
        }
        throw new FormatNotSupportedException();
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return null;
    }

    @Override
    public String getType() {
        return "AlwaysFailing";
    }

}
