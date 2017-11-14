package com.sap.sailing.server.trackfiles.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorExpeditionExtendedMappingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.sensordata.ExpeditionExtendedSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.impl.DoubleVectorFixImpl;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Importer for CSV data files from Bravo units used by the SAP Extreme Sailing Team.
 */
public class ExpeditionExtendedDataImporterImpl extends AbstractDoubleVectorFixImporter implements DoubleVectorFixImporter {
    public static final String EXPEDITION_EXTENDED_TYPE = "EXPEDITION_EXTENDED";
    private static final Logger logger = Logger.getLogger(ExpeditionExtendedDataImporterImpl.class.getName());
    private static final String ORIGINAL_POSITION_HEADER = "Pos[ddd.dd]";
    public static final String COL_NAME_LAT = "Lat";
    public static final String COL_NAME_LON = "Lon";
    private static final String UTC_COLUMN = "Utc";
    private static final String DATE_COLUMN_1 = "dd/mm/yy";
    private static final String DATE_COLUMN_1_PATTERN = "dd/MM/yy";
    private static final String DATE_COLUMN_2 = "mm/dd/yy";
    private static final String DATE_COLUMN_2_PATTERN = "MM/dd/yy";
    private static final String TIME_COLUMN = "hhmmss";
    private static final String GPS_TIME_COLUMN = "GPS Time";
    private final Map<String, Integer> columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix;
    
    /**
     * The maximum index into the double vector fix from the
     * {@link #columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix} values
     */
    private final int trackColumnCount;

    public ExpeditionExtendedDataImporterImpl() {
        super(EXPEDITION_EXTENDED_TYPE);
        columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix = ExpeditionExtendedSensorDataMetadata.getColumnNamesToIndexInDoubleFix();
        trackColumnCount = columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.values().stream().max((x,y)->Integer.compare(x, y)).get()+1;
    }

