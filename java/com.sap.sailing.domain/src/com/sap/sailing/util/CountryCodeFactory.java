package com.sap.sailing.util;

import java.util.Locale;

import com.sap.sailing.util.impl.CountryCodeFactoryImpl;

public interface CountryCodeFactory {
    CountryCodeFactory INSTANCE = new CountryCodeFactoryImpl();

    Iterable<CountryCode> getAll();

    CountryCode getFromThreeLetterIOCName(String threeLetterIOCName);

    CountryCode getFromTwoLetterISOName(String twoLetterISOName);

    CountryCode getFromThreeLetterISOName(String threeLetterISOName);
    
    CountryCode getFromLocale(Locale locale);
    
    CountryCode getFromIANAInternet(String ianaInternet);

    CountryCode getFromUNVehicle(String unVehicle);

}
