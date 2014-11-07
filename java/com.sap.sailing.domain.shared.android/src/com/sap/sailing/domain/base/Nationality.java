package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.CountryCode;
import com.sap.sse.common.Named;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface Nationality extends Named, WithImage, IsManagedByCache {
    @Dimension(messageKey="NationalityAcronym")
    String getThreeLetterIOCAcronym();
    
    CountryCode getCountryCode();
}
