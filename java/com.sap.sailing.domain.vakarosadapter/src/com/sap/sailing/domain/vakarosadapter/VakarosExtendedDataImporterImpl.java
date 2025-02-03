package com.sap.sailing.domain.vakarosadapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.sap.sailing.server.trackfiles.impl.ExpeditionExtendedDataImporterImpl;
import com.sap.sse.common.TimePoint;

public class VakarosExtendedDataImporterImpl extends ExpeditionExtendedDataImporterImpl {
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
}
