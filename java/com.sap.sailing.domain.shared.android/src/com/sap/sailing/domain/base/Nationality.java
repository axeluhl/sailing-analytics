package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sailing.domain.common.Named;

public interface Nationality extends Named, WithImage, IsManagedBySharedDomainFactory {
    String getThreeLetterIOCAcronym();
    
    CountryCode getCountryCode();
}
