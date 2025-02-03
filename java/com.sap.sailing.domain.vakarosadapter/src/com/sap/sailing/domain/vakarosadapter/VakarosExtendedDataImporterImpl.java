package com.sap.sailing.domain.vakarosadapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.trackfiles.impl.ExpeditionExtendedDataImporterImpl;
import com.sap.sse.common.TimePoint;

public class VakarosExtendedDataImporterImpl extends ExpeditionExtendedDataImporterImpl {
    private static final Logger logger = Logger.getLogger(VakarosExtendedDataImporterImpl.class.getName());

    private static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static final String VAKAROS_EXTENDED = "VAKAROS_EXTENDED";

    public VakarosExtendedDataImporterImpl() {
        super(VAKAROS_EXTENDED);
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
