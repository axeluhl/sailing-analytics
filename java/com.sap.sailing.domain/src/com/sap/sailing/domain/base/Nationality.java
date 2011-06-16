package com.sap.sailing.domain.base;

import com.sap.sailing.util.CountryCode;

public interface Nationality extends Named, WithImage {
    String getThreeLetterIOCAcronym();
    
    CountryCode getCountryCode();
}
