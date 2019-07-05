package com.sap.sailing.domain.orc;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;

/**
 * Extracts ORC-Certificates from different sources and different formats. Returns ORC-Certificate objects for given
 * identification (sail number).
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 **/
public interface ORCCertificateImporter {

    /**
     * Creates an {@link ORCCertificate} object to a given sail number.
     * 
     * @param sailnumber
     *            as specified for the boat of the {@link Competitor) in the ORC files. The sailnumber commonly consists
     * out of an alphabetical national code (e.g. 'GER', 'GBR', 'SUI', ...) or an alphanumerical class code (e.g. 'F40',
     * 'X41', ...) and a numerical identification code.
     * @return {@code null}, if there isn't any data for the given sailnumber. Otherwise an {@link ORCCertificate}.
     */
    ORCCertificate getCertificate(String sailnumber);

    /**
     * Creates an {@link Map} of {@link ORCCertificate} objects to the given sail numbers.
     * <p>
     * Same functionality as {@code getCertificate} for a single sailnumber, just for multiple elements.
     * 
     * @param sailnumbers
     *            represents an array of sailnumbers as specified for the boat of the {@link Competitor) in the ORC
     * files. The sailnumber commonly consists out of an alphabetical national code (e.g. 'GER', 'GBR', 'SUI', ...) or
     * an alphanumerical class code (e.g. 'F40', 'X41', ...) and a numerical identification code.
     * @return {@link Map} of {@link ORCCertificate} values and the sailnumber parameters as keys. The value equals
     *         {@code null}, if there isn't any data for the given sailnumber.
     */
    Map<String, ORCCertificate> getCertificates(String[] sailnumbers);

}
