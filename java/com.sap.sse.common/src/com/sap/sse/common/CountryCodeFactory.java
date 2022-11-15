package com.sap.sse.common;

import com.sap.sse.common.impl.CountryCodeFactoryImpl;

public interface CountryCodeFactory {
    CountryCodeFactory INSTANCE = new CountryCodeFactoryImpl();

    Iterable<CountryCode> getAll();

    /**
     * @param threeLetterIOCName if {@code null} then {@code null} will be returned
     */
    CountryCode getFromThreeLetterIOCName(String threeLetterIOCName);

    /**
     * @param twoLetterISOName if {@code null} then {@code null} will be returned
     */
    CountryCode getFromTwoLetterISOName(String twoLetterISOName);

    /**
     * Note that the three-letter ISO names can also be retrieved from a Java Locale, as follows:
     * <pre>
     *          getFromThreeLetterISOName(Locale.GERMANY.getISO3Country().toUpperCase())
     * </pre>
     * 
     * @param threeLetterISOName if {@code null} then {@code null} will be returned
     */
    CountryCode getFromThreeLetterISOName(String threeLetterISOName);
    
    /**
     * @param ianaInternet if {@code null} then {@code null} will be returned
     */
    CountryCode getFromIANAInternet(String ianaInternet);

    /**
     * @param unVehicle if {@code null} then {@code null} will be returned
     */
    CountryCode getFromUNVehicle(String unVehicle);

}
