package com.sap.sailing.domain.common.orc;

import java.io.Serializable;
import java.util.Map;

/**
 * ORC certificates can be obtained from various sources, coming in two forms: upload or download. Uploads happen
 * through a file upload servlet; downloads may happen from the ORC certificate database or from general URLs delivering
 * the certificate document. The solution typically accepts the common RMS and JSON formats for the certificates.
 * <p>
 * 
 * Once provided from document sources, the zero or more certificates extracted from those sources may be used to assign
 * certificates to boats. The boats are identified by their ID, and no more than one {@link ORCCertificate#getId()
 * certificate ID} can be provided per boat ID.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ORCCertificateSelection {
    /**
     * For zero or more boats identified by their ID in the key of an entry, tells which certificate to use for that
     * boat in the value of that entry. The result is never {@code null} but may be empty.
     */
    Iterable<Map.Entry<Serializable, String>> getCertificateIdsForBoatIds();
}
