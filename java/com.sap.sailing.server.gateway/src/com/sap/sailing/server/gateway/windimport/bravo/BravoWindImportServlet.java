package com.sap.sailing.server.gateway.windimport.bravo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter.Callback;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.server.gateway.windimport.AbstractWindImportServlet;
import com.sap.sailing.server.trackfiles.impl.BravoDataImporterImpl;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class BravoWindImportServlet extends AbstractWindImportServlet {
    private static final long serialVersionUID = -4547876638456305135L;
    private static final Logger logger = Logger.getLogger(BravoWindImportServlet.class.getName());
    
    @Override
    protected WindSource getWindSource(UploadRequest uploadRequest) {
        final WindSource windSource;
        final String sourceName;
        logger.info("Importing Bravo wind data from "+uploadRequest.files);
        if (uploadRequest.files != null && !uploadRequest.files.isEmpty()) {
            sourceName = uploadRequest.files.toString();
        } else {
            sourceName = "Bravo Wind Import";
        }
        windSource = new WindSourceWithAdditionalID(WindSourceType.WEB, sourceName + "@" + MillisecondsTimePoint.now());
        return windSource;
    }

    @Override
    protected Iterable<Wind> importWind(Map<InputStream, String> inputStreamsAndFilenames) throws IOException, InterruptedException, FormatNotSupportedException {
        final Iterable<Wind> result;
        if (inputStreamsAndFilenames != null && inputStreamsAndFilenames.size() == 1) {
            logger.info("Reading Bravo wind data from "+inputStreamsAndFilenames.values().iterator().next());
            result = readWind(inputStreamsAndFilenames.values().iterator().next(), inputStreamsAndFilenames.keySet().iterator().next());
        } else {
            final List<Wind> windList = new LinkedList<>();
            for (final Entry<InputStream, String> inputStreamAndFileName : inputStreamsAndFilenames.entrySet()) {
                logger.info("Reading Bravo wind data from "+inputStreamAndFileName.getValue());
                Util.addAll(readWind(inputStreamAndFileName.getValue(), inputStreamAndFileName.getKey()), windList);
            }
            result = windList;
        }
        return result;
    }

    private static enum Fields {
        Lat, Lon, TWS, TWD;
    }
    
    private Iterable<Wind> readWind(String filename, InputStream inputStream) throws InterruptedException, IOException, FormatNotSupportedException {
        final List<Wind> result = new LinkedList<>();
        Map<String, Integer> columnsMap = new HashMap<>();
        for (final Fields field : Fields.values()) {
            columnsMap.put(field.name(), field.ordinal());
        }
        BravoDataImporterImpl importer = new BravoDataImporterImpl(columnsMap);
        final Callback callback = new Callback() {
            @Override
            public void addFixes(Iterable<DoubleVectorFix> fixes, TrackFileImportDeviceIdentifier device) {
                for (final DoubleVectorFix fix : fixes) {
                    // latitude / longitude are represented in funny NMEA-like way; the value divided by 100 as
                    // a floored integer represents the full degrees; the value modulo 100 represents the decimal
                    // minutes. Example: the pair (4124.645890, 213.738670) stands for N41°24.645890 E002°13.738670
                    final Wind wind = new WindImpl(new DegreePosition(FunnyDegreeConverter.funnyLatLng(fix.get(Fields.Lat.ordinal())),
                            FunnyDegreeConverter.funnyLatLng(fix.get(Fields.Lon.ordinal()))),
                            fix.getTimePoint(), new KnotSpeedWithBearingImpl(fix.get(Fields.TWS.ordinal()),
                                    new DegreeBearingImpl(fix.get(Fields.TWD.ordinal())).reverse()));
                    result.add(wind);
                }
            }
        };
        if (filename.toLowerCase().endsWith("zip")) {
            logger.info("Bravo file "+filename+" is a ZIP file");
            final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry=zipInputStream.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".txt")) {
                    logger.info("Reading Bravo wind data from "+filename+"'s ZIP entry "+entry.getName());
                    importer.importFixes(zipInputStream, callback, filename, filename, /* downsample */ false);
                }
            }
        } else {
            importer.importFixes(inputStream, callback, filename, filename, /* downsample */ false);
        }
        return result;
    }
}
