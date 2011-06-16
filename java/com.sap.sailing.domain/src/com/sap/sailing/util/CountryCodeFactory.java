package com.sap.sailing.util;

import java.util.Locale;

import com.sap.sailing.util.impl.CountryCodeFactoryImpl;

public interface CountryCodeFactory {
    CountryCodeFactory INSTANCE = new CountryCodeFactoryImpl();
    
    CountryCode getFromLocale(Locale locale);
    
    CountryCode getFromThreeLetterIOCCode(String threeLetterIOCCode);
    
    CountryCode getFromTwoLetterISOCode(String twoLetterISOCode);
    
    Iterable<CountryCode> getAll();
}
