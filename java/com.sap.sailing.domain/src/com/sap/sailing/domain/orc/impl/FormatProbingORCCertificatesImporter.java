package com.sap.sailing.domain.orc.impl;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sailing.domain.orc.ORCCertificatesImporter;
import com.sap.sse.common.Util;
import com.sap.sse.util.ThreadPoolUtil;

public class FormatProbingORCCertificatesImporter implements ORCCertificatesImporter {
    private static final Logger logger = Logger.getLogger(FormatProbingORCCertificatesImporter.class.getName());
    
    private final ORCCertificatesJsonImporter jsonImporter;
    private final ORCCertificatesRmsImporter rmsImporter;
    final ScheduledExecutorService executor;
    
    public FormatProbingORCCertificatesImporter() {
        jsonImporter = new ORCCertificatesJsonImporter();
        rmsImporter = new ORCCertificatesRmsImporter();
        executor = ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor();
    }
    
    @Override
    public ORCCertificatesCollection read(InputStream inputStream) throws IOException, ParseException {
        final byte[] content = IOUtils.toByteArray(inputStream);
        final Future<ORCCertificatesCollectionJSON> jsonFuture = executor.submit(()->jsonImporter.read(new ByteArrayInputStream(content)));
        final Future<ORCCertificatesCollectionRMS> rmsFuture = executor.submit(()->rmsImporter.read(new ByteArrayInputStream(content)));
        return getResultFromFutures(jsonFuture, rmsFuture);
    }

    @Override
    public ORCCertificatesCollection read(Reader reader) throws IOException, ParseException {
        final char[] content = IOUtils.toCharArray(reader);
        final Future<ORCCertificatesCollectionJSON> jsonFuture = executor.submit(()->jsonImporter.read(new CharArrayReader(content)));
        final Future<ORCCertificatesCollectionRMS> rmsFuture = executor.submit(()->rmsImporter.read(new CharArrayReader(content)));
        return getResultFromFutures(jsonFuture, rmsFuture);
    }

    private ORCCertificatesCollection getResultFromFutures(final Future<ORCCertificatesCollectionJSON> jsonFuture,
            final Future<ORCCertificatesCollectionRMS> rmsFuture) {
        ORCCertificatesCollection result = null;
        try {
            result = jsonFuture.get();
        } catch (Exception e) {
            logger.log(Level.FINE, "Couldn't read ORC certificates from input stream as JSON: "+e.getMessage());
        }
        if (result == null || Util.isEmpty(result.getSailNumbers())) {
            // nothing from JSON; try RMS
            try {
                result = rmsFuture.get();
            } catch (Exception e) {
                logger.log(Level.FINE, "Couldn't read ORC certificates from input stream as RMS either: "+e.getMessage());
            }
        }
        return result;
    }

}
