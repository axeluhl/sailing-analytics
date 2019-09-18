package com.sap.sailing.domain.orc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.orc.impl.FormatProbingORCCertificatesImporter;

/**
 * A factory for {@link ORCCertificatesCollection} objects, based on {@link InputStream}s or
 * {@link Reader}s. Implementations may be specific to a format, such as "RMS" or "JSON" or may
 * probe the stream/reader contents and figure out which format to use for the import.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ORCCertificatesImporter {
    /**
     * An instance of this interface, able to probe the input presented for either RMS or JSON format.
     */
    ORCCertificatesImporter INSTANCE = new FormatProbingORCCertificatesImporter();
    
    ORCCertificatesCollection read(InputStream inputStream) throws IOException, ParseException;
    
    ORCCertificatesCollection read(Reader reader) throws IOException, ParseException;
}