    @Override
    public RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceCompetitorExpeditionExtendedMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }

    @Override
    public void importFixes(InputStream inputStream, final Callback callback, String filename, String sourceName,
            boolean downsample) throws FormatNotSupportedException, IOException {
        final TrackFileImportDeviceIdentifier trackIdentifier = new TrackFileImportDeviceIdentifierImpl(
                UUID.randomUUID(), filename, sourceName, MillisecondsTimePoint.now());
        try {
            logger.fine("Import Expedition CSV from " + filename);
            final InputStreamReader isr;
            if (sourceName.endsWith("gz")) {
                logger.fine("Using gzip stream reader " + filename);
                isr = new InputStreamReader(new GZIPInputStream(inputStream));
            } else {
                isr = new InputStreamReader(inputStream);
            }
            logger.fine("Start parsing Expedition file");
            AtomicLong lineNr = new AtomicLong();
            try (BufferedReader buffer = new BufferedReader(isr)) {
                String headerLine = buffer.readLine();
                lineNr.incrementAndGet();
                logger.fine("Validate and parse header columns");
                final Map<String, Integer> colIndices = parseHeader(headerLine);
                validateHeader(colIndices);
                buffer.lines().forEach(line -> {
                    lineNr.incrementAndGet();
                    if (!line.trim().isEmpty()) {
                        parseLine(lineNr.get(), filename, line, colIndices, (timePoint, fileContentTokens, columnsInFileFromHeader)->{
                            Double[] trackFixData = new Double[trackColumnCount];
                            for (final Entry<String, Integer> columnNameToSearchForInFile : columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.entrySet()) {
                                Integer columnsInFileIdx = columnsInFileFromHeader.get(columnNameToSearchForInFile.getKey());
                                trackFixData[columnNameToSearchForInFile.getValue()] = columnsInFileIdx >= fileContentTokens.length ?
                                        null : fileContentTokens[columnsInFileIdx].trim().isEmpty() ? null :
                                            Double.parseDouble(fileContentTokens[columnsInFileIdx]);
                            }
                            callback.addFixes(Collections.singleton(new DoubleVectorFixImpl(
                                    timePoint, trackFixData)), trackIdentifier);
                        });
                    }
                });
                buffer.close();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception parsing Expedition CSV log file " + filename, e);
        }
    }

    /**
     * When the header contains one or more occurrences of {@link #ORIGINAL_POSITION_HEADER}, it is substituted
     * by the two header columns {@link #COL_NAME_LAT} and {@link #COL_NAME_LON} because that's
     * how positions are stored in Expedition files: as two comma-separated values, one for latitude, another
     * for longitude, although there is only one header field.
     */
    public static Map<String, Integer> parseHeader(String headerLine) {
        final String[] headerTokens = split(headerLine);
        Map<String, Integer> colIndicesInFile = new HashMap<>();
        int columnInResultingHeader = 0;
        for (int columnInHeader = 0; columnInHeader < headerTokens.length; columnInHeader++) {
            String header = headerTokens[columnInHeader];
            if (header.equals(ORIGINAL_POSITION_HEADER)) {
                colIndicesInFile.put(COL_NAME_LAT, columnInResultingHeader++);
                colIndicesInFile.put(COL_NAME_LON, columnInResultingHeader++);
            } else {
                colIndicesInFile.put(header, columnInResultingHeader++);
            }
        }
        return colIndicesInFile;
    }

    /**
     * Ensures that all columns in {@link #columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix}'s key set
     * are present in {@code colIndicesInFile}'s key set. If not, an exception is thrown that reports the columns
     * missing.
     */
    private void validateHeader(Map<String, Integer> colIndicesInFile) {
        Iterable<String> requiredColumnsInFix = columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix.keySet();
        if (!Util.containsAll(colIndicesInFile.keySet(), requiredColumnsInFix)) {
            final Set<String> missingColumns = new HashSet<>();
            Util.addAll(requiredColumnsInFix, missingColumns);
            missingColumns.removeAll(colIndicesInFile.keySet());
            logger.log(Level.SEVERE, "Missing headers: "+missingColumns);
            throw new RuntimeException("Missing headers "+missingColumns+" in import files");
        }
    }

    public static String[] split(String line) {
        return line.split("\\s*,\\s*");
    }

    /**
     * Consumes a line split by the rules of the CSV format handled here; the first argument to
     * {@link #accept(TimePoint, String[], Map) is the time stamp parsed from the line, the second is the tokenized
     * array, each element representing one column in the current line; the third argument maps column names as found
     * in the header line to their 0-based index in the array.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    @FunctionalInterface
    public static interface LineParserCallback {
        void accept(TimePoint timePoint, String[] columns, Map<String, Integer> header);
    }
    
    /**
     * Parses the CSV line and reads the double data values in the order defined by the col enums.
     */
    public static void parseLine(long lineNr, String filename, String line, Map<String, Integer> columnsInFileFromHeader,
            LineParserCallback callback) {
        try {
            String[] fileContentTokens = split(line);
            final TimePoint timePoint;
            final String date;
            final String dateFormatPattern;
            if (columnsInFileFromHeader.containsKey(UTC_COLUMN)) {
                timePoint = getTimePoint(fileContentTokens[columnsInFileFromHeader.get(UTC_COLUMN)]);
            } else if (columnsInFileFromHeader.containsKey(DATE_COLUMN_1) || columnsInFileFromHeader.containsKey(DATE_COLUMN_2)) {
                if (columnsInFileFromHeader.containsKey(DATE_COLUMN_1)) {
                    date = fileContentTokens[columnsInFileFromHeader.get(DATE_COLUMN_1)];
                    dateFormatPattern = DATE_COLUMN_1_PATTERN;
                } else {
                    date = fileContentTokens[columnsInFileFromHeader.get(DATE_COLUMN_2)];
                    dateFormatPattern = DATE_COLUMN_2_PATTERN;
                }
                final String time = fileContentTokens[columnsInFileFromHeader.get(TIME_COLUMN)];
                final DateFormat df = new SimpleDateFormat(dateFormatPattern+"'T'HH:mm:ssX");
                final Date timestamp = df.parse(date+"T"+time+"+00:00"); // assuming UTC
                timePoint = new MillisecondsTimePoint(timestamp);
            } else {
                // assume that "GPS Time" is present:
                timePoint = getTimePoint(fileContentTokens[columnsInFileFromHeader.get(GPS_TIME_COLUMN)]);
            }
            callback.accept(timePoint, fileContentTokens, columnsInFileFromHeader);
        } catch (Exception e) {
            logger.warning(
                    "Error parsing line nr " + lineNr + " in file " + filename + " with exception: " + e.getMessage());
        }
    }

    /**
     * The 1900 Date System http://support.microsoft.com/kb/180162/en-us
     */
    private static final Calendar EXCEL_EPOCH_START = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    static {
        EXCEL_EPOCH_START.set(1899, Calendar.DECEMBER, 30, 0, 0, 0);
        EXCEL_EPOCH_START.set(Calendar.MILLISECOND, 0);
    }
    private static final BigDecimal MILLISECONDS_PER_DAY = BigDecimal.valueOf(24 * 60 * 60 * 1000);
    /**
     * Converts a value from a column such as "GPS Time" to a {@link TimePoint}. The value is expected to be provided
     * in decimal format, representing the days since the {@link #EXCEL_EPOCH_START "Excel Epoch Start"}. 
     * @param time_ExcelEpoch
     * @return
     */
    public static TimePoint getTimePoint(String time_ExcelEpoch) {
        final TimePoint timePoint;
        if (!time_ExcelEpoch.trim().isEmpty()) {
            BigDecimal timeStamp = new BigDecimal(time_ExcelEpoch);
            long millisecondsSinceExcelEpochStart = timeStamp.multiply(MILLISECONDS_PER_DAY).longValue();
            timePoint = new MillisecondsTimePoint(
                    EXCEL_EPOCH_START.getTimeInMillis() + millisecondsSinceExcelEpochStart);
        } else {
            timePoint = null;
        }
        return timePoint;
    }

}
