package com.sap.sailing.domain.base;

import com.sap.sse.common.CountryCode;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.Named;
import com.sap.sse.datamining.annotations.Dimension;

public interface Nationality extends Named, IsManagedByCache<SharedDomainFactory> {
    @Dimension(messageKey="Acronym")
    String getThreeLetterIOCAcronym();
    
    CountryCode getCountryCode();
}
