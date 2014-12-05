package com.sap.sailing.domain.regattalike;

import com.sap.sailing.domain.base.Regatta;

public class RegattaAsRegattaLikeIdentifier implements RegattaLikeIdentifier {
    private static final long serialVersionUID = 8770210543912486547L;
    private final String regattaName;
    
    public RegattaAsRegattaLikeIdentifier(Regatta regatta) {
        this.regattaName = regatta.getName();
    }

    @Override
    public String getName() {
        return regattaName;
    }

    @Override
    public void resolve(RegattaLikeIdentifierResolver resolver) {
        resolver.resolveOnRegattaIdentifier(this);
    }

}
