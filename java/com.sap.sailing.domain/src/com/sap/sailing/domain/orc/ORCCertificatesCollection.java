package com.sap.sailing.domain.orc;

import com.sap.sailing.domain.common.orc.ORCCertificate;

/**
 * Extracts ORC-Certificates from different sources and different formats. Returns ORC-Certificate objects for given
 * identification (sail number).
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 **/
public interface ORCCertificatesCollection {
    /**
     * Tells the sail numbers that can be used as key in {@link #getCertificateById(String)} and {@link #getCertificates(String[])}
     * and for which certificates are available in this object.
     */
    Iterable<String> getCertificateIds();
    
    /**
     * Creates an {@link ORCCertificate} object to a given sail number.
     * 
     * @param sailnumber
     *            as specified for the boat of the {@link Competitor) in the ORC files. The sailnumber commonly consists
     * out of an alphabetical national code (e.g. 'GER', 'GBR', 'SUI', ...) or an alphanumerical class code (e.g. 'F40',
     * 'X41', ...) and a numerical identification code.
     * @return {@code null}, if there isn't any data for the given sailnumber. Otherwise an {@link ORCCertificate}.
     */
    ORCCertificate getCertificateById(String sailnumber);
    
    ORCCertificate getCertificateByBoatName(String boatName);
    
    Iterable<String> getBoatNames();
    
    Iterable<ORCCertificate> getCertificates();
}
