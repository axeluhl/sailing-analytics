package com.sap.sailing.domain.trackimport;

import java.io.IOException;
import java.io.InputStream;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;

public interface DoubleVectorFixImporter {    

    interface Callback {
        void addFix(DoubleVectorFix fix, TrackFileImportDeviceIdentifier device);
    }

    void importFixes(InputStream inputStream, Callback callback, String sourceName)
            throws FormatNotSupportedException, IOException;

    String getType();
    
}
