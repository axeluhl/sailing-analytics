package com.sap.sailing.domain.base.impl;

import java.io.InputStream;

import com.sap.sailing.domain.base.IsManagedBySharedDomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.CountryCodeFactory;

public class NationalityImpl implements Nationality {
    private static final long serialVersionUID = 238906193483424259L;
    private final String threeLetterIOCAcronym;
    
    public NationalityImpl(String threeLetterIOCAcronym) {
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

    @Override
    public IsManagedBySharedDomainFactory resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateNationality(getThreeLetterIOCAcronym());
    }
    
}
