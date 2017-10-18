package com.sap.sailing.domain.expeditionadapter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;

public class ExpeditionGPSFixImporter implements GPSFixImporter {

    @Override
    public void importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing, String sourceName)
            throws FormatNotSupportedException, IOException {
        // TODO the following device identifier will be required for invoking the callback.addFix(...) method
//        TrackFileImportDeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl(sourceName, getType() + "@" + new Date());
        // TODO parse the input stream using the expeditionAdapter, producing GPSFixMoving objects to pass to the callback
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return Arrays.asList(new String[] { "csv", "log", "txt" });
    }

    @Override
    public String getType() {
        return "Expedition";
    }

}
