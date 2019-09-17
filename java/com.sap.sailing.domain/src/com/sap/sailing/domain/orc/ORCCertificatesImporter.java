package com.sap.sailing.domain.orc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.json.simple.parser.ParseException;

/**
 * A factory for {@link ORCCertificatesCollection} objects, based on {@link InputStream}s or
 * {@link Reader}s.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ORCCertificatesImporter {
    ORCCertificatesCollection read(InputStream inputStream) throws IOException, ParseException;
    
    ORCCertificatesCollection read(Reader reader) throws IOException, ParseException;
}
