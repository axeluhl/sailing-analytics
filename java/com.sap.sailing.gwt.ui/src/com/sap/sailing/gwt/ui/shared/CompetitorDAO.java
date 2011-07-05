package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CompetitorDAO implements IsSerializable {
    public String name;
    public String twoLetterIsoCountryCode;
    public String threeLetterIocCountryCode;

    public CompetitorDAO() {}

    public CompetitorDAO(String name, String twoLetterIsoCountryCode, String threeLetterIocCountryCode) {
        this.name = name;
        this.twoLetterIsoCountryCode = twoLetterIsoCountryCode;
        this.threeLetterIocCountryCode = threeLetterIocCountryCode;
    }
    
}
