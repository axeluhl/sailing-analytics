package com.sap.sailing.domain.base.impl;

import java.io.InputStream;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.util.CountryCode;
import com.sap.sailing.util.CountryCodeFactory;

public class NationalityImpl extends NamedImpl implements Nationality {
    private final String threeLetterIOCAcronym;
    
    public NationalityImpl(String name, String threeLetterIOCAcronym) {
        super(name);
        if (threeLetterIOCAcronym.length() != 3) {
            throw new IllegalArgumentException("Three-letter IOC nationality acronym \""+threeLetterIOCAcronym+"\" doesn't have three letters.");
        }
        this.threeLetterIOCAcronym = threeLetterIOCAcronym;
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
