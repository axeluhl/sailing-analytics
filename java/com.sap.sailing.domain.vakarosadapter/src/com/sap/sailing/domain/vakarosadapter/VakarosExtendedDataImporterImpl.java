package com.sap.sailing.domain.vakarosadapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.sensordata.ExpeditionExtendedSensorDataMetadata;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.ExpeditionExtendedDataImporterImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class VakarosExtendedDataImporterImpl extends ExpeditionExtendedDataImporterImpl {
    private static final Logger logger = Logger.getLogger(VakarosExtendedDataImporterImpl.class.getName());

    private static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final String VAKAROS_EXTENDED = "VAKAROS_EXTENDED";
    private static final String HEEL_COLUMN_HEADING = "roll";
    private static final String PITCH_COLUMN_HEADING = "pitch";
    private static final String LOAD_GDF1_COLUMN_HEADING = "load_gdf1";
    private static final String LOAD_GDF2_COLUMN_HEADING = "load_gdf2";

    public VakarosExtendedDataImporterImpl() {
        super(VAKAROS_EXTENDED); // FIXME the ExpeditionImportFileHandler has a hard-coded set of file name extensions; we want our own!
    }

    @Override
    protected Map<String, Integer> getColumnNamesToIndexInDoubleFix() {
        return Util.<String, Integer> mapBuilder()
                .put(HEEL_COLUMN_HEADING, ExpeditionExtendedSensorDataMetadata.HEEL.getColumnIndex())
                .put(PITCH_COLUMN_HEADING, ExpeditionExtendedSensorDataMetadata.TRIM.getColumnIndex())
                .put(LOAD_GDF1_COLUMN_HEADING, ExpeditionExtendedSensorDataMetadata.FORESTAY_LOAD.getColumnIndex())
                .put(LOAD_GDF2_COLUMN_HEADING, ExpeditionExtendedSensorDataMetadata.EXPEDITION_KICKER_TENSION.getColumnIndex())
            .build();
    }

    @Override
    protected TrackFileImportDeviceIdentifierImpl getTrackIdentifier(String filename, String sourceName) {
        return new TrackFileImportDeviceIdentifierImpl(
                UUID.randomUUID(), filename+"-SENSORS", sourceName, MillisecondsTimePoint.now());
    }

    @Override
    public TimePoint getTimePointFromLine(Map<String, Integer> columnsInFileFromHeader, String[] lineContentTokens)
            throws ParseException {
        return TimePoint.of(timestampFormat
                .parse(lineContentTokens[columnsInFileFromHeader.get(VakarosGPSFixImporter.TIMESTAMP_COLUMN_HEADING)]+"Z"));
    }
    
    /**
     * Ensures that all columns in
     * {@link #columnNamesInFileAndTheirValueIndexInResultingDoubleVectorFix}'s
     * key set are present in {@code colIndicesInFile}'s key set. If not, an
     * exception is thrown that reports the columns missing.
     */
    @Override
    public void validateHeader(Map<String, Integer> colIndicesInFile) throws FormatNotSupportedException {
        final boolean dateTimeFormatOk;
        dateTimeFormatOk = colIndicesInFile.containsKey(VakarosGPSFixImporter.TIMESTAMP_COLUMN_HEADING);
        if (!dateTimeFormatOk) {
            final String msg = "Missing timestamp header; expected "+VakarosGPSFixImporter.TIMESTAMP_COLUMN_HEADING;
            logger.log(Level.SEVERE, msg);
            throw new FormatNotSupportedException(msg);
        }
    }
}
