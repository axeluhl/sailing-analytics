package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;

import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface DataRetriever {
    byte[] getData(TrackFilesFormat format, TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;
}
