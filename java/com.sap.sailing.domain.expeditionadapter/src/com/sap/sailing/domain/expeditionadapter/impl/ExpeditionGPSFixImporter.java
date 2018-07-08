package com.sap.sailing.domain.expeditionadapter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.trackfiles.impl.CompressedStreamsUtil;
import com.sap.sailing.server.trackfiles.impl.ExpeditionExtendedDataImporterImpl;
import com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class ExpeditionGPSFixImporter implements GPSFixImporter {
    private static final String LAT_COLUMN_HEADING = ExpeditionExtendedDataImporterImpl.COL_NAME_LAT;
    private static final String LON_COLUMN_HEADING = ExpeditionExtendedDataImporterImpl.COL_NAME_LON;
    private static final String COG_COLUMN_HEADING = "Cog";
    private static final String SOG_COLUMN_HEADING = "Sog";

    @Override
    public boolean importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing,
            final String sourceName)
            throws FormatNotSupportedException, IOException {
        TrackFileImportDeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl(sourceName, getType() + "@" + new Date());
        final AtomicBoolean importedFixes = new AtomicBoolean(false);
        CompressedStreamsUtil.handlePotentiallyCompressedFiles(sourceName, inputStream,
                new ExpeditionImportFileHandler() {
                    @Override
                    protected void handleExpeditionFile(String fileName, InputStream stream) throws IOException {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                        final String headerLine = br.readLine();
                        final Map<String, Integer> columnDefinitions = ExpeditionExtendedDataImporterImpl
                                .parseHeader(headerLine);
                        final AtomicInteger lineNr = new AtomicInteger(0);
                        br.lines().forEach(line -> {
                            if (!line.trim().isEmpty()) {
                                ExpeditionExtendedDataImporterImpl.parseLine(lineNr.incrementAndGet(), fileName, line,
                                        columnDefinitions, (timePoint, columnValues, columns) -> {
                                            final double latDeg = Double
                                                    .parseDouble(columnValues[columns.get(LAT_COLUMN_HEADING)]);
                                            final double lonDeg = Double
                                                    .parseDouble(columnValues[columns.get(LON_COLUMN_HEADING)]);
                                            final double cogDeg = Double
                                                    .parseDouble(columnValues[columns.get(COG_COLUMN_HEADING)]);
                                            final double sogKnots = Double
                                                    .parseDouble(columnValues[columns.get(SOG_COLUMN_HEADING)]);
                                            final GPSFixMoving fix = new GPSFixMovingImpl(
                                                    new DegreePosition(latDeg, lonDeg), timePoint,
                                                    new KnotSpeedWithBearingImpl(sogKnots,
                                                            new DegreeBearingImpl(cogDeg)));
                                            callback.addFix(fix, device);
                                            importedFixes.set(true);
                                        });
                            }
                        });
                    }
                });
        return importedFixes.get();
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return ExpeditionImportFileHandler.supportedExpeditionLogFileExtensions;
    }

    @Override
    public String getType() {
        return GPSFixImporter.EXPEDITION_TYPE;
    }
}
