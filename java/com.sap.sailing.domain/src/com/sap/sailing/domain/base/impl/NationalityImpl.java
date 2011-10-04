package com.sap.sailing.domain.base.impl;

import java.io.InputStream;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.util.CountryCode;
import com.sap.sailing.util.CountryCodeFactory;

public class NationalityImpl implements Nationality {
    private final String threeLetterIOCAcronym;
    
    public NationalityImpl(String name, String threeLetterIOCAcronym) {
        if (threeLetterIOCAcronym.length() != 3) {
            throw new IllegalArgumentException("Three-letter IOC nationality acronym \""+threeLetterIOCAcronym+"\" doesn't have three letters.");
        }
        this.threeLetterIOCAcronym = threeLetterIOCAcronym;
    }

    /**
     * The name is fetched from the country code. However, if this nationality has no valid country code, the three
     * letter IOC code is used as a default.
     */
    @Override
    public String getName() {
        return getCountryCode() == null ? getThreeLetterIOCAcronym() : getCountryCode().getName();
    }
    
    @Override
    public InputStream getImage() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getThreeLetterIOCAcronym() {
        return threeLetterIOCAcronym;
    }

    @Override
    public CountryCode getCountryCode() {
        return CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName(getThreeLetterIOCAcronym());
    }
    
}
