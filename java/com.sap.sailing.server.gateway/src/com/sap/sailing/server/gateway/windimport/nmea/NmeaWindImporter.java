package com.sap.sailing.server.gateway.windimport.nmea;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class NmeaWindImporter extends AbstractWindImporter {
    private static final Logger logger = Logger.getLogger(NmeaWindImporter.class.getName());

    @Override
    protected WindSource getDefaultWindSource(UploadRequest uploadRequest) {
        final WindSource windSource;
        final String sourceName;
        logger.info("Importing NMEA wind data from "+uploadRequest.files);
        if (uploadRequest.files != null && !uploadRequest.files.isEmpty()) {
            sourceName = uploadRequest.files.toString();
        } else {
            sourceName = "NMEA Wind Import";
        }
        windSource = new WindSourceWithAdditionalID(WindSourceType.WEB, sourceName + "@" + MillisecondsTimePoint.now());
        return windSource;
    }

    @Override
    protected Map<WindSource, Iterable<Wind>> importWind(WindSource defaultWindSource,
            Map<InputStream, Pair<String, Charset>> inputStreamsAndFilenamesAndCharsets) throws IOException, InterruptedException {
        final Iterable<Wind> windFixes;
        if (inputStreamsAndFilenamesAndCharsets != null && inputStreamsAndFilenamesAndCharsets.size() == 1) {
            final Pair<String, Charset> filenameAndCharset = inputStreamsAndFilenamesAndCharsets.values().iterator().next();
            logger.info("Reading NMEA wind data from "+filenameAndCharset);
            windFixes = readWind(filenameAndCharset.getA(), inputStreamsAndFilenamesAndCharsets.keySet().iterator().next(), filenameAndCharset.getB());
        } else {
            final List<Wind> windList = new LinkedList<>();
            for (final Entry<InputStream, Pair<String, Charset>> inputStreamAndFileName : inputStreamsAndFilenamesAndCharsets.entrySet()) {
                logger.info("Reading NMEA wind data from "+inputStreamAndFileName.getValue());
                Util.addAll(readWind(inputStreamAndFileName.getValue().getA(), inputStreamAndFileName.getKey(),
                        inputStreamAndFileName.getValue().getB()), windList);
            }
            windFixes = windList;
        }
        final Map<WindSource, Iterable<Wind>> result = new HashMap<>();
        result.put(defaultWindSource, windFixes);
        return result;
    }

    private Iterable<Wind> readWind(String filename, InputStream inputStream, Charset charset) throws InterruptedException, IOException {
        final Iterable<Wind> result;
        if (filename.toLowerCase().endsWith("zip")) {
            logger.info("NMEA file "+filename+" is a ZIP file");
            final List<Wind> windList = new LinkedList<>();
            final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry=zipInputStream.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".txt")) {
                    logger.info("Reading NMEA wind data from "+filename+"'s ZIP entry "+entry.getName());
                    Util.addAll(NmeaFactory.INSTANCE.readWind(zipInputStream), windList);
                }
            }
            result = windList;
        } else {
            result = NmeaFactory.INSTANCE.readWind(inputStream);
        }
        return result;
    }
}
