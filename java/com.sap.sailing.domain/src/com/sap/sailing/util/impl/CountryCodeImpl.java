package com.sap.sailing.util.impl;

import java.util.Locale;

import com.sap.sailing.util.CountryCode;

public class CountryCodeImpl implements CountryCode {
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
    public Locale getLocale() {
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getISO3Country().equalsIgnoreCase(getThreeLetterISOCode())) {
                return l;
            }
        }
        return new Locale(getTwoLetterISOCode(), getTwoLetterISOCode());
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
}
