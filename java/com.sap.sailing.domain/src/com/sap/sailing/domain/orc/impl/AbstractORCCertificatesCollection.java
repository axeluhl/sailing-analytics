package com.sap.sailing.domain.orc.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sse.common.Util;

public abstract class AbstractORCCertificatesCollection implements ORCCertificatesCollection {
    
    /**
     * From a sail number which may contain spaces, also redundantly and repeatedly, as well
     * as dashes, slashes or other special characters, and where upper and lower case may be mixed,
     * constructs a canonicalized version such that the chances for producing a match when searching
     * for a sail number is maximized, while still minimizing the chances for a false match.
     */
    protected String getCanonicalizedSailNumber(String sailNumber) {
        return sailNumber.replaceAll(" ", "").replaceAll("-", "").toUpperCase();
    }
    
    /**
     * Only the first 16 characters after removing spaces and other special characters such as parentheses and dashes
     * are compared case-insensitively.
     */
    protected String getCanonicalizedBoatName(String boatName) {
        final String replaced = boatName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return replaced.substring(0, Math.min(replaced.length(), 16));
    }

    /**
     * Returns a {@link Map} of {@link ORCCertificateImpl} keyed by the {@link String} sailnumbers, which were given as
     * an input inside an array.
     */
    @Override
    public Map<String, ORCCertificate> getCertificates(String[] sailnumbers) {
        Map<String, ORCCertificate> result = new HashMap<>();
        for (String sailnumber : sailnumbers) {
            result.put(sailnumber, getCertificateBySailNumber(sailnumber));
        }
        return result;
    }

    @Override
    public ORCCertificate getCertificateByBoatName(String boatName) {
        final String canonicalizedBoatNameToSearchFor = getCanonicalizedBoatName(boatName);
        for (final ORCCertificate certificate : getCertificates()) {
            if (canonicalizedBoatNameToSearchFor.equals(getCanonicalizedBoatName(certificate.getBoatName()))) {
                return certificate;
            }
        }
        return null;
    }

    @Override
    public Iterable<ORCCertificate> getCertificates() {
        return Util.map(getSailNumbers(), sailNumber->getCertificateBySailNumber(sailNumber));
    }

    @Override
    public Iterable<String> getBoatNames() {
        return Util.map(getCertificates(), c->getCanonicalizedBoatName(c.getBoatName()));
    }
    
    @Override
    public String toString() {
        return Util.joinStrings(", ", Util.map(getCertificates(), c->c.toString()));
    }

}
