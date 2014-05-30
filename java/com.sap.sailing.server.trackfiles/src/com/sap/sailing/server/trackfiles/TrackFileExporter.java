package com.sap.sailing.server.trackfiles;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import com.sap.sailing.domain.common.trackfiles.TrackFilesDataSource;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.trackfiles.impl.TrackFileExporterImpl;

/**
 * Deals with exporting tracked races into a standard file format.
 * 
 * @author Fredrik Teschke
 * 
 */
public interface TrackFileExporter {
    TrackFileExporter INSTANCE = new TrackFileExporterImpl();

    void writeAllData(List<TrackFilesDataSource> data, TrackFilesFormat format, List<TrackedRace> races,
            boolean dataBeforeAfter, boolean rawFixes, ZipOutputStream out) throws IOException;
}
