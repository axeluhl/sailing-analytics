package com.sap.sailing.domain.orc;

import java.util.Map;

/**
 * Extracts ORC-Certificates from different sources and different formats. Returns ORC-Certificate objects for given
 * identification (sailnumber).
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 **/
public interface ORCCertificateImporter {
    
    /**
     * Returns an ORCCertificate object to a given sailnumber.
     * **/
    ORCCertificate getCertificate(String sailnumber);
    
    
    /**
     * Returns a map of ORCCertificate objects to a given array of sailnumbers.
     * **/
    Map<String, ORCCertificate> getCertificates(String[] sailnumbers);

}
