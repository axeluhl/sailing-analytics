package com.sap.sailing.util;

import java.util.Locale;

public interface CountryCode {
    Locale getLocale();

    String getTwoLetterISOCode();

    String getThreeLetterIOCCode();

    String getName();

    String getItuCallPrefix();

    String getUnISONumeric();

    String getUnVehicle();

    String getIanaInternet();

    String getThreeLetterISOCode();

}
