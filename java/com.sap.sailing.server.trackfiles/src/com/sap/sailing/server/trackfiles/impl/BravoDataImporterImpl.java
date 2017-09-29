package com.sap.sailing.server.trackfiles.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.doublefix.DoubleFixProcessor;
import com.sap.sailing.server.trackfiles.impl.doublefix.DoubleVectorFixData;
import com.sap.sailing.server.trackfiles.impl.doublefix.DownsamplerTo1HzProcessor;
import com.sap.sailing.server.trackfiles.impl.doublefix.LearningBatchProcessor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * TODO: access to columns enum actually by public static enum. Col definition should belong to instance, so we can have
 * different col definitions.
 */
public class BravoDataImporterImpl implements DoubleVectorFixImporter {
    private final Logger LOG = Logger.getLogger(DoubleVectorFixImporter.class.getName());
    private final String BOF = "jjlDATE\tjjlTIME\tEpoch";
    private int trackColumnCount = BravoSensorDataMetadata.getTrackColumnCount();

    public void importFixes(InputStream inputStream, Callback callback, final String filename, String sourceName)
            throws FormatNotSupportedException, IOException {
        final TrackFileImportDeviceIdentifier trackIdentifier = new TrackFileImportDeviceIdentifierImpl(
                UUID.randomUUID(), filename, sourceName, MillisecondsTimePoint.now());
        try {
            LOG.fine("Import CSV from " + filename);
            final InputStreamReader isr;
            if (sourceName.endsWith("gz")) {
                LOG.fine("Using gzip stream reader " + filename);
                isr = new InputStreamReader(new GZIPInputStream(inputStream));
            } else {
                isr = new InputStreamReader(inputStream);
            }
            LOG.fine("Start parsing bravo file");
            AtomicLong lineNr = new AtomicLong();
            try (BufferedReader buffer = new BufferedReader(isr)) {
                String headerLine = null;
                headerSearch: while (headerLine == null) {
                    LOG.fine("Searching for header in bravo file");
                    String headerCandidate = buffer.readLine();
                    lineNr.incrementAndGet();
                    if (headerCandidate == null) {
                        throw new RuntimeException("Missing required header in file " + filename);
                    }
                    if (headerCandidate.startsWith(BOF)) {
                        LOG.fine("Found header");
                        headerLine = headerCandidate;
                        break headerSearch;
                    }
                }
                LOG.fine("Validate and parse header columns");
                final Map<String, Integer> colIndices = validateAndParseHeader(headerLine);
                DoubleFixProcessor downsampler = createProcessor(callback, trackIdentifier);
                buffer.lines().forEach(line -> {
                    lineNr.incrementAndGet();
                    downsampler.accept(parseLine(lineNr.get(), filename, line, colIndices));
                });

                downsampler.finish();
                buffer.close();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception parsing bravo CSV file " + filename, e);
        }
    }

    /**
     * This method creates the double fix processor chain used to downsample and batch the stream of fixes parsed by the
     * importer.
     * 
     * This method is protected so it can be overridden by the test case.
     * 
     * @param metadata
     * @param callback
     * @param trackIdentifier
     * @return
     */
    protected DoubleFixProcessor createProcessor(Callback callback,
            final TrackFileImportDeviceIdentifier trackIdentifier) {
        LearningBatchProcessor batchProcessor = new LearningBatchProcessor(5000, 5000, callback, trackIdentifier);
        DoubleFixProcessor downsampler = new DownsamplerTo1HzProcessor(trackColumnCount,
                batchProcessor);

        return downsampler;
    }

    /**
     * Parses the CSV line and reads the double data values in the order defined by the col enums.
     */
    private DoubleVectorFixData parseLine(long lineNr, String filename, String line, Map<String, Integer> columnsInFile) {
        try {
            String[] fileContentTokens = split(line);
            String epochColValue = fileContentTokens[2];
            long epoch;
            if (epochColValue != null && epochColValue.length() > 0) {
                epochColValue = epochColValue.substring(0, epochColValue.indexOf("."));
                epoch = Long.parseLong(epochColValue);
            } else {
                // we don't have epoch, skip the line
                return null;
            }
            double[] trackFixData = new double[trackColumnCount];
            for (int trackColumnIdx = 0; trackColumnIdx < trackColumnCount; trackColumnIdx++) {
                String columnNameToSearchForInFile = BravoSensorDataMetadata.values()[trackColumnIdx].getColumnName();
                Integer columnsInFileIdx = columnsInFile.get(columnNameToSearchForInFile);
                trackFixData[trackColumnIdx] = Double.parseDouble(fileContentTokens[columnsInFileIdx]);
            }
            return new DoubleVectorFixData(epoch, trackFixData);
        } catch (Exception e) {
            LOG.warning(
                    "Error parsing line nr " + lineNr + " in file " + filename + "with exception: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Integer> validateAndParseHeader(String headerLine) {
        final String[] headerTokens = split(headerLine);
        Map<String, Integer> colIndicesInFile = new HashMap<>();
        for (int j = BravoSensorDataMetadata.HEADER_COLUMN_OFFSET; j < headerTokens.length; j++) {
            String header = headerTokens[j];
            colIndicesInFile.put(header, j);
        }
        List<String> requiredColumnsInFix = BravoSensorDataMetadata.getTrackColumnNames();
        if (!colIndicesInFile.keySet().containsAll(requiredColumnsInFix)) {
            final Set<String> missingColumns = new HashSet<>(requiredColumnsInFix);
            missingColumns.removeAll(colIndicesInFile.keySet());
            LOG.log(Level.SEVERE, "Missing headers: "+missingColumns);
            throw new RuntimeException("Missing headers "+missingColumns+" in import files");
        }
        return colIndicesInFile;
    }

    private String[] split(String line) {
        return line.split("\t");
    }

    @Override
    public String getType() {
        return "BRAVO";
    }

    @Override
    public RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceCompetitorBravoMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }
}
