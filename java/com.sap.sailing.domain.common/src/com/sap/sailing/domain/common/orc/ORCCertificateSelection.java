package com.sap.sailing.domain.common.orc;

/**
 * ORC certificates can be obtained from various sources, coming in two forms: upload or download. Uploads happen
 * through a file upload servlet; downloads may happen from the ORC certificate database or from general URLs delivering
 * the certificate document. The solution typically accepts the common RMS and JSON formats for the certificates.
 * <p>
 * 
 * Once provided from a document source, the zero or more certificates extracted from that source may be used to assign
 * certificates to boats. The boats are identified by their ID, and no more than one {@link ORCCertificate#getId()
 * certificate ID} must be provided per boat ID.
 * <p>
 * 
 * The certificates within the documents are identified by the {@code RefNo} field in case of a JSON document, or the
 * {@code NATCERTN.FILE_ID} field in case of an RMS document, respectively.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ORCCertificateSelection {
    
}
