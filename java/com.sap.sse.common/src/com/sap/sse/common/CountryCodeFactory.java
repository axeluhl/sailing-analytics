package com.sap.sse.common;

import com.sap.sse.common.impl.CountryCodeFactoryImpl;

public interface CountryCodeFactory {
    CountryCodeFactory INSTANCE = new CountryCodeFactoryImpl();

    Iterable<CountryCode> getAll();

    CountryCode getFromThreeLetterIOCName(String threeLetterIOCName);

    CountryCode getFromTwoLetterISOName(String twoLetterISOName);

    /**
     * Note that the three-letter ISO names can also be retrieved from a Java Locale, as follows:
     * <pre>
     *          getFromThreeLetterISOName(Locale.GERMANY.getISO3Country().toUpperCase())
     * </pre>
     */
    CountryCode getFromThreeLetterISOName(String threeLetterISOName);
    
    CountryCode getFromIANAInternet(String ianaInternet);

    CountryCode getFromUNVehicle(String unVehicle);

}
