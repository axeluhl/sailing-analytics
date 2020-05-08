package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.orc.ORCCertificatesCollection;
import com.sap.sse.common.Util;

public abstract class AbstractORCCertificatesCollection implements ORCCertificatesCollection {
    /**
     * Only the first 16 characters after removing spaces and other special characters such as parentheses and dashes
     * are compared case-insensitively.
     */
    protected String getCanonicalizedBoatName(String boatName) {
        final String replaced = boatName.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return replaced.substring(0, Math.min(replaced.length(), 16));
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
    
    protected String canonicalizeId(String certificateId) {
        return certificateId.replaceAll(" ", "").replaceAll("\t", "");
    }

    @Override
    public Iterable<ORCCertificate> getCertificates() {
        return Util.map(getCertificateIds(), certificateId->getCertificateById(certificateId));
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
