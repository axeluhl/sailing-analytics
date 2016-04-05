package com.sap.sailing.server.trackfiles.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.sensordata.KnownSensorDataTypes;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * TODO: access to columns enum actually by public static enum. Col definition should belong to instance, so we can have
 * different col definitions.
 */
public class BravoDataImporterImpl implements DoubleVectorFixImporter {
    private final Logger LOG = Logger.getLogger(DoubleVectorFixImporter.class.getName());
    private final static DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSSSSS");
    private final BravoSensorDataMetadata metadata = BravoSensorDataMetadata.INSTANCE;
    private final String BOF = "jjlDATE\tjjlTIME";

    public void importFixes(InputStream inputStream, Callback callback, String sourceName)
            throws FormatNotSupportedException, IOException {

        final TrackFileImportDeviceIdentifier trackIdentifier = new TrackFileImportDeviceIdentifierImpl(
                UUID.randomUUID(), sourceName, sourceName + "_Imu", MillisecondsTimePoint.now());
        try {
            LOG.fine("Import CSV from " + sourceName);
            final InputStreamReader isr;
            if (sourceName.endsWith("gz")) {
                LOG.fine("Using gzip stream reader " + sourceName);
                isr = new InputStreamReader(new GZIPInputStream(inputStream));
            } else {
                isr = new InputStreamReader(inputStream);
            }
            LOG.fine("Start parsing imu file");
            try (BufferedReader buffer = new BufferedReader(isr)) {
                String headerLine = null;
                headerSearch: while (headerLine == null) {
                    LOG.fine("Searching for header in imu file");
                    String headerCandidate = buffer.readLine();
                    if (headerCandidate == null) {
                        throw new RuntimeException("Missing required header in file " + sourceName);
                    }
                    if (headerCandidate.startsWith(BOF)) {
                        LOG.fine("Found header");
                        headerLine = headerCandidate;
                        break headerSearch;
                    }
                }
                LOG.fine("Validate and parse header columns");
                final Map<String, Integer> colIndices = validateAndParseHeader(headerLine);
                LOG.fine("Parse data");
                buffer.lines().forEach(line -> callback.addFix(parseLine(line, colIndices), trackIdentifier));
                buffer.close();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception parsing CSV file " + sourceName, e);
        }
    }

    /**
     * Parses the CSV line and reads the double data values in the order defined by the col enums.
     * 
     * @param line
     * @param colIndices
     * @return
     */
    private DoubleVectorFixImpl parseLine(String line, Map<String, Integer> colIndices) {
        LOG.fine("Process data line: " + line);
        String[] contentTokens = split(line);
        String jjLDATE = contentTokens[0].substring(0, 9);
        StringBuilder dtb = new StringBuilder(jjLDATE);
        String jjlTIME = contentTokens[1];
        int offset = 6 - jjlTIME.indexOf(".");
        IntStream.range(0, offset).forEach(n -> dtb.append("0"));
        dtb.append(jjlTIME);
        LocalDateTime day = DATE_FMT.parse(dtb.toString(), LocalDateTime::from);
        Instant instant = day.toInstant(ZoneOffset.UTC);
        TimePoint fixTp = new MillisecondsTimePoint(Date.from(instant));
        double[] fixData = new double[metadata.getColumns().size()];
        for (int columnIndexInFix = 0; columnIndexInFix < fixData.length; columnIndexInFix++) {
            String columnName = metadata.getColumns().get(columnIndexInFix);
            Integer columnIndexInFile = colIndices.get(columnName);
            fixData[columnIndexInFix] = Double.valueOf(contentTokens[columnIndexInFile]);
        }
        return new DoubleVectorFixImpl(fixTp, fixData);
    }

    private Map<String, Integer> validateAndParseHeader(String headerLine) {
        final String[] headerTokens = split(headerLine);
        Map<String, Integer> colIndicesInFile = new HashMap<>();
        for (int j = 2; j < headerTokens.length; j++) {
            String header = headerTokens[j];
            colIndicesInFile.put(header, j);
        }
        List<String> columnsInFix = metadata.getColumns();
        if (colIndicesInFile.size() != columnsInFix.size() || !colIndicesInFile.keySet().containsAll(columnsInFix)) {
            LOG.log(Level.SEVERE, "Missing headers");
            throw new RuntimeException("Missing headers in import files");
        }
        return colIndicesInFile;
    }

    private String[] split(String line) {
        return line.split("\t");
    }

    @Override
    public String getType() {
        return KnownSensorDataTypes.BRAVO.name();
    }
}
