package com.sap.sailing.server.trackfiles;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import com.sap.sailing.domain.common.trackfiles.TrackFilesDataSource;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.ExportImpl;

/**
 * Deals with exporting tracked races into a standard file format.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface Export {
    Export INSTANCE = new ExportImpl();

    byte[] writeCompetitors(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;

    byte[] writeWind(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;

    byte[] writeBuoys(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;

    byte[] writeManeuvers(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException;

    void writeRaces(List<TrackFilesDataSource> data, TrackFilesFormat format, List<TrackedRace> races,
            boolean dataBeforeAfter, boolean rawFixes, ZipOutputStream out) throws FormatNotSupportedException;
}
