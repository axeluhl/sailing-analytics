package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;

import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;

public interface DataRetriever {
    byte[] getData(TrackFilesFormat format, TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;
}
