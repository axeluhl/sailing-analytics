package com.sap.sailing.server.trackfiles.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.doublefix.DoubleFixProcessor;
import com.sap.sailing.server.trackfiles.impl.doublefix.DoubleVectorFixData;
import com.sap.sailing.server.trackfiles.impl.doublefix.DownsamplerTo1HzProcessor;
import com.sap.sailing.server.trackfiles.impl.doublefix.LearningBatchProcessor;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Base class that processes CSV files containing data from Bravo units. Those files have the following specifics:
 * <ul>
 * <li>There are several comment lines at the beginning starting with {@code #}</li>
 * <li>Date and headlines are separated by tabs</li>
 * <li>The first relevant line contains headlines. The first three values are jjlDATE, jjlTIME and Epoch. The order of
 * all other columns may vary.</li>
 * <li>All values are double values.</li>
 * </ul>
 * 
 * Due to the data being raw data, this importer supports downsampling to improve data quality and reduce the amount of
 * data being stored in the DB.
 * 
 * TODO: access to columns enum actually by public static enum. Col definition should belong to instance, so we can have
 * different col definitions.
 */
public class BaseBravoDataImporterImpl extends AbstractDoubleVectorFixImporter {
    private final Logger LOG = Logger.getLogger(DoubleVectorFixImporter.class.getName());
    private final String BOF = "jjlDATE\tjjlTIME\tEpoch";
    private final Map<String, Integer> columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix;

    public BaseBravoDataImporterImpl(Map<String, Integer> columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix,
            String type) {
        super(type);
        this.columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix = columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix;
    }
    
    /**
     * @param inputStream
     *            the stream from which to read the fixes; this method will <em>not</em> close the stream; it's the
     *            caller who is responsible for closing it. This, in particular, allows for usages with ZIP files
     *            that would suffer from closing the stream here
     * @param downsample
     *            if {@code true}, fixes will be down-sampled to a 1Hz frequency before being emitted to the
     *            {@code callback}. Otherwise, all fixes read will be forwarded straight to the {@link Callback}.
     */
    public boolean importFixes(InputStream inputStream, Callback callback, final String filename, String sourceName,
            boolean downsample) throws FormatNotSupportedException, IOException {
        final TrackFileImportDeviceIdentifier trackIdentifier = new TrackFileImportDeviceIdentifierImpl(
                UUID.randomUUID(), filename, sourceName, MillisecondsTimePoint.now());
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
        BufferedReader buffer = new BufferedReader(isr);
        String headerLine = null;
        headerSearch: while (headerLine == null) {
            LOG.fine("Searching for header in bravo file");
            String headerCandidate = buffer.readLine();
            lineNr.incrementAndGet();
            if (headerCandidate == null) {
                throw new FormatNotSupportedException("Missing required header in file " + filename);
            }
            if (headerCandidate.startsWith(BOF)) {
                LOG.fine("Found header");
                headerLine = headerCandidate;
                break headerSearch;
            }
        }
        LOG.fine("Validate and parse header columns");
        final Map<String, Integer> colIndices = validateAndParseHeader(headerLine);
        DoubleFixProcessor downsampler = downsample ? createDownsamplingProcessor(callback, trackIdentifier)
                : fix -> callback.addFixes(
                        Collections.singleton(new DoubleVectorFixImpl(fix.getTimepoint(), fix.getFix())),
                        trackIdentifier);
        final AtomicBoolean importedFixes = new AtomicBoolean(false);
        buffer.lines().forEach(line -> {
            lineNr.incrementAndGet();
            downsampler.accept(parseLine(lineNr.get(), filename, line, colIndices));
            importedFixes.set(true);
        });
        downsampler.finish();
        return importedFixes.get();
    }

    /**
     * This method creates the double fix processor chain used to downsample and batch the stream of fixes parsed by the
     * importer.
     * 
     * This method is protected so it can be overridden by the test case.
     */
    protected DoubleFixProcessor createDownsamplingProcessor(Callback callback,
            final TrackFileImportDeviceIdentifier trackIdentifier) {
        LearningBatchProcessor batchProcessor = new LearningBatchProcessor(5000, 5000, callback, trackIdentifier);
        DoubleFixProcessor downsampler = new DownsamplerTo1HzProcessor(getTrackColumnCount(),
                batchProcessor);
        return downsampler;
    }

    /**
     * Parses the CSV line and reads the double data values in the order defined by the col enums.
     */
    private DoubleVectorFixData parseLine(long lineNr, String filename, String line, Map<String, Integer> columnsInFileFromHeader) {
        try {
            final DoubleVectorFixData result;
            String[] fileContentTokens = split(line);
            String epochColValue = fileContentTokens[2];
            long epoch;
            if (epochColValue != null && epochColValue.length() > 0) {
                epochColValue = epochColValue.substring(0, epochColValue.indexOf("."));
                epoch = Long.parseLong(epochColValue);
                Double[] trackFixData = new Double[getTrackColumnCount()];
                for (final Entry<String, Integer> columnNameToSearchForInFile : columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.entrySet()) {
                    Integer columnsInFileIdx = columnsInFileFromHeader.get(columnNameToSearchForInFile.getKey());
                    trackFixData[columnNameToSearchForInFile.getValue()] = Double.parseDouble(fileContentTokens[columnsInFileIdx]);
                }
                result = new DoubleVectorFixData(epoch, trackFixData);
            } else {
                result = null;
            }
            return result;
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
        Iterable<String> requiredColumnsInFix = columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.keySet();
        if (!Util.containsAll(colIndicesInFile.keySet(), requiredColumnsInFix)) {
            final Set<String> missingColumns = new HashSet<>();
            Util.addAll(requiredColumnsInFix, missingColumns);
            missingColumns.removeAll(colIndicesInFile.keySet());
            LOG.log(Level.SEVERE, "Missing headers: " + missingColumns);
            StringBuilder errorMessage = new StringBuilder("Missing headers ").append(String.join(", ", missingColumns))
                    .append(" in import file");
            throw new RuntimeException(errorMessage.toString());
        }
        return colIndicesInFile;
    }

    private String[] split(String line) {
        return line.split("\t");
    }

    private int getTrackColumnCount() {
        return Collections.max(columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.values())+1;
    }
}
