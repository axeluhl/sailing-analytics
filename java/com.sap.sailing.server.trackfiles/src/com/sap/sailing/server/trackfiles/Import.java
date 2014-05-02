package com.sap.sailing.server.trackfiles;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.server.trackfiles.impl.ImportImpl;

public interface Import {
    Import INSTANCE = new ImportImpl();
    
    interface FixCallback {
        void addFix(GPSFix fix);
        void addFix(GPSFix fix, String trackName);
    }
    
    /**
     * Retrieves the fixes from the {@code inputStream}, and calls the
     * {@code callback} with every new fix.
     * @param inferSpeedAndBearing Should speed and bearing be inferred by looking
     * at the previous fix, if that data is not directly present within the file?
     */
    void importFixes(InputStream inputStream, FixCallback callback, boolean inferSpeedAndBearing) throws IOException;
}
