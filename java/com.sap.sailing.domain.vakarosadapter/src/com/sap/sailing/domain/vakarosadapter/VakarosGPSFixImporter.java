package com.sap.sailing.domain.vakarosadapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.sensordata.ExpeditionExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.server.trackfiles.impl.CompressedStreamsUtil;
import com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class VakarosGPSFixImporter implements GPSFixImporter {

    private static final Logger logger = Logger.getLogger(VakarosGPSFixImporter.class.getName());

    static final String TIMESTAMP_COLUMN_HEADING = "timestamp";
    private static final String LAT_COLUMN_HEADING = "latitude";
    private static final String LON_COLUMN_HEADING = "longitude";
    private static final String SOG_COLUMN_HEADING = "sog_kts";
    private static final String COG_COLUMN_HEADING = "cog";
    private static final String HDG_COLUMN_HEADING = "hdg_true";
    private static final String HEEL_COLUMN_HEADING = "roll";
    private static final String PITCH_COLUMN_HEADING = "pitch";
    private static final String LOAD_GDF1_COLUMN_HEADING = "load_gdf1";
    private static final String LOAD_GDF2_COLUMN_HEADING = "load_gdf2";
    
    private static final int MAX_EXPEDITION_COLUMN_INDEX = Collections.max(Arrays.asList(
            ExpeditionExtendedSensorDataMetadata.HEEL.getColumnIndex(),
            ExpeditionExtendedSensorDataMetadata.TRIM.getColumnIndex(),
            ExpeditionExtendedSensorDataMetadata.FORESTAY_LOAD.getColumnIndex(),
            ExpeditionExtendedSensorDataMetadata.EXPEDITION_KICKER_TENSION.getColumnIndex()))+1;

    private static final String VAKAROS_TYPE = "Vakaros";

    @Override
    public boolean importFixes(InputStream inputStream, Charset charset, Callback callback,
            boolean inferSpeedAndBearing, final String sourceName)
            throws FormatNotSupportedException, IOException {
        final TrackFileImportDeviceIdentifier gpsDevice = new TrackFileImportDeviceIdentifierImpl(sourceName, getType() + "@" + new Date());
        final TrackFileImportDeviceIdentifier sensorDevice = new TrackFileImportDeviceIdentifierImpl(sourceName+"-SENSORS", getType() + "@" + new Date());
        final AtomicBoolean importedFixes = new AtomicBoolean(false);
        CompressedStreamsUtil.handlePotentiallyCompressedFiles(sourceName, inputStream,
                charset, new ExpeditionImportFileHandler() {
                    @Override
                    protected void handleExpeditionFile(String fileName, InputStream stream, Charset charset) throws IOException {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(stream, charset));
                        final String headerLine = br.readLine();
                        final VakarosExtendedDataImporterImpl importer = new VakarosExtendedDataImporterImpl();
                        final Map<String, Integer> columnDefinitions = importer.parseHeader(headerLine);
                        final AtomicInteger lineNr = new AtomicInteger(0);
                        br.lines().forEach(line -> {
                            if (!line.trim().isEmpty()) {
                                importer.parseLine(lineNr.incrementAndGet(), fileName, line,
                                        columnDefinitions, (timePoint, columnValues, columns) -> {
                                            final double latDeg = Double
                                                    .parseDouble(columnValues[columns.get(LAT_COLUMN_HEADING)]);
                                            final double lonDeg = Double
                                                    .parseDouble(columnValues[columns.get(LON_COLUMN_HEADING)]);
                                            final double cogDeg = Double
                                                    .parseDouble(columnValues[columns.get(COG_COLUMN_HEADING)]);
                                            final double sogKnots = Double
                                                    .parseDouble(columnValues[columns.get(SOG_COLUMN_HEADING)]);
                                            final DegreePosition position = new DegreePosition(latDeg, lonDeg);
                                            Bearing optionalTrueHeading;
                                            if (columns.containsKey(HDG_COLUMN_HEADING)) {
                                                try {
                                                    optionalTrueHeading = new DegreeBearingImpl(Double.parseDouble(columnValues[columns.get(HDG_COLUMN_HEADING)]));
                                                } catch (NumberFormatException e) {
                                                    logger.log(Level.WARNING, "Problem obtaining declination for Expedition fix heading", e);
                                                    optionalTrueHeading = null;
                                                }
                                            } else {
                                                optionalTrueHeading = null;
                                            }
                                            final GPSFixMoving fix = new GPSFixMovingImpl(
                                                    position, timePoint,
                                                    new KnotSpeedWithBearingImpl(sogKnots,
                                                            new DegreeBearingImpl(cogDeg)), optionalTrueHeading);
                                            callback.addFix(fix, gpsDevice);
                                            final Double[] fixData = new Double[MAX_EXPEDITION_COLUMN_INDEX];
                                            fixData[ExpeditionExtendedSensorDataMetadata.HEEL.getColumnIndex()] = parseOptionalValue(HEEL_COLUMN_HEADING, columnValues, columns);
                                            fixData[ExpeditionExtendedSensorDataMetadata.TRIM.getColumnIndex()] = parseOptionalValue(PITCH_COLUMN_HEADING, columnValues, columns);
                                            fixData[ExpeditionExtendedSensorDataMetadata.FORESTAY_LOAD.getColumnIndex()] = parseOptionalValue(LOAD_GDF1_COLUMN_HEADING, columnValues, columns);
                                            fixData[ExpeditionExtendedSensorDataMetadata.EXPEDITION_KICKER_TENSION.getColumnIndex()] = parseOptionalValue(LOAD_GDF2_COLUMN_HEADING, columnValues, columns);
                                            final DoubleVectorFix sensorFix = new DoubleVectorFixImpl(timePoint, fixData);
                                            callback.addSensorFixes(Collections.singleton(sensorFix), sensorDevice);
                                            importedFixes.set(true);
                                        });
                            }
                        });
                    }
                });
        return importedFixes.get();
    }
    
    private Double parseOptionalValue(final String columnName, String[] columns, Map<String, Integer> header) {
        final Integer columnIndex = header.get(columnName);
        final Double result;
        if (columnIndex != null && columns.length > columnIndex) {
            final String columnContent = columns[columnIndex];
            result = Util.hasLength(columnContent) ? Double.parseDouble(columnContent) : null;
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        return ExpeditionImportFileHandler.supportedExpeditionLogFileExtensions;
    }

    @Override
    public String getType() {
        return VAKAROS_TYPE;
    }
}
