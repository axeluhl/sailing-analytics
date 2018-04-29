package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.sensordata.ExpeditionExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.CompressedStreamsUtil;
import com.sap.sailing.server.trackfiles.impl.ExpeditionExtendedDataImporterImpl;
import com.sap.sailing.server.trackfiles.impl.ExpeditionImportFileHandler;
import com.sap.sse.common.TimePoint;

/**
 * From an Expedition log file extracts a {@link ExpeditionStartData} object that has all mark "ping" positions for the
 * two ends of the start line ("committee boat" and "pin end"), furthermore all time points identified as possible start
 * time points because the "time to gun" value went to zero.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ExpeditionCourseInferrer {
    private static final Logger logger = Logger.getLogger(ExpeditionCourseInferrer.class.getName());
    private static final String START_LINE_PORT_END_LAT = "Port lat";
    private static final String START_LINE_PORT_END_LON = "Port lon";
    private static final String START_LINE_STARBOARD_END_LAT = "Stbd lat";
    private static final String START_LINE_STARBOARD_END_LON = "Stbd lon";
    
    public ExpeditionStartData getStartData(InputStream inputStream, String filenameWithSuffix)
            throws IOException, FormatNotSupportedException {
        final List<TimePoint> startTimeCandidates = new ArrayList<>();
        final List<GPSFix> startLinePortEndFixes = new ArrayList<>();
        final List<GPSFix> startLineStarboardEndFixes = new ArrayList<>();
        CompressedStreamsUtil.handlePotentiallyCompressedFiles(filenameWithSuffix, inputStream, new ExpeditionImportFileHandler() {
            @Override
            protected void handleExpeditionFile(String fileName, InputStream inputStream) throws IOException, FormatNotSupportedException {
                logger.fine("Start parsing Expedition file");
                final AtomicLong lineNr = new AtomicLong();
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
                    String headerLine = buffer.readLine();
                    lineNr.incrementAndGet();
                    logger.fine("Validate and parse header columns");
                    final Map<String, Integer> colIndices = ExpeditionExtendedDataImporterImpl.parseHeader(headerLine);
                    ExpeditionExtendedDataImporterImpl.validateHeader(colIndices);
                    final Double lastTimeToGunValue[] = new Double[1];
                    buffer.lines().forEach(line -> {
                        lineNr.incrementAndGet();
                        if (!line.trim().isEmpty()) {
                            ExpeditionExtendedDataImporterImpl.parseLine(lineNr.get(), filenameWithSuffix, line, colIndices,
                                    (timePoint, lineContentTokens, columnsInFileFromHeader) -> {
                                        // look for a start time based on the time to gun turning negative
                                        final Double timeToGunValue = getColumnValue(lineContentTokens, columnsInFileFromHeader,
                                                ExpeditionExtendedSensorDataMetadata.EXPEDITION_TMTOGUN.getColumnName());
                                        if (lastTimeToGunValue[0] != null && lastTimeToGunValue[0] > 0 && timeToGunValue != null &&
                                                timeToGunValue <= 0) {
                                            // found a start candidate:
                                            startTimeCandidates.add(timePoint);
                                        }
                                        lastTimeToGunValue[0] = timeToGunValue;
                                        // look for start line position pings:
                                        final Double startLinePortEndLat = getColumnValue(lineContentTokens, columnsInFileFromHeader, START_LINE_PORT_END_LAT);
                                        final Double startLinePortEndLon = getColumnValue(lineContentTokens, columnsInFileFromHeader, START_LINE_PORT_END_LON);
                                        final Double startLineStarboardEndLat = getColumnValue(lineContentTokens, columnsInFileFromHeader, START_LINE_STARBOARD_END_LAT);
                                        final Double startLineStarboardEndLon = getColumnValue(lineContentTokens, columnsInFileFromHeader, START_LINE_STARBOARD_END_LON);
                                        addFixIfCoordinatesValid(startLinePortEndLat, startLinePortEndLon, timePoint, startLinePortEndFixes);
                                        addFixIfCoordinatesValid(startLineStarboardEndLat, startLineStarboardEndLon, timePoint, startLineStarboardEndFixes);
                                    });
                        }
                    });
                    buffer.close();
                }
            }
        });
        return new ExpeditionStartDataImpl(startLinePortEndFixes, startLineStarboardEndFixes, startTimeCandidates);
    }

    private void addFixIfCoordinatesValid(Double lat, Double lon, TimePoint timePoint, List<GPSFix> listToAddTo) {
        if (lat != null && lon != null) {
            listToAddTo.add(new GPSFixImpl(new DegreePosition(lat, lon), timePoint));
        }
    }
    
    private Double getColumnValue(String[] lineContentTokens, Map<String, Integer> columnsInFileFromHeader, String columnName) {
        return getColumnValue(lineContentTokens, columnsInFileFromHeader.get(columnName));
    }

    private Double getColumnValue(String[] lineContentTokens, final Integer columnIndex) {
        return columnIndex == null ? null :
            columnIndex >= lineContentTokens.length ? null
                : lineContentTokens[columnIndex].trim().isEmpty() ? null
                        : Double.parseDouble(lineContentTokens[columnIndex]);
    }
}
