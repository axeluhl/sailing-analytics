package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.RegattaSearchResult;

public class RegattaSearchResultImpl implements RegattaSearchResult {
    private final Regatta regatta;
    
    public RegattaSearchResultImpl(Regatta regatta) {
        this.regatta = regatta;
    }

    public Regatta getRegatta() {
        return regatta;
    }
}
