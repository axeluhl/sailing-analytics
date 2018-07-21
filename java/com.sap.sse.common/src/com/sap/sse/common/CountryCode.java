package com.sap.sse.common;

import java.io.Serializable;

public interface CountryCode extends Serializable {

    String getTwoLetterISOCode();

    String getThreeLetterIOCCode();

    String getName();

    String getITUCallPrefix();

    String getUNISONumeric();

    String getUNVehicle();

    String getIANAInternet();

    String getThreeLetterISOCode();

}
