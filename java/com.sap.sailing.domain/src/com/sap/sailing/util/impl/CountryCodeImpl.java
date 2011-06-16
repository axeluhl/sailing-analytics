package com.sap.sailing.util.impl;

import java.util.Locale;

import com.sap.sailing.util.CountryCode;

public class CountryCodeImpl implements CountryCode {
    private final String name;
    private final String threeLetterIOCCode;
    private final String twoLetterISOCode;
    private final Locale locale;

    public CountryCodeImpl(String name, String threeLetterIOCCode, String twoLetterISOCode, Locale locale) {
        super();
        this.name = name;
        this.threeLetterIOCCode = threeLetterIOCCode;
        this.twoLetterISOCode = twoLetterISOCode;
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public String getThreeLetterIOCCode() {
        return threeLetterIOCCode;
    }

    public String getTwoLetterISOCode() {
        return twoLetterISOCode;
    }

    public Locale getLocale() {
        return locale;
    }
}
