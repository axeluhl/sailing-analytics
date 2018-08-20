package com.sap.sse.common.impl;

import com.sap.sse.common.CountryCode;

/**
 * Equality and hash are based on the {@link #threeLetterISOCode three-letter ISO code} only.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CountryCodeImpl implements CountryCode {
    private static final long serialVersionUID = 74265853281480956L;
    
    private final String name;
    private final String threeLetterIOCCode;
    private final String twoLetterISOCode;
    private final String threeLetterISOCode;
    private final String ianaInternet;
    private final String unVehicle;
    private final String unISONumeric;
    private final String ituCallPrefix;

    public CountryCodeImpl(String name, String twoLetterISOCode, String threeLetterISOCode, String ianaInternet,
            String unVehicle, String threeLetterIOCCode, String unISONumeric, String ituCallPrefix) {
        super();
        this.name = name;
        this.threeLetterIOCCode = threeLetterIOCCode;
        this.twoLetterISOCode = twoLetterISOCode;
        this.threeLetterISOCode = threeLetterISOCode;
        this.ianaInternet = ianaInternet;
        this.unVehicle = unVehicle;
        this.unISONumeric = unISONumeric;
        this.ituCallPrefix = ituCallPrefix;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getThreeLetterIOCCode() {
        return threeLetterIOCCode;
    }

    @Override
    public String getTwoLetterISOCode() {
        return twoLetterISOCode;
    }

    @Override
    public String getThreeLetterISOCode() {
        return threeLetterISOCode;
    }

    @Override
    public String getIANAInternet() {
        return ianaInternet;
    }

    @Override
    public String getUNVehicle() {
        return unVehicle;
    }

    @Override
    public String getUNISONumeric() {
        return unISONumeric;
    }

    @Override
    public String getITUCallPrefix() {
        return ituCallPrefix;
    }
    
    @Override
    public String toString() {
        return getThreeLetterIOCCode();
    }
}
