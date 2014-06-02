package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;

public class AlwaysFailingGPSFixImporter implements GPSFixImporter {

    @Override
    public void importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing, String sourceName)
            throws FormatNotSupportedException, IOException {
        while (inputStream.read() != -1) {
            
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
