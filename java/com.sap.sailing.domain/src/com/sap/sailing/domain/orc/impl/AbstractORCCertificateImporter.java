package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.orc.ORCCertificateImporter;

public abstract class AbstractORCCertificateImporter implements ORCCertificateImporter {
    
    /**
     * From a sail number which may contain spaces, also redundantly and repeatedly, as well
     * as dashes, slashes or other special characters, and where upper and lower case may be mixed,
     * constructs a canonicalized version such that the chances for producing a match when searching
     * for a sail number is maximized, while still minimizing the chances for a false match.
     */
    protected String getCanonicalizedSailNumber(String sailNumber) {
        return sailNumber.replaceAll(" ", "").toUpperCase();
    }
}
