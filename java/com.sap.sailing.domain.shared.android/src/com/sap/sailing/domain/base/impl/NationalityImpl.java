package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.IsManagedByCache;

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
    public String getThreeLetterIOCAcronym() {
        return threeLetterIOCAcronym;
    }

    @Override
    public CountryCode getCountryCode() {
        return CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName(getThreeLetterIOCAcronym());
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getOrCreateNationality(getThreeLetterIOCAcronym());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((threeLetterIOCAcronym == null) ? 0 : threeLetterIOCAcronym.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NationalityImpl other = (NationalityImpl) obj;
        if (threeLetterIOCAcronym == null) {
            if (other.threeLetterIOCAcronym != null)
                return false;
        } else if (!threeLetterIOCAcronym.equals(other.threeLetterIOCAcronym))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getThreeLetterIOCAcronym();
    }
}
